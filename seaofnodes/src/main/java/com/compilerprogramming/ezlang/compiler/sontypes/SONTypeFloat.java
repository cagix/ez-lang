package com.compilerprogramming.ezlang.compiler.sontypes;

import com.compilerprogramming.ezlang.compiler.Utils;
import java.util.ArrayList;

/**
 * Float Type
 */
public class SONTypeFloat extends SONType {

    public final static SONTypeFloat ONE = constant(1.0);
    public final static SONTypeFloat FZERO = constant(0.0);
    public final static SONTypeFloat F32 = make(32, 0);
    public final static SONTypeFloat F64 = make(64, 0);

    // - high -64, high -32, con 0, low +32, low +64
    public final byte _sz;

    /**
     * The constant value or 0
     */
    public final double _con;

    private SONTypeFloat(byte sz, double con) {
        super(TFLT);
        _sz = sz;
        _con = con;
    }
    private static SONTypeFloat make(int sz, double con) {
        return new SONTypeFloat((byte)sz,con).intern();
    }

    public static SONTypeFloat constant(double con) { return make(0, con); }

    public static void gather(ArrayList<SONType> ts) { ts.add(F64); ts.add(F32); ts.add(constant(3.141592653589793)); }

    @Override public String str() {
        return switch( _sz ) {
        case -64 -> "~flt";
        case -32 -> "~f32";
        case   0 -> ""+_con+((float)_con==_con ? "f" : "");
        case  32 ->  "f32";
        case  64 ->  "flt";
        default  -> throw Utils.TODO();
        };
    }
    private boolean isF32() { return ((float)_con)==_con; }

    @Override public boolean isHigh    () { return _sz< 0; }
    @Override public boolean isConstant() { return _sz==0; }
    @Override public int log_size() { return _sz==32 || _sz==-32 ? 2 : 3; }

    public double value() { return _con; }

    @Override
    public SONTypeFloat xmeet(SONType other) {
        SONTypeFloat f = (SONTypeFloat)other;
        // Invariant from caller: 'this' != 'other' and same class (TypeFloat).
        SONTypeFloat i = (SONTypeFloat)other; // Contract
        // Larger size in i1, smaller in i0
        SONTypeFloat i0 = _sz < i._sz ? this : i;
        SONTypeFloat i1 = _sz < i._sz ? i : this;

        if( i1._sz== 64 ) return F64;
        if( i0._sz==-64 ) return i1;
        if( i1._sz== 32 )
            return i0._sz==0 && !i0.isF32() ? F64 : F32;
        if( i1._sz!=  0 ) return i1;
        // i1 is a constant
        if( i0._sz==-32 )
            return i1.isF32() ? i1 : F64;
        // Since both are constants, and are never equals (contract) unequals
        // constants fall to bottom
        return i0.isF32() && i1.isF32() ? F32 : F64;
    }

    @Override
    public SONType dual() {
        return isConstant() ? this : make(-_sz,0); // Constants are a self-dual
    }

    @Override public SONType makeZero() { return FZERO; }
    @Override public SONType glb() { return F64; }

    @Override
    int hash() { return (int)(Double.hashCode(_con) ^ _sz ^ (1<<17)); }
    @Override
    public boolean eq( SONType t ) {
        SONTypeFloat i = (SONTypeFloat)t; // Contract
        // Allow NaN to check for equality
        return _sz==i._sz && Double.doubleToLongBits(_con)==Double.doubleToLongBits(i._con);
    }

}
