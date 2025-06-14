package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.CastNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;

public class CastX86 extends CastNode implements MachNode {
    CastX86( CastNode cast ) { super(cast); }
    @Override public String op() { return label(); }
    @Override public RegMask regmap(int i) { assert i==1; return RegMask.FULL; }
    @Override public RegMask outregmap() { return RegMask.FULL; }
    @Override public int twoAddress( ) { return 1; }
    @Override public void encoding( Encoding enc ) { }
    @Override public void asm(CodeGen code, SB sb) { _t.print(sb.p(code.reg(in(1))).p(" isa ")); }
}
