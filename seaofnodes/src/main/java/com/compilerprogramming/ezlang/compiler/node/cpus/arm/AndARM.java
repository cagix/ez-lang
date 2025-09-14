package com.compilerprogramming.ezlang.compiler.node.cpus.arm;

import com.compilerprogramming.ezlang.compiler.util.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.node.MachConcreteNode;
import com.compilerprogramming.ezlang.compiler.node.MachNode;
import com.compilerprogramming.ezlang.compiler.node.Node;

public class AndARM extends MachConcreteNode implements MachNode {
    AndARM(Node and) { super(and); }
    @Override public String op() { return "and"; }
    @Override public String glabel() { return "&"; }
    @Override public RegMask regmap(int i) { return arm.RMASK; }
    @Override public RegMask outregmap() { return arm.WMASK; }
    // AND (shifted register)
    @Override public void encoding( Encoding enc ) { arm.r_reg(enc,this,arm.OP_AND); }
    // General form:  #rd = rs1 & rs2
    @Override public void asm(CodeGen code, SB sb){
        sb.p(code.reg(this)).p(" = ").p(code.reg(in(1))).p(" & ").p(code.reg(in(2)));
    }
}
