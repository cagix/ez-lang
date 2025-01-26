package com.compilerprogramming.ezlang.compiler;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Implements the original graph coloring algorithm described by Chaitin.
 * Since we are targeting an abstract machine where there are no limits on
 * number of registers except how we set them, our goal here is to get to
 * the minimum number of registers required to execute the function.
 * <p>
 * We do want to implement spilling even though we do not need it for the
 * abstract machine, but it is not yet implemented. We would spill to a
 * stack attached to the abstract machine.
 *
 * TODO spilling
 */
public class ChaitinGraphColoringRegisterAllocator {

    public Map<Integer, Integer> assignRegisters(CompiledFunction function, int numRegisters, EnumSet<Options> options) {
        if (function.isSSA) throw new IllegalStateException("Register allocation should be done after exiting SSA");
        // Remove useless copy operations
        InterferenceGraph g = coalesce(function, options);
        // Get used registers
        Set<Integer> registers = registersInIR(function);
        // Create color set
        List<Integer> colors = new ArrayList<>(IntStream.range(0, numRegisters).boxed().toList());
        // Function args are pre-assigned colors
        // and we remove them from the register set
        Map<Integer, Integer> assignments = preAssignArgsToColors(function, registers, colors);
        // TODO spilling
        // execute graph coloring on remaining registers
        assignments = colorGraph(g, registers, new HashSet<>(colors), assignments);
        // update all instructions
        // We simply set the slot on each register - rather than actually trying to replace them
        updateInstructions(function, assignments);
        // Compute and set the new framesize
        function.setFrameSize(computeFrameSize(assignments));
        if (options.contains(Options.DUMP_POST_CHAITIN_IR))
            function.dumpIR(false, "Post Chaitin Register Allocation");
        return assignments;
    }

    /**
     * Frame size = max number of registers needed to execute the function
     */
    private int computeFrameSize(Map<Integer, Integer> assignments) {
        return assignments.values().stream().mapToInt(k->k).max().orElse(0);
    }

    /**
     * Due to the way function args are received by the abstract machine, we need
     * to assign them register slots starting from 0. After assigning colors/slots
     * we remove these from the set so that the graph coloring algo does
     */
    private Map<Integer, Integer> preAssignArgsToColors(CompiledFunction function, Set<Integer> registers, List<Integer> colors) {
        int count = 0;
        Map<Integer, Integer> assignments = new HashMap<>();
        for (Instruction instruction : function.entry.instructions) {
            if (instruction instanceof Instruction.ArgInstruction argInstruction) {
                Integer color = colors.get(count);
                Register reg = argInstruction.arg().reg;
                registers.remove(reg.nonSSAId());   // Remove register from set before changing slot
                assignments.put(reg.nonSSAId(), color);
                count++;
            }
            else break;
        }
        return assignments;
    }

    private void updateInstructions(CompiledFunction function, Map<Integer, Integer> assignments) {
        var regPool = function.registerPool;
        // First reset the slots of every register to -1
        for (int r = 0; r < regPool.numRegisters(); r++)
            regPool.getReg(r).updateSlot(-1);
        // Now set the slot to the color assigned by the graph coloring algo
        for (var entry : assignments.entrySet()) {
            int reg = entry.getKey();
            int color = entry.getValue();
            regPool.getReg(reg).updateSlot(color);
        }
    }

    /**
     * Chaitin: coalesce_nodes - coalesce away copy operations
     */
    public InterferenceGraph coalesce(CompiledFunction function, EnumSet<Options> options) {
        boolean changed = true;
        InterferenceGraph igraph = null;
        while (changed) {
            igraph = new InterferenceGraphBuilder().build(function);
            changed = coalesceCopyOperations(function, igraph);
        }
        if (options.contains(Options.DUMP_CHAITIN_COALESCE)) {
            System.out.println("Post Chaitin Coalesce Registers");
            System.out.println(function.toStr(new StringBuilder(), false));
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
            for (Instruction instruction: block.instructions) {
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
            Integer c = assignedColors.get(neighbour);
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
    private Map<Integer, Integer> colorGraph(InterferenceGraph g, Set<Integer> nodes, Set<Integer> colors, Map<Integer, Integer> preAssignedColors) {
        if (nodes.size() == 0)
            return preAssignedColors;
        int numColors = colors.size();
        Integer node = findNodeWithNeighborCountLessThan(g, nodes, numColors);
        if (node == null)
            return null;
        Map<Integer, Integer> coloring = colorGraph(g.dup().subtract(node), subtract(nodes, node), colors, preAssignedColors);
        if (coloring == null)
            return null;
        Set<Integer> neighbourColors = getNeighborColors(g, node, coloring);
        Integer color = chooseSomeColorNotAssignedToNeighbors(colors, neighbourColors);
        coloring.put(node, color);
        return coloring;
    }

}
