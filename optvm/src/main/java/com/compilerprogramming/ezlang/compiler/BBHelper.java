package com.compilerprogramming.ezlang.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class BBHelper {
    /**
     * Utility to locate all the basic blocks, order does not matter.
     */
    public static List<BasicBlock> findAllBlocks(BasicBlock root) {
        List<BasicBlock> nodes = new ArrayList<>();
        postOrderWalkForwardCFG(root, (n) -> nodes.add(n), new HashSet<>());
        return nodes;
    }

    public static List<BasicBlock> findAllBlocksPostOrderForwardCFG(CompiledFunction function) {
        List<BasicBlock> nodes = new ArrayList<>();
        postOrderWalkForwardCFG(function.entry, (n) -> nodes.add(n), new HashSet<>());
        return nodes;
    }

    public static List<BasicBlock> findAllBlocksReversePostOrderForwardCFG(CompiledFunction function) {
        List<BasicBlock> nodes = new ArrayList<>();
        postOrderWalkForwardCFG(function.entry, (n) -> nodes.add(0,n), new HashSet<>());
        return nodes;
    }

    static void postOrderWalkForwardCFG(BasicBlock n, Consumer<BasicBlock> consumer, HashSet<BasicBlock> visited) {
        visited.add(n);
        /* For each successor node */
        for (BasicBlock s : n.successors) {
            if (!visited.contains(s))
                postOrderWalkForwardCFG(s, consumer, visited);
        }
        consumer.accept(n);
    }

    static void postOrderWalkReverseCFG(BasicBlock n, Consumer<BasicBlock> consumer, HashSet<BasicBlock> visited) {
        visited.add(n);
        /* For each successor node */
        for (BasicBlock s : n.predecessors) {
            if (!visited.contains(s))
                postOrderWalkReverseCFG(s, consumer, visited);
        }
        consumer.accept(n);
    }

    public static List<BasicBlock> findAllBlocksPostOrderReverseCFG(CompiledFunction function) {
        List<BasicBlock> nodes = new ArrayList<>();
        postOrderWalkReverseCFG(function.exit, (n) -> nodes.add(n), new HashSet<>());
        return nodes;
    }

    public static List<BasicBlock> findAllBlocksReversePostOrderReverseCFG(CompiledFunction function) {
        List<BasicBlock> nodes = new ArrayList<>();
        postOrderWalkReverseCFG(function.exit, (n) -> nodes.add(0,n), new HashSet<>());
        return nodes;
    }
}
