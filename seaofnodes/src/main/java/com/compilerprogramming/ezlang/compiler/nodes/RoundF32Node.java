package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;

import java.util.BitSet;

public class RoundF32Node extends Node {
    public RoundF32Node(Node lhs) { super(null, lhs); }

    @Override public String label() { return "RoundF32"; }

    @Override public String glabel() { return "(f32)"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        return in(1)._print0(sb.append("((f32)"), visited).append(")");
    }

    @Override
    public SONType compute() {
        if (in(1)._type instanceof SONTypeFloat i0 && i0.isConstant() )
            return SONTypeFloat.constant((float)i0.value());
        return in(1)._type;
    }

    @Override
    public Node idealize() {
        Node lhs = in(1);
        SONType t1 = lhs._type;

        // RoundF32 of float
        if( t1 instanceof SONTypeFloat tf && tf._sz==32 )
            return lhs;

        return null;
    }
}
