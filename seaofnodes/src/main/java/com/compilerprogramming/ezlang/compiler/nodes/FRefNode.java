package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.Var;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.util.BitSet;

/**
 *  A Forward Reference.  Its any final constant, including functions.  When
 *  the Def finally appears its plugged into the forward reference, which then
 *  peepholes to the Def.
 */
public class FRefNode extends ConstantNode {
    public static final SONType FREF_TYPE = SONType.BOTTOM;
    public final Var _n;
    public FRefNode( Var n ) { super(FREF_TYPE); _n = n; }

    @Override public String label() { return "FRef"+_n; }

    @Override public String uniqueName() { return "FRef_" + _nid; }

    @Override public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        return sb.append("FRef_").append(_n);
    }

    @Override public Node idealize() {
        // When FRef finds its definition, idealize to it
        return nIns()==1 ? null : in(1);
    }

    public CompilerException err() { return Compiler.error("Undefined name '"+_n._name+"'"); }

    @Override public boolean eq(Node n) { return this==n; }
    @Override int hash() { return _n._idx; }
}
