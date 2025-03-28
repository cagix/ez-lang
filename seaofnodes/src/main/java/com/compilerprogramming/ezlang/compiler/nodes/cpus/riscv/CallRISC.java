package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.*;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFunPtr;

public class CallRISC extends CallNode implements MachNode, RIPRelSize {
    final SONTypeFunPtr _tfp;
    final String _name;
    CallRISC( CallNode call, SONTypeFunPtr tfp ) {
        super(call);
        assert tfp.isConstant();
        _inputs.pop(); // Pop constant target
        _tfp = tfp;
        _name = CodeGen.CODE.link(tfp)._name;
    }

    @Override public String op() { return "call"; }
    @Override public String label() { return op(); }
    @Override public RegMask regmap(int i) {
        // Last call input is AUIPC
        if( i == nIns()-1 ) return riscv.RMASK;
        return riscv.callInMask(_tfp,i);
    }
    @Override public RegMask outregmap() { return riscv.RPC_MASK; }
    @Override public String name() { return _name; }
    @Override public SONTypeFunPtr tfp() { return _tfp; }

    @Override public void encoding( Encoding enc ) {
        // Short form +/-4K:  beq r0,r0,imm12
        // Long form:  auipc rX,imm20/32; jal r0,[rX+imm12/32]
        enc.relo(this);
        short rpc = enc.reg(this);
        enc.add4(riscv.j_type(riscv.J_JAL, rpc, 0));
    }

    // Delta is from opcode start
    @Override public byte encSize(int delta) {
        if( -(1L<<20) <= delta && delta < (1L<<20) ) return 4;
        // 2 word encoding needs a tmp register, must teach RA
        throw Utils.TODO();
    }

    // Delta is from opcode start
    @Override public void patch( Encoding enc, int opStart, int opLen, int delta ) {
        short rpc = enc.reg(this);
        if( opLen==4 ) {
            enc.patch4(opStart,riscv.j_type(riscv.J_JAL, rpc, delta));
        } else {
            throw Utils.TODO();
        }
    }

    @Override public void asm(CodeGen code, SB sb) {
        sb.p(_name).p("  ");
        for( int i=0; i<nargs(); i++ )
            sb.p(code.reg(arg(i+2))).p(", ");
        sb.unchar(2).p("  ").p(code.reg(fptr()));

    }
}
