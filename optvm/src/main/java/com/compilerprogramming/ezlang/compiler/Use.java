package com.compilerprogramming.ezlang.compiler;

import java.util.ArrayList;
import java.util.List;

public class Use {

    public void computeUses(CompiledFunction function) {
        List<BasicBlock> blocks = BBHelper.findAllBlocks(function.entry);
        RegisterPool regPool = function.registerPool;
        clearUses(regPool);
        computeUses(blocks);
    }

    private static void computeUses(List<BasicBlock> blocks) {
        for (BasicBlock block : blocks) {
            for (Instruction i: block.instructions) {
                if (i.usesVars()) {
                    for (Register use: i.uses()) {
                        recordUse(use, i);
                    }
                }
            }
        }
    }

    private static void recordUse(Register reg, Instruction use) {
        if (reg.uses == null)
            reg.uses = new ArrayList<>();
        reg.uses.add(use);
    }

    private void clearUses(RegisterPool regPool) {
        for (int i = 0; i < regPool.numRegisters(); i++) {
            Register reg = regPool.getReg(i);
            reg.uses = null;
        }
    }
}
