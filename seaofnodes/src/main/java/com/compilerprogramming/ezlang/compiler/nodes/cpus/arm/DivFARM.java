package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.*;

public class DivFARM extends MachConcreteNode implements MachNode {
    DivFARM( Node divf) { super(divf); }
    @Override public String op() { return "divf"; }
    @Override public RegMask regmap(int i) { return arm.DMASK; }
    @Override public RegMask outregmap() {  return arm.DMASK; }

    // FDIV (scalar)
    @Override public void encoding( Encoding enc ) { arm.f_scalar(enc,this,0b000110); }
    // General form: "VDIF =  dst /= src"
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(" = ").p(code.reg(in(1))).p(" / ").p(code.reg(in(2)));
    }
}
