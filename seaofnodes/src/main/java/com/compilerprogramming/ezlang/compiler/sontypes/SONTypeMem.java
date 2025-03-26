package com.compilerprogramming.ezlang.compiler.sontypes;

import com.compilerprogramming.ezlang.compiler.SB;

import java.util.ArrayList;


/**
 * Represents a slice of memory corresponding to a set of aliases
 */
public class SONTypeMem extends SONType {

    // Which slice of memory?
    //  0 means TOP, no slice.
    //  0 means BOT, all memory.
    //  N means slice#N.
    public final int _alias;
    public final SONType _t;       // Memory contents, some scalar type

    private SONTypeMem(int alias, SONType t) {
        super(TMEM);
        assert alias!=0 || (t== SONType.TOP || t== SONType.BOTTOM);
        _alias = alias;
        _t = t;
    }

    public static SONTypeMem make(int alias, SONType t) { return new SONTypeMem(alias,t).intern(); }
    public static final SONTypeMem TOP = make(0, SONType.TOP   );
    public static final SONTypeMem BOT = make(0, SONType.BOTTOM);

    public static void gather(ArrayList<SONType> ts) { ts.add(make(1, SONType.NIL)); ts.add(make(1, SONTypeInteger.ZERO)); ts.add(BOT); }

    @Override
    SONTypeMem xmeet(SONType t) {
        SONTypeMem that = (SONTypeMem) t; // Invariant: TypeMem and unequal
        if( this==TOP ) return that;
        if( that==TOP ) return this;
        if( this==BOT ) return BOT;
        if( that==BOT ) return BOT;
        int alias = _alias==that._alias ? _alias : 0;
        SONType mt = _t.meet(that._t);
        return make(alias,mt);
    }

    @Override
    public SONType dual() {
        return make(_alias,_t.dual());
    }

    @Override public boolean isHigh() { return _t.isHigh(); }
    @Override public SONType glb() { return make(_alias,_t.glb()); }

    @Override int hash() { return 9876543 + _alias + _t.hashCode(); }

    @Override boolean eq(SONType t) {
        SONTypeMem that = (SONTypeMem) t; // Invariant
        return _alias == that._alias && _t == that._t;
    }

    @Override public SB print(SB sb) {
        sb.p("#");
        if( _alias==0 ) return sb.p(_t._type==TTOP ? "TOP" : "BOT");
        return _t.print(sb.p(_alias).p(":"));
    }
    @Override public SB gprint(SB sb) {
        sb.p("#");
        if( _alias==0 ) return sb.p(_t._type==TTOP ? "TOP" : "BOT");
        return _t.gprint(sb.p(_alias).p(":"));
    }

    @Override public String str() { return print(new SB()).toString(); }
}
