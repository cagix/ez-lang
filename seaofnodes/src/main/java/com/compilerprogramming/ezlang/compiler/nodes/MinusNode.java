package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

import java.util.BitSet;

public class MinusNode extends Node {
    public MinusNode(Node in) { super(null, in); }

    @Override public String label() { return "Minus"; }

    @Override public String glabel() { return "-"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("(-"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        if( in(1)._type.isHigh() ) return SONTypeInteger.TOP;
        if( in(1)._type instanceof SONTypeInteger i0 &&
            !(i0== SONTypeInteger.BOT || i0._min == Long.MIN_VALUE || i0._max == Long.MIN_VALUE) )
            return SONTypeInteger.make(-i0._max,-i0._min);
        return SONTypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        // -(-x) is x
        if( in(1) instanceof MinusNode minus )
            return minus.in(1);

        return null;
    }
    @Override Node copyF() { return new MinusFNode(null); }
}
