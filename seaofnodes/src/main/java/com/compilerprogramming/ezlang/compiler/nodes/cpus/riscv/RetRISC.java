package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.nodes.FunNode;
import com.compilerprogramming.ezlang.compiler.nodes.ReturnNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;

public class RetRISC extends ReturnNode implements MachNode {
    public RetRISC(ReturnNode ret, FunNode fun) { super(ret, fun); fun.setRet(this); }
    @Override public void encoding( Encoding enc ) {
        int sz = fun()._frameAdjust;
        if( sz >= 1L<<12 ) throw Utils.TODO();
        if( sz != 0 )
            enc.add4(riscv.i_type(riscv.OP_IMM, riscv.RSP, 0, riscv.RSP, sz & 0xFFF));
        short rpc = enc.reg(rpc());
        enc.add4(riscv.i_type(riscv.OP_JALR, riscv.ZERO, 0, rpc, 0));
    }
}
