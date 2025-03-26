package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;

import java.util.BitSet;

public class MinusFNode extends Node {
    public MinusFNode(Node in) { super(null, in); }

    @Override public String label() { return "MinusF"; }

    @Override public String glabel() { return "-"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("(-"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        if (in(1)._type instanceof SONTypeFloat i0)
            return i0.isConstant() ? SONTypeFloat.constant(-i0.value()) : i0;
        return SONTypeFloat.F64;
    }

    @Override
    public Node idealize() {
        // -(-x) is x
        if( in(1) instanceof MinusFNode minus )
            return minus.in(1);

        return null;
    }
}
