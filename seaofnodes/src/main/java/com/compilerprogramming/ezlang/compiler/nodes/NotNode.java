package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.*;
import java.util.BitSet;

public class NotNode extends Node {
    public NotNode(Node in) { super(null, in); }

    @Override public String label() { return "Not"; }

    @Override public String glabel() { return "!"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        in(1)._print0(sb.append("(!"), visited);
        return sb.append(")");
    }

    @Override
    public SONTypeInteger compute() {
        SONType t0 = in(1)._type;
        if( t0.isHigh() )  return SONTypeInteger.BOOL.dual();
        if( t0 == SONType.NIL || t0 == SONTypeInteger.ZERO ) return SONTypeInteger.TRUE;
        if( t0 instanceof SONTypeNil tn && tn.notNull() ) return SONTypeInteger.FALSE;
        if( t0 instanceof SONTypeInteger i && (i._min > 0 || i._max < 0) ) return SONTypeInteger.FALSE;
        return SONTypeInteger.BOOL;
    }

    @Override
    public Node idealize() { return null; }
}
