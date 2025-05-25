package com.compilerprogramming.ezlang.compiler;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestDominators {
    BasicBlock add(List<BasicBlock> nodes, BasicBlock node) {
        nodes.add(node);
        return node;
    }

    // This CFG example is taken from page 473 of
    // Engineering a Compiler 3rd ed
    BasicBlock makeGraph(List<BasicBlock> nodes) {
        BasicBlock r0 = add(nodes, new BasicBlock(1));
        BasicBlock r1 = add(nodes, new BasicBlock(2, r0));
        BasicBlock r2 = add(nodes, new BasicBlock(3, r1));
        BasicBlock r3 = add(nodes, new BasicBlock(4, r2));
        BasicBlock r4 = add(nodes, new BasicBlock(5, r3));
        BasicBlock r5 = add(nodes, new BasicBlock(6, r1));
        BasicBlock r6 = add(nodes, new BasicBlock(7, r5));
        BasicBlock r7 = add(nodes, new BasicBlock(8, r6));
        BasicBlock r8 = add(nodes, new BasicBlock(9, r5));
        r8.addSuccessor(r7);
        r7.addSuccessor(r3);
        r3.addSuccessor(r1);
        return r0;
    }

    // This DOM Tree example is taken from page 473 of
    // Engineering a Compiler 3rd ed
    @Test
    public void testDominatorTree() {
        List<BasicBlock> nodes = new ArrayList<>();
        BasicBlock root = makeGraph(nodes);
        DominatorTree tree = new DominatorTree(root);
        System.out.println(tree.generateDotOutput());
        // expected            {_,_,0,1,1,3,1,5,5,5}
        // Note first entry is not used
        // Note we set idom of root to itself so the second entry
        // does not match example
        long[] expectedIdoms = {-1,0,1,2,2,4,2,6,6,6};
        for (BasicBlock n: nodes) {
            if (expectedIdoms[(int)n.bid] == 0)
                Assert.assertNull(n.idom);
            else
                Assert.assertEquals(expectedIdoms[(int)n.bid], n.idom.bid);
        }
        // -1 means empty set
        long[] expectedDF = {0,-1,2,4,2,-1,4,8,4,8};
        for (BasicBlock n: nodes) {
            if (expectedDF[(int)n.bid] == -1) {
                Assert.assertTrue(n.dominationFrontier.isEmpty());
            }
            else {
                Assert.assertEquals(1,n.dominationFrontier.size());
            }
        }
    }

    BasicBlock makeGraph2(List<BasicBlock> nodes) {

        BasicBlock r1 = add(nodes, new BasicBlock(1));
        BasicBlock r2 = add(nodes, new BasicBlock(2, r1));
        BasicBlock r3 = add(nodes, new BasicBlock(3, r2));
        BasicBlock r4 = add(nodes, new BasicBlock(4, r2));
        BasicBlock r5 = add(nodes, new BasicBlock(5, r4));
        BasicBlock r6 = add(nodes, new BasicBlock(6, r4));
        BasicBlock r7 = add(nodes, new BasicBlock(7, r5, r6));
        BasicBlock r8 = add(nodes, new BasicBlock(8, r5));
        BasicBlock r9 = add(nodes, new BasicBlock(9, r8));
        BasicBlock r10 = add(nodes, new BasicBlock(10, r9));
        BasicBlock r11 = add(nodes, new BasicBlock(11, r7));
        BasicBlock r12 = add(nodes, new BasicBlock(12, r10, r11));

        r3.addSuccessor(r2);
        r4.addSuccessor(r2);
        r10.addSuccessor(r5);
        r9.addSuccessor(r8);
        return r1;
    }

    public String generateDotOutput(List<BasicBlock> nodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph g {\n");
        for (BasicBlock n: nodes)
            sb.append(n.uniqueName()).append(";\n");
        for (BasicBlock n: nodes) {
            for (BasicBlock use: n.successors) {
                sb.append(n.uniqueName()).append("->").append(use.uniqueName()).append(";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    @Test
    public void testLoopNests() {
        List<BasicBlock> nodes = new ArrayList<>();
        BasicBlock root = makeGraph2(nodes);
        System.out.println(generateDotOutput(nodes));
        DominatorTree tree = new DominatorTree(root);
        List<LoopNest> loopNests = LoopFinder.findLoops(nodes);
        Assert.assertEquals(2, loopNests.get(0)._loopHead.bid);
        Assert.assertEquals(2, loopNests.get(1)._loopHead.bid);
        Assert.assertEquals(5, loopNests.get(2)._loopHead.bid);
        Assert.assertEquals(8, loopNests.get(3)._loopHead.bid);
        List<LoopNest> loops = LoopFinder.mergeLoopsWithSameHead(loopNests);
        return;
    }

}
