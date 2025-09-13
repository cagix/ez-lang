package com.compilerprogramming.ezlang.compiler.node.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.node.FunNode;
import com.compilerprogramming.ezlang.compiler.node.MachNode;

public class FunRISC extends FunNode implements MachNode {
    FunRISC(FunNode fun) {super(fun);}
    @Override public void computeFrameAdjust(CodeGen code, int maxReg) {
        super.computeFrameAdjust(code,maxReg);
        if( _hasCalls )         // If non-leaf, pad to 16b
            _frameAdjust = ((_frameAdjust+8) & -16);
    }
    @Override public void encoding( Encoding enc ) {
        int sz = _frameAdjust;
        if( sz == 0 ) return; // Skip if no frame adjust
        if( sz >= 1L<<12 ) throw Utils.TODO();
        enc.add4(riscv.i_type(riscv.OP_IMM, riscv.RSP, 0, riscv.RSP, -sz & 0xFFF));
    }
}
