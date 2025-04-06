package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.ConstantNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

// Load upper 20bits.
public class LUI extends ConstantNode implements MachNode {
    public LUI( int imm20 ) {
        super(SONTypeInteger.constant(imm20));
        assert riscv.imm20Exact((SONTypeInteger)_con);
    }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return riscv.WMASK; }
    @Override public boolean isClone() { return true; }
    @Override public LUI copy() { return new LUI((int)((SONTypeInteger)_con).value()); }
    @Override public String op() { return "lui"; }
    @Override public void encoding( Encoding enc ) {
        long x = ((SONTypeInteger)_con).value();
        int imm20 = (int)(x>>12) & 0xFFFFF;
        short dst = enc.reg(this);
        int lui = riscv.u_type(riscv.OP_LUI, dst, imm20);
        enc.add4(lui);
    }

    @Override public void asm(CodeGen code, SB sb) {
        String reg = code.reg(this);
        sb.p(reg).p(" = #").hex4((int)(((SONTypeInteger)_con).value()));
    }
}
