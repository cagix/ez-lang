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

    public final String _name;
    public final int _alias;
    // Declared type; not final because it might be a forward-reference
    // which will be lazily improved when the reference is declared.
    public SONType _declaredType;

    public MemOpNode(String name, int alias, SONType glb, Node mem, Node ptr, Node off) {
        super(null, mem, ptr, off);
        _name  = name;
        _alias = alias;
        _declaredType = glb;
    }
    public MemOpNode(String name, int alias, SONType glb, Node mem, Node ptr, Node off, Node value) {
        this(name, alias, glb, mem, ptr, off);
        addDef(value);
    }
    public MemOpNode( Node mach, MemOpNode mop ) {
        super(mach);
        _name  = mop==null ? null : mop._name;
        _alias = mop==null ? 0    : mop._alias;
        _declaredType = mop==null ? SONType.BOTTOM : mop._declaredType;
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
    boolean eq(Node n) {
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
