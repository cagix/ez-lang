package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeTuple;

import java.util.BitSet;

// "Never true" for infinite loop exits
public class NeverNode extends IfNode {
    public NeverNode(Node ctrl) { super(ctrl,Compiler.ZERO); }

    @Override public String label() { return "Never"; }

    @Override public StringBuilder _print1(StringBuilder sb, BitSet visited) { return sb.append("Never"); }

    @Override public SONType compute() { return SONTypeTuple.IF_BOTH; }

    @Override public Node idealize() { return null; }
}
