package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

public class SarNode extends LogicalNode {
    public SarNode(Node lhs, Node rhs) { super(lhs, rhs); }

    @Override public String label() { return "Sar"; }
    @Override public String op() { return ">>"; }

    @Override public String glabel() { return "&gt;&gt;"; }

    @Override
    public SONType compute() {
        SONType t1 = in(1)._type, t2 = in(2)._type;
        if( t1.isHigh() || t2.isHigh() )
            return SONTypeInteger.TOP;
        if (t1 instanceof SONTypeInteger i0 &&
            t2 instanceof SONTypeInteger i1) {
            if( i0 == SONTypeInteger.ZERO )
                return SONTypeInteger.ZERO;
            if( i0.isConstant() && i1.isConstant() )
                return SONTypeInteger.constant(i0.value()>>i1.value());
            if( i1.isConstant() ) {
                int log = (int)i1.value();
                return SONTypeInteger.make(-1L<<(63-log),(1L<<(63-log))-1);
            }
        }
        return SONTypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        Node lhs = in(1);
        Node rhs = in(2);
        SONType t2 = rhs._type;

        // Sar of 0.
        if( t2.isConstant() && t2 instanceof SONTypeInteger i && (i.value()&63)==0 )
            return lhs;

        // TODO: x >> 3 >> (y ? 1 : 2) ==> x >> (y ? 4 : 5)

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new SarNode(lhs,rhs); }
}
