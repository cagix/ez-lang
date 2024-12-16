package com.compilerprogramming.ezlang.compiler;

import java.util.BitSet;
import java.util.List;

/**
 * Compute LiveOut for each Basic Block
 * Implementation is based on description in 'Engineering a Compiler' 2nd ed.
 * pages 446-447.
 */
public class Liveness {

    public void computeLiveness(CompiledFunction function) {
        List<BasicBlock> blocks = BBHelper.findAllBlocks(function.entry);
        RegisterPool regPool = function.registerPool;
        init(regPool, blocks);
        computeLiveness(blocks);
    }

    private void init(RegisterPool regPool, List<BasicBlock> blocks) {
        int numRegisters = regPool.numRegisters();
        for (BasicBlock block : blocks) {
            block.UEVar = new BitSet(numRegisters);
            block.varKill = new BitSet(numRegisters);
            block.liveOut = new BitSet(numRegisters);
            for (Instruction instruction : block.instructions) {
                if (instruction.usesVars()) {
                    for (Register use : instruction.uses()) {
                        if (!block.varKill.get(use.id))
                            block.UEVar.set(use.id);
                    }
                }
                if (instruction.definesVar()) {
                    Register def = instruction.def();
                    block.varKill.set(def.id);
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
        BitSet oldLiveOut = (BitSet) block.liveOut.clone();
        for (BasicBlock m: block.successors) {
            BitSet mLiveIn = (BitSet) m.liveOut.clone();
            // LiveOut(m) intersect not VarKill(m)
            mLiveIn.andNot(m.varKill);
            // UEVar(m) union (LiveOut(m) intersect not VarKill(m))
            mLiveIn.or(m.UEVar);
            // LiveOut(block) =union (UEVar(m) union (LiveOut(m) intersect not VarKill(m)))
            block.liveOut.or(mLiveIn);
        }
        return !oldLiveOut.equals(block.liveOut);
    }
}
