package com.compilerprogramming.ezlang.compiler.node.cpus.arm;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.node.*;

// compare instruction on float regs input(D-registers)
public class CmpFARM extends MachConcreteNode implements MachNode{
    CmpFARM(Node cmp) {super(cmp);}
    @Override public String op() { return "cmpf"; }
    @Override public RegMask regmap(int i) { return arm.DMASK; }
    @Override public RegMask outregmap() { return arm.FLAGS_MASK; }

    // FCMP
    @Override public void encoding( Encoding enc ) { arm.f_cmp(enc,this); }
    // General form: "cmp  d0, d1"
    @Override public void asm(CodeGen code, SB sb) {
        String dst = code.reg(this);
        if( dst!="flags" )  sb.p(dst).p(" = ");
        sb.p(code.reg(in(1))).p(", ").p(code.reg(in(2)));
    }
}
