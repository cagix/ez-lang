package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;
import java.util.BitSet;

public class SubNode extends Node {
    public SubNode(Node lhs, Node rhs) { super(null, lhs, rhs); }

    @Override public String label() { return "Sub"; }

    @Override public String glabel() { return "-"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("("), visited);
        in(2)._print0(sb.append("-"), visited);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        SONType t1 = in(1)._type, t2 = in(2)._type;
        if( t1.isHigh() || t2.isHigh() )
            return SONTypeInteger.TOP;
        // Sub of same is 0
        if( in(1)==in(2) )
            return SONTypeInteger.ZERO;
        if( t1 instanceof SONTypeInteger i1 &&
            t2 instanceof SONTypeInteger i2 ) {
            if (i1.isConstant() && i2.isConstant())
                return SONTypeInteger.constant(i1.value()-i2.value());
            // Fold ranges like {2-3} - {0-1} into {1-3}.
            if( !AddNode.overflow(i1._min,-i2._max) &&
                !AddNode.overflow(i1._max,-i2._min) &&
                i2._min != Long.MIN_VALUE  )
                return SONTypeInteger.make(i1._min-i2._max,i1._max-i2._min);
        }

        return SONTypeInteger.BOT;
    }

    @Override
    public Node idealize() {
        // x - (-y) is x+y
        if( in(2) instanceof MinusNode minus )
            return new AddNode(in(1),minus.in(1));

        // (-x) - y is -(x+y)
        if( in(1) instanceof MinusNode minus )
            return new MinusNode(new AddNode(minus.in(1),in(2)).peephole());

        return null;
    }

    @Override Node copy(Node lhs, Node rhs) { return new SubNode(lhs,rhs); }
    @Override Node copyF() { return new SubFNode(null,null); }
}
