package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.ConstantNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;

// Add upper 20bits to PC.  Immediate comes from the relocation info.
public class AUIPC extends ConstantNode implements MachNode, RIPRelSize {
    AUIPC( SONType tfp ) { super(tfp); }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return riscv.WMASK; }
    @Override public boolean isClone() { return true; }
    @Override public AUIPC copy() { return new AUIPC(_con); }
    @Override public String op() { return "auipc"; }
    @Override public void encoding( Encoding enc ) {
        enc.relo(this);
        short dst = enc.reg(this);
        enc.add4(riscv.u_type(riscv.OP_AUIPC, dst, 0));
    }

    // Delta is from opcode start, but X86 measures from the end of the 5-byte encoding
    @Override public byte encSize(int delta) { return 4; }

    // Delta is from opcode start
    @Override public void patch( Encoding enc, int opStart, int opLen, int delta ) {
        //short rpc = enc.reg(this);
        //// High half is where the TFP constant used to be, the last input
        //short auipc = enc.reg(in(_inputs._len-1));
        //enc.patch4(opStart,riscv.i_type(0x67, rpc, 0, auipc, delta));
        throw Utils.TODO();
    }

    @Override public void asm(CodeGen code, SB sb) {
        String reg = code.reg(this);
        sb.p(reg).p(" = PC+#");
        if( _con == null ) sb.p("---");
        else _con.print(sb);
    }
}
