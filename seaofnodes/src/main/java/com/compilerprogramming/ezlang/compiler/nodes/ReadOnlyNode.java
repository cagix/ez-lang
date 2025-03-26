package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeMemPtr;

import java.util.BitSet;

/**
 * Cast a pointer to read-only
 */
public class ReadOnlyNode extends Node {
    public ReadOnlyNode( Node n ) { super(null,n); }
    public ReadOnlyNode( ReadOnlyNode n ) { super(n); }
    @Override public String label() { return "ReadOnly"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        return in(1)._print0(sb.append("(const)"),visited);
    }
    @Override public SONType compute() {
        SONType t = in(1)._type;
        return t instanceof SONTypeMemPtr tmp ? tmp.makeRO() : t;
    }

    @Override public Node idealize() {
        if( in(1)._type instanceof SONTypeMemPtr tmp && tmp.isFinal() )
            return in(1);
        return null;
    }
}
