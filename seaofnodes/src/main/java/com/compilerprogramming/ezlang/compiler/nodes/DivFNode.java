package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;

import java.util.BitSet;

public class DivFNode extends Node {
    public DivFNode(Node lhs, Node rhs) { super(null, lhs, rhs); }

    @Override public String label() { return "DivF"; }

    @Override public String glabel() { return "/"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("("), visited);
        in(2)._print0(sb.append("/"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        if (in(1)._type instanceof SONTypeFloat i0 &&
            in(2)._type instanceof SONTypeFloat i1) {
            if (i0.isConstant() && i1.isConstant())
                return SONTypeFloat.constant(i0.value()/i1.value());
        }
        return in(1)._type.meet(in(2)._type);
    }

    @Override
    public Node idealize() {
        // Div of constant
        if( in(2)._type instanceof SONTypeFloat f && f.isConstant() )
            return new MulFNode(in(1),new ConstantNode(SONTypeFloat.constant(1.0/f.value())).peephole());

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new DivFNode(lhs,rhs); }
}
