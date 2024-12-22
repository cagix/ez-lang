package com.compilerprogramming.ezlang.compiler;

import java.util.ArrayList;
import java.util.List;

public class ChaitinGraphColoringRegisterAllocator {

    public ChaitinGraphColoringRegisterAllocator(CompiledFunction function) {
        coalesce(function);
    }

    private void coalesce(CompiledFunction function) {
        boolean changed = true;
        while (changed) {
            var igraph = new InterferenceGraphBuilder().build(function);
            changed = coalesceRegisters(function, igraph);
        }
    }

    private boolean coalesceRegisters(CompiledFunction function, InterferenceGraph igraph) {
        boolean changed = false;
        for (var block: function.getBlocks()) {
            List<Integer> instructionsToRemove = new ArrayList<>();
            for (int j = 0; j < block.instructions.size(); j++) {
                Instruction i = block.instructions.get(j);
                if (i instanceof Instruction.Move move
                    && move.from() instanceof Operand.RegisterOperand targetOperand) {
                    Register source = move.def();
                    Register target = targetOperand.reg;
                    if (source.id != target.id &&
                        !igraph.interfere(target.id, source.id)) {
                        igraph.rename(source.id, target.id);
                        rewriteInstructions(function, i, source, target);
                        instructionsToRemove.add(j);
                        changed = true;
                    }
                }
            }
            for (var j: instructionsToRemove) {
                block.instructions.set(j, new Instruction.NoOp());
            }
        }
        return changed;
    }

    private void rewriteInstructions(CompiledFunction function, Instruction notNeeded, Register source, Register target) {
        for (var block: function.getBlocks()) {
            for (Instruction i: block.instructions) {
                if (i == notNeeded)
                    continue;
                if (i.definesVar() && source.id == i.def().id)
                    i.replaceDef(target);
                i.replaceUse(source, target);
            }
        }
    }
}
