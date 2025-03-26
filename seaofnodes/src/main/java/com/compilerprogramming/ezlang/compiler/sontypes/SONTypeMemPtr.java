package com.compilerprogramming.ezlang.compiler.sontypes;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.Utils;
import java.util.ArrayList;

/**
 * Represents a Pointer to memory.
 *
 * Null is generic pointer to non-existent memory.
 * *void is a non-Null pointer to all possible refs, both structs and arrays.
 * Pointers can be to specific struct and array types, or a union with Null.
 * The distinguished *$BOT ptr represents union of *void and Null.
 * The distinguished *$TOP ptr represents the dual of *$BOT.
 */
public class SONTypeMemPtr extends SONTypeNil {
    // A TypeMemPtr is pair (obj,nil)
    // where obj is a TypeStruct, possibly TypeStruct.BOT/TOP
    // where nil is an explicit null is allowed or not

    // Examples:
    // (Person,false) - a not-nil Person
    // (Person,true ) - a Person or a nil
    // (BOT   ,false) - a not-nil void* (unspecified struct)
    // (TOP   ,true ) - a nil

    public final SONTypeStruct _obj;

    public SONTypeMemPtr(byte nil, SONTypeStruct obj) {
        super(TMEMPTR,nil);
        assert obj!=null;
        _obj = obj;
    }
    static SONTypeMemPtr make(byte nil, SONTypeStruct obj) { return new SONTypeMemPtr(nil, obj).intern(); }
    public static SONTypeMemPtr makeNullable(SONTypeStruct obj) { return make((byte)3, obj); }
    public static SONTypeMemPtr make(SONTypeStruct obj) { return new SONTypeMemPtr((byte)2, obj).intern(); }
    public SONTypeMemPtr makeFrom(SONTypeStruct obj) { return obj==_obj ? this : make(_nil, obj); }
    public SONTypeMemPtr makeNullable() { return makeFrom((byte)3); }
    @Override
    SONTypeMemPtr makeFrom(byte nil) { return nil==_nil ? this : make(nil,_obj); }
    @Override public SONTypeMemPtr makeRO() { return makeFrom(_obj.makeRO()); }
    @Override public boolean isFinal() { return _obj.isFinal(); }

    // An abstract pointer, pointing to either a Struct or an Array.
    // Can also be null or not, so 4 choices {TOP,BOT} x {nil,not}
    public static SONTypeMemPtr BOT = make((byte)3, SONTypeStruct.BOT);
    public static SONTypeMemPtr TOP = BOT.dual();
    public static SONTypeMemPtr NOTBOT = make((byte)2, SONTypeStruct.BOT);

    public static SONTypeMemPtr TEST= make((byte)2, SONTypeStruct.TEST);
    public static void gather(ArrayList<SONType> ts) { ts.add(NOTBOT); ts.add(BOT); ts.add(TEST); }

    @Override
    public SONTypeNil xmeet(SONType t) {
        SONTypeMemPtr that = (SONTypeMemPtr) t;
        return SONTypeMemPtr.make(xmeet0(that), (SONTypeStruct)_obj.meet(that._obj));
    }

    @Override
    public SONTypeMemPtr dual() { return SONTypeMemPtr.make( dual0(), _obj.dual()); }

    // RHS is NIL; do not deep-dual when crossing the centerline
    @Override
    SONType meet0() { return _nil==3 ? this : make((byte)3,_obj); }


    // True if this "isa" t up to named structures
    @Override public boolean shallowISA( SONType t ) {
        if( !(t instanceof SONTypeMemPtr that) ) return false;
        if( this==that ) return true;
        if( xmeet0(that)!=that._nil ) return false;
        if( _obj==that._obj ) return true;
        if( _obj._name.equals(that._obj._name) )
            return true;        // Shallow, do not follow matching names, just assume ok
        throw Utils.TODO(); // return _obj.shallowISA(that._obj);
    }

    @Override public SONTypeMemPtr glb() { return make((byte)3,_obj.glb()); }
    // Is forward-reference
    @Override public boolean isFRef() { return _obj.isFRef(); }

    @Override public int log_size() { return 2; } // (1<<2)==4-byte pointers

    @Override int hash() { return _obj.hashCode() ^ super.hash(); }

    @Override boolean eq(SONType t) {
        SONTypeMemPtr ptr = (SONTypeMemPtr)t; // Invariant
        return _obj == ptr._obj  && super.eq(ptr);
    }

    @Override public String str() {
        if( this== NOTBOT) return "*void";
        if( this==    BOT) return "*void?";
        return x()+"*"+_obj.str()+q();
    }

    @Override public SB print(SB sb) {
        if( this== NOTBOT) return sb.p("*void");
        if( this==    BOT) return sb.p("*void?");
        return _obj.print(sb.p(x()).p("*")).p(q());
    }
    @Override public SB gprint(SB sb) {
        if( this== NOTBOT) return sb.p("*void");
        if( this==    BOT) return sb.p("*void?");
        return _obj.gprint(sb.p(x()).p("*")).p(q());
    }
}
