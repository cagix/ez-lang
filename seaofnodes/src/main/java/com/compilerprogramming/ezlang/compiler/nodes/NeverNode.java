package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.Type;
import com.compilerprogramming.ezlang.compiler.sontypes.TypeTuple;

import java.util.BitSet;

// "Never true" for infinite loop exits
public class NeverNode extends IfNode {
    public NeverNode(NeverNode ctrl) { super(ctrl); }
    public NeverNode(Node ctrl) { super(ctrl,null); }

    @Override public String label() { return "Never"; }

    @Override public StringBuilder _print1(StringBuilder sb, BitSet visited) { return sb.append("Never"); }

    @Override public Type compute() { return TypeTuple.IF_BOTH; }

    @Override public Node idealize() { return null; }
}
