package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.ConstantNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

// 12-bit integer constant.  Larger constants are made up in the instruction
// selection by adding with a LUI.
public class IntRISC extends ConstantNode implements MachNode {
    public IntRISC(ConstantNode con) { super(con); }
    @Override public String op() { return "ldi"; }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return riscv.WMASK; }
    @Override public boolean isClone() { return true; }
    @Override public IntRISC copy() { return new IntRISC(this); }
    @Override public void encoding( Encoding enc ) {
        short dst  = enc.reg(this);
        SONTypeInteger ti = (SONTypeInteger)_con;
        // Explicit truncation of larger immediates; this will sign-extend on
        // load and this is handled during instruction selection.
        enc.add4(riscv.i_type(riscv.OP_IMM, dst, 0, riscv.ZERO, (int)(ti.value() & 0xFFF)));
    }
    @Override public void asm(CodeGen code, SB sb) {
        String reg = code.reg(this);
        _con.print(sb.p(reg).p(" = #"));
    }
}
