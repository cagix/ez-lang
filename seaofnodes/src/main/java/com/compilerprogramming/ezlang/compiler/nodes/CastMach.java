package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;

/**
 * Cast a pointer to read-only
 */
public class CastMach extends CastNode implements MachNode {
    public CastMach( CastNode n ) { super(n); }
    @Override public String op() { return label(); }
    @Override public RegMask regmap(int i) { assert i==1; return RegMask.FULL; }
    @Override public RegMask outregmap() { return RegMask.FULL; }
    @Override public int twoAddress( ) { return 1; }
    @Override public void encoding( Encoding enc ) { }
    @Override public void asm(CodeGen code, SB sb) { sb.p(code.reg(in(1))); }
}
