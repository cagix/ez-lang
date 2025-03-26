package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;
import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.util.BitSet;

public class MulNode extends Node {
    public MulNode(Node lhs, Node rhs) { super(null, lhs, rhs); }

    @Override public String label() { return "Mul"; }

    @Override public String glabel() { return "*"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("("), visited);
        in(2)._print0(sb.append("*"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        SONType t1 = in(1)._type, t2 = in(2)._type;
        if( t1.isHigh() || t2.isHigh() )
            return SONTypeInteger.TOP;
        if( t1 instanceof SONTypeInteger i1 &&
            t2 instanceof SONTypeInteger i2 ) {
            if( i1== SONTypeInteger.ZERO || i2== SONTypeInteger.ZERO)
                return SONTypeInteger.ZERO;
            if (i1.isConstant() && i2.isConstant())
                return SONTypeInteger.constant(i1.value()*i2.value());
        }
        return SONTypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        Node lhs = in(1);
        Node rhs = in(2);
        SONType t1 = lhs._type;
        SONType t2 = rhs._type;

        // Move constants to RHS: con*arg becomes arg*con
        if ( t1.isConstant() && !t2.isConstant() )
            return swap12();

        // Multiply by constant
        if ( t2.isConstant() && t2 instanceof SONTypeInteger i ) {
            // Mul of 1.  We do not check for (1*x) because this will already
            // canonicalize to (x*1)
            long c = i.value();
            if( c==1 )  return lhs;
            if( c==0 )  return Compiler.ZERO;
            // Mul by a power of 2, +/-1.  Bit patterns more complex than this
            // are unlikely to win on an X86 vs the normal "imul", and so
            // become machine-specific.
            if( (c & (c-1)) == 0 )
                return new ShlNode(lhs,con(Long.numberOfTrailingZeros(c)));
            // 2**n + 1, e.g. 9
            long d = c-1;
            if( (d & (d-1)) == 0 )
                return new AddNode(new ShlNode(lhs,con(Long.numberOfTrailingZeros(d))).peephole(),lhs);
            // 2**n - 1, e.g. 31
            long e = c+1;
            if( (e & (e-1)) == 0 )
                return new SubNode(new ShlNode(lhs,con(Long.numberOfTrailingZeros(e))).peephole(),lhs);

        }

        // Do we have ((x * (phi cons)) * con) ?
        // Do we have ((x * (phi cons)) * (phi cons)) ?
        // Push constant up through the phi: x * (phi con0*con0 con1*con1...)
        Node phicon = AddNode.phiCon(this,true);
        if( phicon!=null ) return phicon;

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new MulNode(lhs,rhs); }
    @Override Node copyF() { return new MulFNode(null,null); }
    @Override public CompilerException err() {
        if( in(1)._type.isHigh() || in(2)._type.isHigh() ) return null;
        if( !(in(1)._type instanceof SONTypeInteger) ) return Compiler.error("Cannot '"+label()+"' " + in(1)._type.glb());
        if( !(in(2)._type instanceof SONTypeInteger) ) return Compiler.error("Cannot '"+label()+"' " + in(2)._type.glb());
        return null;
    }
}
