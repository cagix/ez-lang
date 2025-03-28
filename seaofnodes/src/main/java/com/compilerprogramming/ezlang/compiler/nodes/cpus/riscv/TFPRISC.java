package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.ConstantNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFunPtr;

public class TFPRISC extends ConstantNode implements MachNode, RIPRelSize {
    TFPRISC(ConstantNode con) { super(con); }
    @Override public String op() { return "ldx"; }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return riscv.WMASK; }
    @Override public boolean isClone() { return true; }
    @Override public TFPRISC copy() { return new TFPRISC(this); }
    @Override public void encoding( Encoding enc ) {
        enc.relo(this);
        // TODO: 1 op encoding, plus a TODO if it does not fit
        short dst = enc.reg(this);
        SONTypeFunPtr tfp = (SONTypeFunPtr)_con;
        // auipc  t0,0
        int auipc = riscv.u_type(0b0010111, dst, 0);
        // addi   t1,t0 + #0
        int addi = riscv.i_type(0b0010011, dst, 0, dst, 0);
        enc.add4(auipc);
        enc.add4(addi);
    }

    @Override public byte encSize(int delta) {
        if( -(1L<<11) <= delta && delta < (1L<<11) ) return 4;
        throw Utils.TODO();
    }

    // Delta is from opcode start
    @Override public void patch( Encoding enc, int opStart, int opLen, int delta ) {
        short rpc = enc.reg(this);
        if( opLen==4 ) {
            // AUIPC (upper 20 bits)
            // opstart of add
            int next = opStart + opLen;
            enc.patch4(opStart,riscv.u_type(0b0010111, rpc, delta));
            // addi(low 12 bits)
            enc.patch4(next,riscv.i_type(0b0010011, rpc, 0, rpc, delta & 0xFFF));
            // addi
        } else {
            throw Utils.TODO();
        }
    }

    @Override public void asm(CodeGen code, SB sb) {
        _con.print(sb.p(code.reg(this)).p(" #"));
    }

}
