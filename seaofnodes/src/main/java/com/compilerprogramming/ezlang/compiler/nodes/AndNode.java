package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

public class AndNode extends LogicalNode {
    public AndNode(Node lhs, Node rhs) { super(lhs, rhs); }

    @Override public String label() { return "And"; }
    @Override public String op() { return "&"; }

    @Override public String glabel() { return "&"; }

    @Override
    public SONType compute() {
        SONType t1 = in(1)._type, t2 = in(2)._type;
        if( t1.isHigh() || t2.isHigh() )
            return SONTypeInteger.TOP;
        if( t1 instanceof SONTypeInteger i0 &&
            t2 instanceof SONTypeInteger i1 ) {
            if( i0.isConstant() && i1.isConstant() )
                return SONTypeInteger.constant(i0.value()&i1.value());
            // Sharpen allowed bits if either value is narrowed
            long mask = i0.mask() & i1.mask();
            return mask < 0 ? SONTypeInteger.BOT : SONTypeInteger.make(0,mask);
        }
        return SONTypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        Node lhs = in(1);
        Node rhs = in(2);
        SONType t1 = lhs._type;
        SONType t2 = rhs._type;
        if( !(t1 instanceof SONTypeInteger && t2 instanceof SONTypeInteger t2i) )
            return null; // Malformed, e.g. (17 & 3.14)

        // And of -1.  We do not check for (-1&x) because this will already
        // canonicalize to (x&-1).  We do not check for zero, because
        // the compute() call will return a zero already.
        if( t2i.isConstant() && t2i.value()==-1 )
            return lhs;

        // Move constants to RHS: con*arg becomes arg*con
        if( t1.isConstant() && !t2.isConstant() )
            return swap12();

        // Do we have ((x & (phi cons)) & con) ?
        // Do we have ((x & (phi cons)) & (phi cons)) ?
        // Push constant up through the phi: x & (phi con0&con0 con1&con1...)
        Node phicon = AddNode.phiCon(this,true);
        if( phicon!=null ) return phicon;

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new AndNode(lhs,rhs); }
}
