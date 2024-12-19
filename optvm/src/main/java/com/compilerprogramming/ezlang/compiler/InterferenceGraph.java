package com.compilerprogramming.ezlang.compiler;

import java.util.*;

public class InterferenceGraph {
    Map<Integer, Set<Integer>> edges = new HashMap<>();

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

    public boolean containsEdge(Integer from, Integer to) {
        var set = edges.get(from);
        return set != null && set.contains(to);
    }

    public Set<Integer> adjacents(Integer node) {
        return edges.get(node);
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
