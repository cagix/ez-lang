package com.compilerprogramming.ezlang.compiler;

import java.util.*;

/**
 * Implement method to exit SSA by converting to conventional SSA,
 * without coalescing. This is the basic form.
 */
public class ExitSSA2 {

    CompiledFunction function;
    Map<BasicBlock,PCopy> parallelCopies = new HashMap<>();
    List<BasicBlock> allBlocks;

    public ExitSSA2(CompiledFunction function, EnumSet<Options> options) {
        this.function = function;
        allBlocks = function.getBlocks();
        insertPCopiesForEachBlock();
        makeConventionalSSA();
        removePhis();
        sequenceParallelCopies();
    }

    private void insertPCopiesForEachBlock() {
        // We do not actually insert pcopy instruction until needed
        // but we create an auxiliary data structure to help us track these
        for (BasicBlock block: allBlocks) {
            parallelCopies.put(block,new PCopy(block));
        }
    }
    private void insertAtEnd(BasicBlock bb, Instruction i) {
        assert bb.instructions.size() > 0;
        // Last instruction is a branch - so new instruction will
        // go before that
        int pos = bb.instructions.size()-1;
        bb.add(pos, i);
    }

    private Instruction.ParallelCopyInstruction getParallelCopyAtEnd(BasicBlock block) {
        PCopy pcopy = parallelCopies.get(block);
        if (pcopy.pCopyEnd == null) {
            pcopy.pCopyEnd = new Instruction.ParallelCopyInstruction();
            insertAtEnd(block,pcopy.pCopyEnd);
        }
        return pcopy.pCopyEnd;
    }

    private void insertAfterPhis(BasicBlock bb, Instruction newInst) {
        assert bb.instructions.size() > 0;
        int insertionPos = -1;
        for (int pos = 0; pos < bb.instructions.size(); pos++) {
            Instruction i = bb.instructions.get(pos);
            if (i instanceof Instruction.Phi) {
                insertionPos = pos+1;   // After phi
            }
            else
                break;
        }
        if (insertionPos < 0) {
            throw new IllegalStateException();
        }
        bb.add(insertionPos, newInst);
    }

    private Instruction.ParallelCopyInstruction getParallelCopyAtBegin(BasicBlock block) {
        PCopy pcopy = parallelCopies.get(block);
        if (pcopy.pCopyBegin == null) {
            pcopy.pCopyBegin = new Instruction.ParallelCopyInstruction();
            insertAfterPhis(block,pcopy.pCopyBegin);
        }
        return pcopy.pCopyBegin;
    }

    /**
     * Isolate phi nodes to make SSA conventionalS
     * This is Phase 1 as described in Engineering a Compiler 3rd Edition, p490.
     */
    private void makeConventionalSSA() {
        var blocks = function.getBlocks();
        for (BasicBlock block: blocks) {
            var phis = block.phis();
            if (phis.isEmpty())
                continue;
            for (var phi: phis) {
                for (int j = 0; j < phi.numInputs(); j++) {
                    BasicBlock pred = block.predecessor(j);
                    var pCopyBEnd = getParallelCopyAtEnd(pred);
                    var oldInput = phi.input(j);
                    var newInput = function.registerPool.newTempReg(oldInput.type);
                    pCopyBEnd.addCopy(oldInput,new Operand.RegisterOperand(newInput));
                    phi.replaceInput(j,newInput);
                }
                var oldPhiVar = phi.value();
                var newPhiVar = function.registerPool.newTempReg(oldPhiVar.type);
                phi.replaceValue(newPhiVar);
                var pCopyBBegin = getParallelCopyAtBegin(block);
                pCopyBBegin.addCopy(new Operand.RegisterOperand(newPhiVar),new Operand.RegisterOperand(oldPhiVar));
            }
        }
    }

    private void removePhis() {
        var blocks = function.getBlocks();
        for (BasicBlock block: blocks) {
            var phis = block.phis();
            if (phis.isEmpty())
                continue;
            // Insert copy in predecessor
            for (var phi: phis) {
                for (int j = 0; j < phi.numInputs(); j++) {
                    BasicBlock pred = block.predecessor(j);
                    var pCopyBEnd = getParallelCopyAtEnd(pred);
                    var phiInput = phi.input(j);
                    var phiVar = phi.value();
                    pCopyBEnd.addCopy(phiInput,new Operand.RegisterOperand(phiVar));
                }
            }
            block.instructions.removeIf(instruction -> instruction instanceof Instruction.Phi);
        }
    }

    private void sequenceParallelCopies() {
        for (var block: function.getBlocks()) {
            var pcopy = parallelCopies.get(block);
            if (pcopy.pCopyBegin != null)
                sequenceParallelCopy(block,pcopy.pCopyBegin);
            if (pcopy.pCopyEnd != null)
                sequenceParallelCopy(block,pcopy.pCopyEnd);
        }
    }

    private void replaceInstruction(BasicBlock block, Instruction.ParallelCopyInstruction pcopy, ArrayList<Instruction> instructions) {
        block.replaceInstruction(pcopy, instructions);
    }

    static final class Copy {
        Operand src;
        Operand dest;
        boolean removed = false;

        public Copy(Operand src, Operand dest) {
            this.src = src;
            this.dest = dest;
        }
    }

    List<Copy> getCopies(Instruction.ParallelCopyInstruction pcopy) {
        List<Copy> copies = new ArrayList<>();
        for (int i = 0; i < pcopy.sourceOperands.size(); i++) {
            var src = pcopy.sourceOperands.get(i);
            var dest = pcopy.destOperands.get(i);
            if (src instanceof Operand.RegisterOperand srcR && dest instanceof Operand.RegisterOperand destR) {
                if (srcR.reg.id == destR.reg.id)
                    continue;
            }
            copies.add(new Copy(src,dest));
        }
        return copies;
    }

    private void sequenceParallelCopy(BasicBlock block, Instruction.ParallelCopyInstruction pcopy) {
        var copyInstructions = new ArrayList<Instruction>();
        var copies = getCopies(pcopy);

        while (copies.size() > 0) {
            boolean progress = false;

            for (var copy: copies) {
                boolean cycle = false;
                for (int i = 0; i < copies.size(); i++) {
                    if (copy.removed == true)
                        continue;
                    if (copy.src.equals(copies.get(i).dest)) {
                        cycle = true;
                        break;
                    }
                }
                if (!cycle) {
                    copyInstructions.add(new Instruction.Move(copy.src,copy.dest));
                    copy.removed = true;
                    progress = true;
                }
            }

            copies.removeIf(c->c.removed);
            if (progress)
                continue;

            var copy = copies.removeFirst();
            var temp = new Operand.RegisterOperand(function.registerPool.newTempReg(copy.src.type));
            copyInstructions.add(new Instruction.Move(copy.src,temp));
            copies.add(new Copy(copy.dest,temp));
        }
        replaceInstruction(block,pcopy,copyInstructions);
    }

    private void sequenceParallelCopyX(BasicBlock block, Instruction.ParallelCopyInstruction pcopy) {
        var copyInstructions = new ArrayList<Instruction>();
        var ready = new ArrayList<Operand>();
        var toDo = new ArrayList<Operand>();
        var directPred = new HashMap<Operand,Operand>();
        var loc = new HashMap<Operand,Operand>();
        for (int i = 0; i <pcopy.sourceOperands.size(); i++) {
            var a = pcopy.sourceOperands.get(i);
            var b = pcopy.destOperands.get(i);
            if (a.equals(b))
                continue;
            loc.put(a,a);
            directPred.put(b,a);
            toDo.add(b);
        }
        for (int i = 0; i <pcopy.sourceOperands.size(); i++) {
            var a = pcopy.sourceOperands.get(i);
            var b = pcopy.destOperands.get(i);
            if (a == b)
                continue;
            if (loc.get(b) == null)
                ready.add(b);
        }
        while (!toDo.isEmpty()) {
            while (!ready.isEmpty()) {
                var b = ready.removeLast();
                var a = directPred.get(b);
                var c = loc.get(a);
                copyInstructions.add(new Instruction.Move(c,b));
                loc.put(a,b);
                if (a == c && directPred.get(a) != null)
                    ready.add(a);
            }
            var b = toDo.removeLast();
            if (b == loc.get(directPred.get(b))) {
                var n = new Operand.RegisterOperand(function.registerPool.newTempReg(b.type));
                copyInstructions.add(new Instruction.Move(b,n));
                loc.put(b,n);
                ready.add(b);
            }
        }
        replaceInstruction(block,pcopy,copyInstructions);
    }

    static final class PCopy {
        BasicBlock block;
        /**
         * Parallel copy instruction after any Phi instructions
         * in the block, null if not present
         */
        Instruction.ParallelCopyInstruction pCopyBegin = null;
        /**
         * Parallel copy instruction at the end of a block,
         * null if not present
         */
        Instruction.ParallelCopyInstruction pCopyEnd = null;

        public PCopy(BasicBlock block) {
            this.block = block;
        }
    }
}
