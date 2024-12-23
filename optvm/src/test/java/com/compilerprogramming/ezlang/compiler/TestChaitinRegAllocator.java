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
        new ChaitinGraphColoringRegisterAllocator(function);
        System.out.println(function.toStr(new StringBuilder(), true));
    }
}