package com.compilerprogramming.ezlang.compiler;

import java.util.BitSet;
import java.util.List;

/**
 * Compute LiveOut for each Basic Block
 * Implementation is based on description in 'Engineering a Compiler' 2nd ed.
 * pages 446-447.
 */
public class Liveness {

    public Liveness(CompiledFunction function) {
        List<BasicBlock> blocks = BBHelper.findAllBlocks(function.entry);
        RegisterPool regPool = function.registerPool;
        init(regPool, blocks);
        computeLiveness(blocks);
        function.hasLiveness = true;
    }

    private void init(RegisterPool regPool, List<BasicBlock> blocks) {
        int numRegisters = regPool.numRegisters();
        for (BasicBlock block : blocks) {
            block.UEVar = new LiveSet(numRegisters);
            block.varKill = new LiveSet(numRegisters);
            block.liveOut = new LiveSet(numRegisters);
            for (Instruction instruction : block.instructions) {
                for (Register use : instruction.uses()) {
                    if (!block.varKill.isMember(use))
                        block.UEVar.add(use);
                }
                if (instruction.definesVar()) {
                    Register def = instruction.def();
                    block.varKill.add(def);
                }
            }
        }
    }

    private void computeLiveness(List<BasicBlock> blocks) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock block : blocks) {
                if (recomputeLiveOut(block))
                    changed = true;
            }
        }
    }

    private boolean recomputeLiveOut(BasicBlock block) {
        LiveSet oldLiveOut = block.liveOut.dup();
        for (BasicBlock m: block.successors) {
            LiveSet mLiveIn = m.liveOut.dup();
            // LiveOut(m) intersect not VarKill(m)
            mLiveIn.intersectNot(m.varKill);
            // UEVar(m) union (LiveOut(m) intersect not VarKill(m))
            mLiveIn.union(m.UEVar);
            // LiveOut(block) =union (UEVar(m) union (LiveOut(m) intersect not VarKill(m)))
            block.liveOut.union(mLiveIn);
        }
        return !oldLiveOut.equals(block.liveOut);
    }
}
