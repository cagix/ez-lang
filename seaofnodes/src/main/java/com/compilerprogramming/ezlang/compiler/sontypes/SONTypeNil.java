package com.compilerprogramming.ezlang.compiler.sontypes;

import java.util.ArrayList;

/**
 * Nil-able Scalar types
 */
public abstract class SONTypeNil extends SONType {
    // 0 = high-subclass choice nil
    // 1 = high-subclass no nil
    // 2 = low -subclass no nil
    // 3 = low -subclass also nil
    final byte _nil;

    SONTypeNil(byte t, byte nil ) { super(t); _nil = nil; }

    public static void gather(ArrayList<SONType> ts) { }

    abstract SONTypeNil makeFrom(byte nil);

    byte xmeet0(SONTypeNil that) { return (byte)Math.max(_nil,that._nil); }

    byte dual0() { return (byte)(3-_nil); }

    // RHS is NIL
    abstract SONType meet0();

    // RHS is XNIL
    SONType meetX() {
        return _nil==0 ? XNIL : (_nil<=2 ? SONTypePtr.NPTR : SONTypePtr.PTR);
    }

    SONType nmeet(SONTypeNil tn) {
        // Invariants: both are TypeNil subclasses and unequal classes.
        // If this is TypePtr, we went to TypePtr.nmeet and not here.
        // If that is TypePtr, this is not (invariant); reverse and go again.
        if( tn instanceof SONTypePtr ts ) return ts.nmeet(this);

        // Two mismatched TypeNil, no Scalar.
        if( _nil==0 && tn._nil==0 ) return XNIL;
        if( _nil<=2 && tn._nil<=2 ) return SONTypePtr.NPTR;
        return SONTypePtr.PTR;
    }

    @Override public boolean isHigh       () { return _nil <= 1; }
    @Override public boolean isConstant   () { return false; }
    @Override public boolean isHighOrConst() { return isHigh() || isConstant(); }

    @Override public SONType glb() { return SONType.NIL; }

    public boolean notNull() { return _nil==1 || _nil==2; }
    public boolean nullable() { return _nil==3; }

    final String q() { return _nil == 1 || _nil == 2 ? "" : "?"; }
    final String x() { return isHigh() ? "~" : ""; }

    int hash() { return _nil<<17; }

    boolean eq(SONTypeNil ptr) { return _nil == ptr._nil; }
}
