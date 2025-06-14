package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;

import java.util.BitSet;

public class MulFNode extends Node {
    public MulFNode(Node lhs, Node rhs) { super(null, lhs, rhs); }

    @Override public String label() { return "MulF"; }

    @Override public String glabel() { return "*"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("("), visited);
        in(2)._print0(sb.append("*"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        if (in(1)._type instanceof SONTypeFloat i0 &&
            in(2)._type instanceof SONTypeFloat i1) {
            if (i0.isConstant() && i1.isConstant())
                return SONTypeFloat.constant(i0.value()*i1.value());
        }
        return in(1)._type.meet(in(2)._type);
    }

    @Override
    public Node idealize() {
        Node lhs = in(1);
        Node rhs = in(2);
        SONType t1 = lhs._type;
        SONType t2 = rhs._type;

        // Mul of 1.  We do not check for (1*x) because this will already
        // canonicalize to (x*1)
        if ( t2.isConstant() && t2 instanceof SONTypeFloat i && i.value()==1 )
            return lhs;

        // Move constants to RHS: con*arg becomes arg*con
        if ( t1.isConstant() && !t2.isConstant() )
            return swap12();

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new MulFNode(lhs,rhs); }
}
