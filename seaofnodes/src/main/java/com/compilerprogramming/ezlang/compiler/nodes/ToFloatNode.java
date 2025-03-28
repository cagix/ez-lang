package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

import java.util.BitSet;

public class ToFloatNode extends Node {
    public ToFloatNode(Node lhs) { super(null, lhs); }

    @Override public String label() { return "ToFloat"; }

    @Override public String glabel() { return "(flt)"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        return in(1)._print0(sb.append("(flt)"), visited);
    }

    @Override
    public SONType compute() {
        if( in(1)._type == SONType.NIL ) return SONTypeFloat.FZERO;
        if (in(1)._type instanceof SONTypeInteger i0 ) {
            if( i0.isHigh() ) return SONTypeFloat.F64.dual();
            if( i0.isConstant() )
                return SONTypeFloat.constant(i0.value());
        }
        return SONTypeFloat.F64;
    }

    @Override public Node idealize() { return null; }
    @Override Node copy(Node lhs, Node rhs) { return new ToFloatNode(lhs); }
}
