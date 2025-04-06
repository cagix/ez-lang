package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.nodes.MachConcreteNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class LslIARM extends MachConcreteNode implements MachNode {
    final int _imm;
    LslIARM(Node lsl, int imm) {
        super(lsl);
        _inputs.pop();
        _imm = imm;
    }
    @Override public String op() { return "lsli"; }
    @Override public RegMask regmap(int i) { return arm.RMASK; }
    @Override public RegMask outregmap() { return arm.RMASK; }
    @Override public void encoding( Encoding enc ) {
        short rd = enc.reg(this);
        short rn = enc.reg(in(1));
        assert _imm > 0;
        // UBFM <Xd>, <Xn>, #(-<shift> MOD 64), #(63-<shift>)
        // immr must be (-<shift> MOD 64) = 64 - shift
        enc.add4(arm.imm_shift(arm.OPI_LSL, 64 - _imm, (64 - _imm) - 1, rn, rd));
    }
    // General form: "lsli  rd = rs1 << imm"
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(" = ").p(code.reg(in(1))).p(" << #").p(_imm);
    }
}