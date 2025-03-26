package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;
import java.util.BitSet;

public class DivNode extends Node {
    public DivNode(Node lhs, Node rhs) { super(null, lhs, rhs); }

    @Override public String label() { return "Div"; }

    @Override public String glabel() { return "//"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("("), visited);
        in(2)._print0(sb.append("/"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        SONType t1 = in(1)._type, t2 = in(2)._type;
        if( t1.isHigh() || t2.isHigh() )
            return SONTypeInteger.TOP;
        if( t1 instanceof SONTypeInteger i1 &&
            t2 instanceof SONTypeInteger i2 ) {
            if (i1.isConstant() && i2.isConstant())
                return i2.value() == 0
                    ? SONTypeInteger.ZERO
                    : SONTypeInteger.constant(i1.value()/i2.value());
        }
        return SONTypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        // Div of 1.
        if( in(2)._type == SONTypeInteger.TRUE )
            return in(1);
        return null;
    }

    @Override Node copy(Node lhs, Node rhs) { return new DivNode(lhs,rhs); }
    @Override Node copyF() { return new DivFNode(null,null); }
}
