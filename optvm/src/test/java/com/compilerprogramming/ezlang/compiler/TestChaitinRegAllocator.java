package com.compilerprogramming.ezlang.compiler;

import org.junit.Assert;
import org.junit.Test;

public class TestChaitinRegAllocator {

    /* Test move does not interfere with uses */
    @Test
    public void test4() {
        CompiledFunction function = TestInterferenceGraph.buildTest4();
        var graph = new InterferenceGraphBuilder().build(function);
        System.out.println(graph.generateDotOutput());
        var edges = graph.getEdges();
        Assert.assertEquals(2, edges.size());
        Assert.assertTrue(edges.contains(new InterferenceGraph.Edge(0, 1)));
        Assert.assertTrue(edges.contains(new InterferenceGraph.Edge(0, 2)));
        var regAssignments = new ChaitinGraphColoringRegisterAllocator().assignRegisters(function, 64, Options.OPT);
        String result = function.toStr(new StringBuilder(), false).toString();
        Assert.assertEquals("""
L0:
    a = 1
    b = 2
    t = b+a
    goto  L1
L1:
""", result);
        Assert.assertEquals(regAssignments.size(), 3);
        Assert.assertEquals(regAssignments.values().stream().sorted().distinct().count(), 2);
    }
}