package com.compilerprogramming.ezlang.compiler.node.cpus.arm;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.node.FunNode;
import com.compilerprogramming.ezlang.compiler.node.MachNode;

public class FunARM  extends FunNode implements MachNode {
    FunARM(FunNode fun) { super(fun); }
    @Override public void computeFrameAdjust(CodeGen code, int maxReg) {
        super.computeFrameAdjust(code,maxReg);
        if( _hasCalls )         // If non-leaf, pad to 16b
            _frameAdjust = ((_frameAdjust+8) & -16);
    }
    @Override public void encoding( Encoding enc ) {
        int sz = _frameAdjust;
        if( sz == 0 ) return;   // Skip if no frame adjust
        if( sz >= 1L<<12 ) throw Utils.TODO();
        enc.add4(arm.imm_inst(arm.OPI_ADD, -sz&0xFFF, arm.RSP, arm.RSP));
    }
}
