package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.sontypes.Type;
import com.compilerprogramming.ezlang.compiler.sontypes.TypeInteger;

public class ShlNode extends LogicalNode {
    public ShlNode(Node lhs, Node rhs) { super(lhs, rhs); }

    @Override public String label() { return "Shl"; }
    @Override public String op() { return "<<"; }

    @Override public String glabel() { return "&lt;&lt;"; }

    @Override
    public Type compute() {
        Type t1 = in(1)._type, t2 = in(2)._type;
        if( t1.isHigh() || t2.isHigh() )
            return TypeInteger.TOP;
        if (t1 instanceof TypeInteger i0 &&
            t2 instanceof TypeInteger i1 ) {
            if( i0 == TypeInteger.ZERO )
                return TypeInteger.ZERO;
            if( i0.isConstant() && i1.isConstant() )
                return TypeInteger.constant(i0.value()<<i1.value());
        }
        return TypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        Node lhs = in(1);
        Node rhs = in(2);

        if( rhs._type instanceof TypeInteger shl && shl.isConstant() ) {
            // Shl of 0.
            if( (shl.value()&63)==0 )
                return lhs;
            // (x + c) << i  =>  (x << i) + (c << i)
            if( lhs instanceof AddNode add && addDep(add).in(2)._type instanceof TypeInteger c && c.isConstant() ) {
                long sum = c.value() << shl.value();
                if( Integer.MIN_VALUE <= sum  && sum <= Integer.MAX_VALUE )
                    return new AddNode(new ShlNode(add.in(1),rhs).peephole(), Compiler.con(sum) );
            }
        }

        // TODO: x << 3 << (y ? 1 : 2) ==> x << (y ? 4 : 5)

        return null;
    }
    @Override Node copy(Node lhs, Node rhs) { return new ShlNode(lhs,rhs); }
}
