package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;
import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.util.BitSet;

public abstract class LogicalNode extends Node {

    public LogicalNode(Node lhs, Node rhs) { super(null, lhs, rhs); }
    abstract String op();

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("("), visited);
        in(2)._print0(sb.append(op()), visited);
        return sb.append(")");
    }


    @Override public CompilerException err() {
        if( in(1)._type.isHigh() || in(2)._type.isHigh() ) return null;
        if( !(in(1)._type instanceof SONTypeInteger) ) return Compiler.error("Cannot '"+op()+"' " + in(1)._type);
        if( !(in(2)._type instanceof SONTypeInteger) ) return Compiler.error("Cannot '"+op()+"' " + in(2)._type);
        return null;
    }
}
