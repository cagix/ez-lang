package com.compilerprogramming.ezlang.compiler.node;

import com.compilerprogramming.ezlang.compiler.type.Type;
import com.compilerprogramming.ezlang.compiler.type.TypeFloat;

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
    public Type compute() {
        if (in(1)._type instanceof TypeFloat i0)
            return i0.isConstant() ? TypeFloat.constant(-i0.value()) : i0;
        return TypeFloat.F64;
    }

    @Override
    public Node idealize() {
        // -(-x) is x
        if( in(1) instanceof MinusFNode minus )
            return minus.in(1);

        return null;
    }
}
