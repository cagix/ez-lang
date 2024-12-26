package com.compilerprogramming.ezlang.compiler;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Implement the original graph coloring algorithm described by Chaitin.
 *
 * TODO spilling
 */
public class ChaitinGraphColoringRegisterAllocator {

    public ChaitinGraphColoringRegisterAllocator() {
    }

    public Map<Integer, Integer> assignRegisters(CompiledFunction function, int numRegisters) {
        if (function.isSSA) throw new IllegalStateException("Register allocation should be done after exiting SSA");
        var g = coalesce(function);
        var registers = registersInIR(function);
        var colors = IntStream.range(0, numRegisters).boxed().toList();
        // TODO pre-assign regs to args
        // TODO spilling
        var assignments = colorGraph(g, registers, new HashSet<>(colors));
        return assignments;
    }

    /**
     * Chaitin: coalesce_nodes - coalesce away copy operations
     */
    public InterferenceGraph coalesce(CompiledFunction function) {
        boolean changed = true;
        InterferenceGraph igraph = null;
        while (changed) {
            igraph = new InterferenceGraphBuilder().build(function);
            changed = coalesceCopyOperations(function, igraph);
        }
        return igraph;
    }

    /**
     * Chaitin: coalesce_nodes - coalesce away copy operations
     */
    private boolean coalesceCopyOperations(CompiledFunction function, InterferenceGraph igraph) {
        boolean changed = false;
        for (var block: function.getBlocks()) {
            Iterator<Instruction> iter = block.instructions.iterator();
            while (iter.hasNext()) {
                Instruction instruction = iter.next();
                if (instruction instanceof Instruction.Move move
                    && move.from() instanceof Operand.RegisterOperand registerTarget) {
                    Register source = move.def();
                    Register target = registerTarget.reg;
                    if (source.id != target.id &&
                        !igraph.interfere(target.id, source.id)) {
                        igraph.rename(source.id, target.id);
                        rewriteInstructions(function, instruction, source, target);
                        iter.remove();
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Chaitin: rewrite_il
     */
    private void rewriteInstructions(CompiledFunction function, Instruction deadInstruction, Register source, Register target) {
        for (var block: function.getBlocks()) {
            for (Instruction i: block.instructions) {
                if (i == deadInstruction)
                    continue;
                if (i.definesVar() && source.id == i.def().id)
                    i.replaceDef(target);
                i.replaceUse(source, target);
            }
        }
    }

    /**
     * Get the list of registers in use in the Intermediate Code
     * Chaitin: registers_in_il()
     */
    private Set<Integer> registersInIR(CompiledFunction function) {
        Set<Integer> registers = new HashSet<>();
        for (var block: function.getBlocks()) {
            Iterator<Instruction> iter = block.instructions.iterator();
            while (iter.hasNext()) {
                Instruction instruction = iter.next();
                if (instruction.definesVar())
                    registers.add(instruction.def().id);
                for (Register use: instruction.uses())
                    registers.add(use.id);
            }
        }
        return registers;
    }

    /**
     * Chaitin: color_graph line 2-3
     */
    private Integer findNodeWithNeighborCountLessThan(InterferenceGraph g, Set<Integer> nodes, int numColors) {
        for (var node: nodes) {
            if (g.neighbors(node).size() < numColors) {
                return node;
            }
        }
        return null;
    }

    private Set<Integer> getNeighborColors(InterferenceGraph g, Integer node, Map<Integer,Integer> assignedColors) {
        Set<Integer> colors = new HashSet<>();
        for (var neighbour: g.neighbors(node)) {
            var c = assignedColors.get(neighbour);
            if (c != null) {
                colors.add(c);
            }
        }
        return colors;
    }

    private Integer chooseSomeColorNotAssignedToNeighbors(Set<Integer> colors, Set<Integer> neighborColors) {
        // Create new color set that removes the colors assigned to neighbors
        var set = new HashSet<>(colors);
        set.removeAll(neighborColors);
        // pick a random color (we pick the first)
        return set.stream().findAny().orElseThrow();
    }

    private static HashSet<Integer> subtract(Set<Integer> originalSet, Integer node) {
        var reducedSet = new HashSet<>(originalSet);
        reducedSet.remove(node);
        return reducedSet;
    }

    /**
     * Chaitin: color_graph
     */
    private Map<Integer, Integer> colorGraph(InterferenceGraph g, Set<Integer> nodes, Set<Integer> colors) {
        if (nodes.size() == 0)
            return new HashMap<>();
        var numColors = colors.size();
        var node = findNodeWithNeighborCountLessThan(g, nodes, numColors);
        if (node == null)
            return null;
        var coloring = colorGraph(g.dup().subtract(node), subtract(nodes, node), colors);
        if (coloring == null)
            return null;
        var neighbourColors = getNeighborColors(g, node, coloring);
        var color = chooseSomeColorNotAssignedToNeighbors(colors, neighbourColors);
        coloring.put(node, color);
        return coloring;
    }

}
