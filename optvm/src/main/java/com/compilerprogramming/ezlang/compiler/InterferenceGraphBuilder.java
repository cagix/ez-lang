package com.compilerprogramming.ezlang.compiler;

public class InterferenceGraphBuilder {

    public InterferenceGraph build(CompiledFunction function) {
        InterferenceGraph graph = new InterferenceGraph();
        // Calculate liveOut for all basic blocks
        function.livenessAnalysis();
        System.out.println(function.toStr(new StringBuilder(), true));
        var blocks = BBHelper.findAllBlocks(function.entry);
        for (var b : blocks) {
            // Start with the set of live vars at the end of the block
            // This liveness will be updated as we look through the
            // instructions in the block
            var liveNow = b.liveOut.dup();
            // liveNow is initially the set of values that are live (and avail?) at the
            // end of the block.
            // Process each instruction in the block in reverse order
            for (var i: b.instructions.reversed()) {
                if (i instanceof Instruction.Move) {
                    // Move(copy) instructions are handled specially to avoid
                    // adding an undesirable interference between the source and
                    // destination (section 2.2.2 in Briggs thesis)
                    // Engineering a Compiler: The copy operation does not
                    // create an interference cause both values can occupy the
                    // same register
                    // Same argument applies to phi. (Phi's do not generate uses)
                    liveNow.remove(i.uses());
                }
                if (i.definesVar()) {
                    var def = i.def();
                    // Defined vars interfere with all members of the live set
                    addInterference(graph, def, liveNow);
                    // Defined vars are removed from the live set
                    liveNow.dead(def);
                }
                // All used vars are added to the live set
                liveNow.live(i.uses());
            }
        }
        return graph;
    }

    private static void addInterference(InterferenceGraph graph, Register def, LiveSet liveSet) {
        for (int regNum = liveSet.nextSetBit(0); regNum >= 0; regNum = liveSet.nextSetBit(regNum+1)) {
            if (regNum != def.id)
                graph.addEdge(regNum, def.id);
        }
    }
}
