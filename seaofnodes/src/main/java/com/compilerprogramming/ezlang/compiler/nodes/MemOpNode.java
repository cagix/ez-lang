package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeMemPtr;
import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.lang.StringBuilder;
import java.util.BitSet;

/**
 * Convenience common base for Load and Store.
 *
 * 0 - Control
 * 1 - Memory
 * 2 - Base of object, OOP
 * 3 - Offset, integer types
 * 4 - Value, for Stores only
 */
public abstract class MemOpNode extends Node {

    // The equivalence alias class
    public final int _alias;

    // True if load-like, false if store-like.
    //
    // Stores produce memory (maybe as part of a tuple with other things),
    // loads do not.
    //
    // Loads might pick up anti-dependences on prior Stores, and never cause an
    // anti-dependence themselves.
    //
    // Stores must maximally sink to the least dominator use.  Loads can be
    // opportunistically hoisted.
    public final boolean _isLoad;

    // Declared type; not final because it might be a forward-reference
    // which will be lazily improved when the reference is declared.
    public SONType _declaredType;

    // A debug name, no semantic meaning
    public final String _name;
    public MemOpNode(String name, int alias, boolean isLoad, SONType glb, Node mem, Node ptr, Node off) {
        super(null, mem, ptr, off);
        _name  = name;
        _alias = alias;
        _declaredType = glb;
        _isLoad = isLoad;
    }
    public MemOpNode(String name, int alias, boolean isLoad, SONType glb, Node mem, Node ptr, Node off, Node value) {
        this(name, alias, isLoad, glb, mem, ptr, off);
        addDef(value);
    }
    public MemOpNode( Node mach, MemOpNode mop ) {
        super(mach);
        _name  = mop==null ? null : mop._name;
        _alias = mop==null ? 0    : mop._alias;
        _isLoad= mop==null ? true : mop._isLoad;
        _declaredType = mop==null ? SONType.BOTTOM : mop._declaredType;
        if( mop==null )
            throw Utils.TODO("Load or not");
    }

    // Used by M2 when translating graph to Simple
    public MemOpNode( boolean isLoad ) {
        super((Node)null);
        _name         = null;
        _alias        = 0;

        _isLoad       = isLoad;
        _declaredType = SONType.BOTTOM;
    }

    //
    static String mlabel(String name) { return "[]".equals(name) ? "ary" : ("#".equals(name) ? "len" : name); }
    String mlabel() { return mlabel(_name); }

    public Node mem() { return in(1); }
    public Node ptr() { return in(2); }
    public Node off() { return in(3); }

    @Override public StringBuilder _print1( StringBuilder sb, BitSet visited ) { return _printMach(sb,visited);  }
    public StringBuilder _printMach( StringBuilder sb, BitSet visited ) { throw Utils.TODO(); }


    @Override
    public boolean eq(Node n) {
        MemOpNode mem = (MemOpNode)n; // Invariant
        return _alias==mem._alias;    // When comparing types error to use "equals"; always use "=="
    }

    @Override
    int hash() { return _alias; }

    @Override
    public CompilerException err() {
        SONType ptr = ptr()._type;
        // Already an error, but better error messages come from elsewhere
        if( ptr == SONType.BOTTOM ) return null;
        if( ptr.isHigh() ) return null; // Assume it will fall to not-null
        // Better be a not-nil TMP
        if( ptr instanceof SONTypeMemPtr tmp && tmp.notNull() )
            return null;
        return Compiler.error( "Might be null accessing '" + _name + "'");
    }
}
