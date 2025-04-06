package com.compilerprogramming.ezlang.compiler.sontypes;

import com.compilerprogramming.ezlang.compiler.SB;
import java.util.ArrayList;

public class SONTypeTuple extends SONType {

    public final SONType[] _types;

    public SONTypeTuple(SONType[] types) { super(TTUPLE); _types = types; }
    public  static SONTypeTuple make(SONType... types) { return new SONTypeTuple(types).intern(); }

    public static final SONTypeTuple BOT = new SONTypeTuple(new SONType[0]).intern();
    public static final SONTypeTuple TOP = new SONTypeTuple(null).intern();

    //public static final SONTypeTuple TEST = make(SONTypeInteger.BOT, SONTypeMemPtr.TEST);
    public static final SONTypeTuple START= make(SONType.CONTROL, SONTypeMem.TOP, SONTypeInteger.BOT);
    public static final SONTypeTuple MAIN = make(SONTypeInteger.BOT);
    public static final SONTypeTuple RET  = make(SONType.CONTROL, SONTypeMem.BOT, SONType.BOTTOM);

    public static final SONTypeTuple IF_BOTH    = make(new SONType[]{SONType. CONTROL, SONType. CONTROL});
    public static final SONTypeTuple IF_NEITHER = make(new SONType[]{SONType.XCONTROL, SONType.XCONTROL});
    public static final SONTypeTuple IF_TRUE    = make(new SONType[]{SONType. CONTROL, SONType.XCONTROL});
    public static final SONTypeTuple IF_FALSE   = make(new SONType[]{SONType.XCONTROL, SONType. CONTROL});

    public  static void gather(ArrayList<SONType> ts) { ts.add(BOT); /* ts.add(TEST); */ ts.add(START); ts.add(MAIN); ts.add(IF_TRUE); }

    @Override
    SONType xmeet(SONType other) {
        SONTypeTuple tt = (SONTypeTuple)other;     // contract from xmeet
        if( this==TOP ) return other;
        if( tt  ==TOP ) return this ;
        if( _types.length != tt._types.length )
            return BOT;
        SONType[] ts = new SONType[_types.length];
        for( int i=0; i<_types.length; i++ )
            ts[i] = _types[i].meet(tt._types[i]);
        return make(ts);
    }

    @Override public SONTypeTuple dual() {
        if( this==TOP ) return BOT;
        if( this==BOT ) return TOP;
        SONType[] ts = new SONType[_types.length];
        for( int i=0; i<_types.length; i++ )
            ts[i] = _types[i].dual();
        return make(ts);
    }

    @Override
    public SONType glb() {
        SONType[] ts = new SONType[_types.length];
        for( int i=0; i<_types.length; i++ )
            ts[i] = _types[i].glb();
        return make(ts);
    }

    @Override public boolean isConstant() {
        for( SONType t : _types )
            if( !t.isConstant() )
                return false;
        return true;
    }

    @Override public int log_size() {
        assert isConstant();
        int log_size = 0;
        for( SONType t : _types )
            log_size = Math.max(log_size,t.log_size());
        return log_size;
    }

    public SONType ret() { assert _types.length==3; return _types[2]; }

    @Override public String str() { return print(new SB()).toString(); }

    @Override public SB print(SB sb) {
        if( this==TOP ) return sb.p("[TOP]");
        if( this==BOT ) return sb.p("[BOT]");
        sb.p("[  ");
        for( SONType t : _types )
            t.print(sb).p(", ");
        return sb.unchar(2).p("]");
    }
    @Override public SB gprint(SB sb) {
        if( this==TOP ) return sb.p("[TOP]");
        if( this==BOT ) return sb.p("[BOT]");
        sb.p("[  ");
        for( SONType t : _types )
            t.gprint(sb).p(", ");
        return sb.unchar(2).p("]");
    }

    @Override
    int hash() {
        int sum = 0;
        if( _types!=null ) for( SONType type : _types ) sum ^= type.hashCode();
        return sum;
    }

    @Override
    boolean eq( SONType t ) {
        SONTypeTuple tt = (SONTypeTuple)t; // Contract
        if( _types==null && tt._types==null ) return true;
        if( _types==null || tt._types==null ) return false;
        if( _types.length != tt._types.length ) return false;
        for( int i=0; i<_types.length; i++ )
            if( _types[i]!=tt._types[i] )
                return false;
        return true;
    }


}
