package com.compilerprogramming.ezlang.compiler.node.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.util.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.node.*;

public class NegX86 extends MachConcreteNode implements MachNode {
    NegX86(MinusNode not) { super(not); }
    @Override public String op() { return "neg"; }
    @Override public RegMask regmap(int i) { return x86_64_v2.RMASK; }
    @Override public RegMask outregmap()   { return x86_64_v2.RMASK; }
    @Override public RegMask killmap()     { return x86_64_v2.FLAGS_MASK; }
    @Override public int twoAddress() { return 1; }

    @Override public void encoding( Encoding enc ) {
        short dst = enc.reg(this );
        enc.add1(x86_64_v2.rex(0, dst, 0));
        enc.add1(0xF7); // opcode
        enc.add1(x86_64_v2.modrm(x86_64_v2.MOD.DIRECT, 3, dst));
    }
    @Override public void asm(CodeGen code, SB sb) { sb.p(code.reg(this)); }
}
