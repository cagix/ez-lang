package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.*;
import com.compilerprogramming.ezlang.compiler.nodes.MachConcreteNode;

public class NegARM extends MachConcreteNode implements MachNode {
    NegARM(Node sub) {
        super(sub);
        //_inputs.pop();
    }
    @Override public String op() { return "neg"; }
    @Override public RegMask regmap(int i) { return arm.RMASK; }
    @Override public RegMask outregmap()   { return arm.WMASK; }
    @Override public void encoding( Encoding enc ) {
        // reverse subtract with immediate 0
        arm.imm_inst(enc,this, in(1), 0b00100110, 0);
    }
    // General form: "subi  rd = rs1 - imm"
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(" = -").p(code.reg(in(1)));
    }
}
