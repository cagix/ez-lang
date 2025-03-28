package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.compiler.sontypes.*;


/**
 *  The tracked fields are now complex enough to deserve a array-of-structs layout
 */
public class Var {

    public final String _name;   // Declared name
    public int _idx;             // index in containing scope
    private SONType _type;          // Declared type
    public boolean _final;       // Final field
    public boolean _fref;        // Forward ref

    public Var(int idx, String name, SONType type, boolean xfinal) {
        this(idx,name,type,xfinal,false);
    }
    public Var(int idx, String name, SONType type, boolean xfinal, boolean fref) {
        _idx = idx;
        _name = name;
        _type = type;
        _final = xfinal;
        _fref = fref;
    }
    public SONType type() {
        if( !_type.isFRef() ) return _type;
        // Update self to no longer use the forward ref type
        SONType def = Compiler.TYPES.get(((SONTypeMemPtr)_type)._obj._name);
        return (_type=_type.meet(def));
    }
    public SONType lazyGLB() {
        SONType t = type();
        return t instanceof SONTypeMemPtr ? t : t.glb();
    }

    // Forward reference variables (not types) must be BOTTOM and
    // distinct from inferred variables
    public boolean isFRef() { return _fref; }

    public void defFRef(SONType type, boolean xfinal) {
        assert isFRef() && xfinal;
        _type = type;
        _final = true;
        _fref = false;
    }

    @Override public String toString() {
        return _type.toString()+(_final ? " ": " !")+_name;
    }
}
