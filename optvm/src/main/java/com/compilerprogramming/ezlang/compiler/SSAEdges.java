package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SSA Edges are connections from the unique point where a variable is
 * given a value to a use of that variable. SSA edges are essentially
 * def-use chains in the SSA program.
 */
public class SSAEdges {

    public static final class SSADef {
        /**
         * Instruction where the definition occurs
         */
        public final Instruction instruction;
        /**
         * Instructions that use the definition
         */
        public final List<Instruction> useList;

        public SSADef(Instruction instruction) {
            this.instruction = instruction;
            this.useList = new ArrayList<>();
        }
    }

    public static Map<Register, SSADef> buildDefUseChains(CompiledFunction function) {

        if (!function.isSSA) throw new CompilerException("Function must be in SSA form");

        Map<Register, SSADef> defUseChains = new HashMap<>();
        recordDefs(function, defUseChains);
        recordUses(function, defUseChains);

        return defUseChains;
    }

    private static void recordDefs(CompiledFunction function, Map<Register, SSADef> defUseChains) {
        for (BasicBlock block : function.getBlocks()) {
            for (Instruction instruction : block.instructions) {
                if (instruction instanceof Instruction.Phi phi) {
                    recordDef(defUseChains, phi.value(), instruction);
                }
                else if (instruction.definesVar()) {
                    recordDef(defUseChains, instruction.def(), instruction);
                }
            }
        }
    }

    private static void recordUses(CompiledFunction function, Map<Register, SSADef> defUseChains) {
        for (BasicBlock block : function.getBlocks()) {
            for (Instruction instruction : block.instructions) {
                if (instruction instanceof Instruction.Phi phi) {
                    recordUses(defUseChains, phi.inputRegisters(), block, instruction);
                }
                else {
                    List<Register> uses = instruction.uses();
                    if (!uses.isEmpty())
                        recordUses(defUseChains, uses.toArray(new Register[uses.size()]), block, instruction);
                }
            }
        }
    }

    private static void recordUses(Map<Register, SSADef> defUseChains, Register[] inputs, BasicBlock block, Instruction instruction) {
        for (Register register : inputs) {
            SSADef def = defUseChains.get(register);
            def.useList.add(instruction);
        }
    }

    private static void recordDef(Map<Register, SSADef> defUseChains, Register value, Instruction instruction) {
        if (defUseChains.containsKey(value))
            throw new CompilerException("Register already defined, invalid multiple definition in SSA");
        defUseChains.put(value, new SSADef(instruction));
    }
}
