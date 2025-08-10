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
        sequentialzeParallelCopies();
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

    }

    private void sequentialzeParallelCopies() {

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
