package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.*;

// corresponds to slt (and NOT sltu, slti, sltiu)
public class SetRISC extends MachConcreteNode implements MachNode {
    SetRISC( Node cmp ) { super(cmp); }
    @Override public String op() { return "slt"; }
    @Override public RegMask regmap(int i) { return riscv.RMASK; }
    @Override public RegMask outregmap() { return riscv.WMASK; }
    @Override public void encoding( Encoding enc ) { riscv.r_type(enc,this,2,0); }
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(" = ").p(code.reg(in(1))).p(" < ").p(code.reg(in(2)));
    }
}
