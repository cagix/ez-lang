package com.compilerprogramming.ezlang.compiler;

import java.util.*;

public class InterferenceGraph {
    private Map<Integer, Set<Integer>> edges = new HashMap<>();

    private Set<Integer> addNode(Integer node) {
        var set = edges.get(node);
        if (set == null) {
            set = new HashSet<>();
            edges.put(node, set);
        }
        return set;
    }

    public void addEdge(Integer from, Integer to) {
        if (from == to) {
            return;
        }
        var set1 = addNode(from);
        var set2 = addNode(to);
        set1.add(to);
        set2.add(from);
    }

    /**
     * Remove a node from the interference graph
     * deleting it from all adjacency lists
     */
    public InterferenceGraph subtract(Integer node) {
        edges.remove(node);
        for (var key : edges.keySet()) {
            var neighbours = edges.get(key);
            neighbours.remove(key);
        }
        return this;
    }

    /**
     * Duplicate an interference graph
     */
    public InterferenceGraph dup() {
        var igraph = new InterferenceGraph();
        igraph.edges = new HashMap<>();
        for (var key : edges.keySet()) {
            var neighbours = edges.get(key);
            igraph.edges.put(key, new HashSet<>(neighbours));
        }
        return igraph;
    }

    public boolean interfere(Integer from, Integer to) {
        var set = edges.get(from);
        return set != null && set.contains(to);
    }

    /**
     * The source is replaced by target in the graph.
     * All nodes that interfered with source are made to interfere with target.
     */
    public void rename(Integer source, Integer target) {
        // Move all interferences
        var fromSet = edges.remove(source);
        var toSet = edges.get(target);
        if (toSet == null) {
            //throw new RuntimeException("Cannot find edge " + target + " from " + source);
            return; // FIXME this is workaround to handle scenario where target is arg register but we need a better way
        }
        toSet.addAll(fromSet);
        // If any node interfered with from
        // it should now interfere with to
        for (var k: edges.keySet()) {
            var set = edges.get(k);
            if (set.contains(source)) {
                set.remove(source);
                if (k != target)
                    set.add(target);
            }
        }
    }

    /**
     * Get neighbours of the node
     * Chaitin: neighbors()
     */
    public Set<Integer> neighbors(Integer node) {
        var adjacents = edges.get(node);
        if (adjacents == null)
            adjacents = Collections.emptySet();
        return adjacents;
    }

    public static final class Edge {
        public final int from;
        public final int to;
        public Edge(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return (from == edge.from && to == edge.to)
                    || (from == edge.to && to == edge.from);
        }

        @Override
        public int hashCode() {
            return from+to;
        }
    }

    public Set<Edge> getEdges() {
        Set<Edge> all = new HashSet<>();
        for (Integer from: edges.keySet()) {
            var set  = edges.get(from);
            for (Integer to: set) {
                all.add(new Edge(from, to));
            }
        }
        return all;
    }

    public String generateDotOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph IGraph {\n");
        for (var edge: getEdges()) {
            sb.append(edge.from).append("->").append(edge.to).append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

}
