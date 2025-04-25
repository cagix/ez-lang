package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.MachConcreteNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.nodes.NotNode;

public class NotRISC extends MachConcreteNode implements MachNode {
    public NotRISC(NotNode not) { super(not); }
    @Override public String op() { return "not"; }
    @Override public RegMask regmap(int i) { return riscv.RMASK; }
    @Override public RegMask outregmap()   { return riscv.WMASK; }
    @Override public void encoding( Encoding enc ) {
        short dst = enc.reg(this );
        short src = enc.reg(in(1));
        enc.add4(riscv.i_type(riscv.OP_IMM, dst, 2, src, 1));
    }
    @Override public void asm(CodeGen code, SB sb) { sb.p(code.reg(this)); }
}
