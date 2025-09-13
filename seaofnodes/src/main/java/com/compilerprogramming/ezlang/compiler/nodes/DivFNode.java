package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.type.Type;
import com.compilerprogramming.ezlang.compiler.type.TypeFloat;

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
    public Type compute() {
        if (in(1)._type instanceof TypeFloat i0 &&
            in(2)._type instanceof TypeFloat i1) {
            if (i0.isConstant() && i1.isConstant())
                return TypeFloat.constant(i0.value()/i1.value());
        }
        return in(1)._type.meet(in(2)._type);
    }

    @Override
    public Node idealize() {
        // Div of constant
        if( in(2)._type instanceof TypeFloat f && f.isConstant() )
            return new MulFNode(in(1),new ConstantNode(TypeFloat.constant(1.0/f.value())).peephole());

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new DivFNode(lhs,rhs); }
}
