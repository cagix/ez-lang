package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.ConstantNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;

//FMOV (scalar, immediate)
//Floating-point move immediate.
public class FloatARM extends ConstantNode implements MachNode, RIPRelSize {
    FloatARM(ConstantNode con) { super(con); }
    @Override public String op() { return "ld8"; }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return arm.DMASK; }
    @Override public boolean isClone() { return true; }
    @Override public FloatARM copy() { return new FloatARM(this); }

    @Override public void encoding( Encoding enc ) {
        enc.largeConstant(this,_con,0,-1/*TODO: ARM-style ELF patching*/);
        short dst = (short)(enc.reg(this) - arm.D_OFFSET);
        double d = ((SONTypeFloat)_con).value();
        long x = Double.doubleToRawLongBits(d);
        enc.add4(arm.load_pc(arm.OPF_ARM, 0, dst));
    }

    // Delta is from opcode start.
    @Override public byte encSize(int delta) { return 4;  }

    // Delta is from opcode start
    @Override public void patch( Encoding enc, int opStart, int opLen, int delta ) {
        short dst = (short)(enc.reg(this) - arm.D_OFFSET);
        enc.patch4(opStart, arm.load_pc(arm.OPF_ARM, delta, dst));
    }

    // Human-readable form appended to the SB.  Things like the encoding,
    // indentation, leading address or block labels not printed here.
    // Just something like "ld4\tR17=[R18+12] // Load array base".
    // General form: "op\tdst=src+src"
    @Override public void asm(CodeGen code, SB sb) {
        _con.print(sb.p(code.reg(this)).p(" = #"));
    }

}
