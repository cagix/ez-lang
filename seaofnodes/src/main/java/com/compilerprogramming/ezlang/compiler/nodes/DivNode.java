package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.Type;
import com.compilerprogramming.ezlang.compiler.sontypes.TypeInteger;
import java.util.BitSet;

public class DivNode extends ArithNode {
    public DivNode(Node lhs, Node rhs) { super(lhs, rhs); }

    @Override public String label() { return "Div"; }
    @Override public String op() { return "//"; }

    @Override long doOp( long x, long y ) { return y==0 ? 0 : x / y; }
    @Override TypeInteger doOp(TypeInteger x, TypeInteger y) {
        return TypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        // Div of 1.
        if( in(2)._type == TypeInteger.TRUE )
            return in(1);
        return super.idealize();
    }

    @Override Node copy(Node lhs, Node rhs) { return new DivNode(lhs,rhs); }
    @Override Node copyF() { return new DivFNode(null,null); }
}
