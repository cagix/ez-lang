package com.compilerprogramming.ezlang.compiler.sontypes;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.Utils;
import java.util.ArrayList;

/**
 * Represents a Scalar; a single register-sized value.
 */
public class SONTypePtr extends SONTypeNil {

    private SONTypePtr(byte nil) { super(TPTR,nil);  }

    // An abstract pointer, pointing to either a Struct or an Array.
    // Can also be null or not, so 4 choices {TOP,BOT} x {nil,not}
    public static SONTypePtr XPTR = new SONTypePtr((byte)0).intern();
    public static SONTypePtr XNPTR= new SONTypePtr((byte)1).intern();
    public static SONTypePtr NPTR = new SONTypePtr((byte)2).intern();
    public static SONTypePtr PTR  = new SONTypePtr((byte)3).intern();
    private static final SONTypePtr[] PTRS = new SONTypePtr[]{XPTR,XNPTR,NPTR,PTR};

    public static void gather(ArrayList<SONType> ts) { ts.add(PTR); ts.add(NPTR); }

    SONTypeNil makeFrom(byte nil) { throw Utils.TODO(); }

    @Override public SONTypeNil xmeet(SONType t) {
        SONTypePtr that = (SONTypePtr) t;
        return PTRS[xmeet0(that)];
    }

    @Override public SONTypePtr dual() { return PTRS[dual0()]; }

    // High scalar loses, low scalar wins
    @Override
    SONTypeNil nmeet(SONTypeNil tn) {
        if( _nil==0 ) return tn; // High scalar loses
        if( _nil==1 ) return tn.makeFrom(xmeet0(tn)); // High scalar loses
        if( _nil==2 ) return tn._nil==3 ? PTR : NPTR; // Low scalar wins
        return PTR; // this
    }


    // RHS is  NIL
    @Override
    SONType meet0() { return isHigh() ? NIL : PTR; }
    // RHS is XNIL
    // 0->xscalar, 1->nscalar, 2->nscalar, 3->scalar
    @Override
    SONType meetX() { return _nil==0 ? XNIL : (_nil==3 ? PTR : NPTR); }

    @Override public SONTypePtr glb() { return PTR; }

    private static final String[] STRS = new String[]{"~ptr","~nptr","nptr","ptr"};
    @Override public String str() { return STRS[_nil]; }
    @Override public SB print(SB sb) { return sb.p(str()); }
}
