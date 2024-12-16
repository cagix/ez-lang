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
        postOrderWalk(root, (n) -> nodes.add(n), new HashSet<>());
        return nodes;
    }

    static void postOrderWalk(BasicBlock n, Consumer<BasicBlock> consumer, HashSet<BasicBlock> visited) {
        visited.add(n);
        /* For each successor node */
        for (BasicBlock s : n.successors) {
            if (!visited.contains(s))
                postOrderWalk(s, consumer, visited);
        }
        consumer.accept(n);
    }
}
