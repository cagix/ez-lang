package com.compilerprogramming.ezlang.compiler.sontypes;

import com.compilerprogramming.ezlang.compiler.SB;

import java.util.ArrayList;

/**
 * Represents a field in a struct. This is not a Type in the type system.
 */
public class Field extends SONType {

    // The pair {fieldName,type} uniquely identifies a field.

    // Field name
    public final String _fname;
    // Type of the field
    public final SONType _type;
    // Unique memory alias, not sensibly part of a "type" but very convenient here.
    public final int _alias;
    // Field must be written to exactly once, no more, no less
    public final boolean _final;

    public Field(String fname, SONType type, int alias, boolean xfinal ) {
        super(TFLD);
        _fname = fname;
        _type  = type;
        _alias = alias;
        _final = xfinal;
    }
    // Make with existing alias
    public static Field make(String fname, SONType type, int alias, boolean xfinal ) {
        return new Field(fname,type,alias,xfinal).intern();
    }
    public Field makeFrom( SONType type ) {
        return type == _type ? this : new Field(_fname,type,_alias,_final).intern();
    }
    @Override public Field makeRO() { return _final ? this : make(_fname,_type.makeRO(),_alias,true);  }
    @Override public boolean isFinal() { return _final && _type.isFinal(); }

    public static final Field TEST = make("test", SONType.NIL,-2,false);
    public static final Field TEST2= make("test", SONType.NIL,-2,true);
    public static void gather(ArrayList<SONType> ts) { ts.add(TEST); ts.add(TEST2); }

    @Override Field xmeet( SONType that ) {
        Field fld = (Field)that; // Invariant
        assert _fname.equals(fld._fname);
        assert _alias==fld._alias;
        return make(_fname,_type.meet(fld._type),_alias,_final | fld._final);
    }

    @Override
    public Field dual() { return make(_fname,_type.dual(),_alias,!_final); }

    @Override public Field glb() {
        SONType glb = _type.glb();
        return (glb==_type && _final) ? this : make(_fname,glb,_alias,true);
    }

    // Override in subclasses
    int hash() { return _fname.hashCode() ^ _type.hashCode() ^ _alias ^ (_final ? 1024 : 0); }

    boolean eq(SONType t) {
        Field f = (Field)t;
        return _fname.equals(f._fname) && _type==f._type && _alias==f._alias && _final==f._final;
    }


    @Override
    public SB print( SB sb ) {
        return _type.print(sb.p(_final?"":"!").p(_fname).p(":").p(_alias).p(" : "));
    }

    @Override public String str() { return _fname; }
}
