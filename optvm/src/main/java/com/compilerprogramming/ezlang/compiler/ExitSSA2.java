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
            // Insert copy in predecessor, since we are in CSSA, this is
            // a simple assignment from phi input to phi var
            for (var phi: phis) {
                for (int j = 0; j < phi.numInputs(); j++) {
                    BasicBlock pred = block.predecessor(j);
                    var phiInput = phi.input(j);
                    var phiVar = phi.value();
                    insertAtEnd(pred,new Instruction.Move(phiInput,new Operand.RegisterOperand(phiVar)));
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

    private boolean isEqual(Operand op1, Operand op2) {
        if (op1 instanceof Operand.RegisterOperand reg1 && op2 instanceof Operand.RegisterOperand reg2)
            return reg1.reg.id == reg2.reg.id;
        return false;
    }

    // The parallel move algo below is from
    // https://xavierleroy.org/publi/parallel-move.pdf
    // Tilting at windmills with Coq:
    // formal verification of a compilation algorithm
    // for parallel moves
    // Laurence Rideau, Bernard Paul Serpette, Xavier Leroy
    enum MoveStatus {
        TO_MOVE, BEING_MOVED, MOVED
    }

    static final class MoveCtx {
        Operand[] src;
        Operand[] dst;
        MoveStatus[] status;
        ArrayList<Instruction> copyInstructions;

        MoveCtx(Instruction.ParallelCopyInstruction pcopy) {
            src = pcopy.sourceOperands.toArray(new Operand[0]);
            dst = pcopy.destOperands.toArray(new Operand[0]);
            copyInstructions = new ArrayList<Instruction>();
            status = new MoveStatus[src.length];
            Arrays.fill(status, MoveStatus.TO_MOVE);
        }
    }

    private void moveOne(MoveCtx ctx, int i) {
        Operand[] src = ctx.src;
        Operand[] dst = ctx.dst;
        if (!isEqual(src[i], dst[i])) {
            ctx.status[i] = MoveStatus.BEING_MOVED;
            for (int j = 0; j < src.length; j++) {
                if (isEqual(src[j],dst[i])) {
                    // cycle found
                    switch (ctx.status[j]) {
                        case TO_MOVE:
                            moveOne(ctx, j);
                            break;
                        case BEING_MOVED:
                            var temp = new Operand.RegisterOperand(function.registerPool.newTempReg(src[j].type));
                            ctx.copyInstructions.add(new Instruction.Move(src[j], temp));
                            src[j] = temp;
                            break;
                        case MOVED:
                            break;
                    }
                }
            }
            ctx.copyInstructions.add(new Instruction.Move(src[i], dst[i]));
            ctx.status[i] = MoveStatus.MOVED;
        }
    }

    private void sequenceParallelCopy(BasicBlock block, Instruction.ParallelCopyInstruction parallelCopyInstruction) {
        var ctx = new MoveCtx(parallelCopyInstruction);
        for (int i = 0; i < ctx.src.length; i++)
            if (ctx.status[i] == MoveStatus.TO_MOVE)
                moveOne(ctx,i);
        replaceInstruction(block,parallelCopyInstruction,ctx.copyInstructions);
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
