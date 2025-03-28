package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.LoadNode;
import com.compilerprogramming.ezlang.compiler.nodes.Node;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;

// Load memory addressing on ARM
// Support imm, reg(direct), or reg+off(indirect) addressing
// Base = base - base pointer, offset is added to base
// idx  = null
// off  = off - offset added to base

public class LoadARM extends MemOpARM{
    LoadARM(LoadNode ld,Node base, Node idx, int off) {
        super(ld, base, idx, off, 0);
    }
    @Override public String op() { return "ld"+_sz; }
    @Override public RegMask outregmap() { return arm.MEM_MASK; }
    // ldr(immediate - unsigned offset) | ldr(register)
    @Override public void encoding( Encoding enc ) {
        if(_declaredType == SONTypeFloat.F32 || _declaredType == SONTypeFloat.F64) {
            ldst_encode(enc, 0b1111110101, 0b11111100011, this, true);
        } else {
            ldst_encode(enc, 0b1111100101, 0b11111000011, this, false);
        }
    }
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(",");
        asm_address(code,sb);
    }
}
