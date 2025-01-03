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

    @Test
    public void testDominatorTree() {
        List<BasicBlock> nodes = new ArrayList<>();
        BasicBlock root = makeGraph(nodes);
        DominatorTree tree = new DominatorTree(root);
        System.out.println(tree.generateDotOutput());
        long[] expectedIdoms = {0,1,1,2,2,4,2,6,6,6};
        for (BasicBlock n: nodes) {
            Assert.assertEquals(expectedIdoms[(int)n.bid], n.idom.bid);
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
