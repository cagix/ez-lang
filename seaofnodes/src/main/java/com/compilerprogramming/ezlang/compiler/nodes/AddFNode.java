package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;

import java.util.BitSet;

public class AddFNode extends Node {
    public AddFNode(Node lhs, Node rhs) { super(null, lhs, rhs); }

    @Override public String label() { return "AddF"; }

    @Override public String glabel() { return "+"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("("), visited);
        in(2)._print0(sb.append("+"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        if (in(1)._type instanceof SONTypeFloat i0 &&
            in(2)._type instanceof SONTypeFloat i1) {
            if (i0.isConstant() && i1.isConstant())
                return SONTypeFloat.constant(i0.value()+i1.value());
        }
        return in(1)._type.meet(in(2)._type);
    }

    @Override
    public Node idealize() {
        Node lhs = in(1);
        SONType t2 = in(2)._type;

        // Add of 0.
        if ( t2.isConstant() && t2 instanceof SONTypeFloat i && i.value()==0 )
            return lhs;

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new AddFNode(lhs,rhs); }
}
