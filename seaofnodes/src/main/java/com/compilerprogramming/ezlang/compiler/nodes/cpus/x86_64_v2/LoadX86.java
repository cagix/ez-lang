package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.LoadNode;
import com.compilerprogramming.ezlang.compiler.nodes.Node;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

public class LoadX86 extends MemOpX86 {
    LoadX86( LoadNode ld, Node base, Node idx, int off, int scale ) {
        super(ld,ld, base, idx, off, scale, 0);
    }
    @Override public String op() { return "ld"+_sz; }
    @Override public RegMask outregmap() { return x86_64_v2.MEM_MASK; }
    @Override public void encoding( Encoding enc ) {
        // REX.W + 8B /r	MOV r64, r/m64
        // Zero extension for u8, u16 and u32 but sign extension i8, i16, i32
        // Use movsx and movzx

        short dst = enc.reg(this );
        short ptr = enc.reg(ptr());
        short idx = enc.reg(idx());

        if (_declaredType != SONTypeInteger.U32 && _declaredType != SONTypeFloat.F32 && _declaredType != SONTypeFloat.F64) {
            enc.add1(x86_64_v2.rex(dst, ptr, idx));
        }

        if(_declaredType == SONTypeFloat.F32) {
            // F3 0F 10 /r MOVSS xmm1, m32
            enc.add1(0xF3);
            enc.add1(0x0F);
            enc.add1(0x10);
            dst -= (short)x86_64_v2.XMM_OFFSET;
        }
        if(_declaredType == SONTypeFloat.F64) {
            //  F2 0F 10 /r MOVSD xmm1, m64
            enc.add1(0xF2);
            enc.add1(0x0F);
            enc.add1(0x10);
            dst -= (short)x86_64_v2.XMM_OFFSET;
        }
        if(_declaredType == SONTypeInteger.I8) {
            // sign extend: REX.W + 0F BE /r	MOVSX r64, r/m8
            enc.add1(0x0F);
            enc.add1(0xBE);
        } else if(_declaredType == SONTypeInteger.I16) {
            // sign extend: REX.W + 0F BF /r	MOVSX r64, r/m16
            enc.add1(0x0F);
            enc.add1(0xBF);
        } else if(_declaredType == SONTypeInteger.I32) {
            // sign extend: REX.W + 63 /r	MOVSXD r64, r/m32
            enc.add1(0x63);
        } else if(_declaredType == SONTypeInteger.U8) {
            // zero extend: REX.W + 0F B6 /r	MOVZX r64, r/m8
            enc.add1(0x0F);
            enc.add1(0xB6);
        } else if(_declaredType == SONTypeInteger.U16) {
            // zero extend:   REX.W + 0F B7 /r	MOVZX r64, r/m16
            enc.add1(0x0F);
            enc.add1(0xB7);
        } else if(_declaredType == SONTypeInteger.U32) {
            // zero extend:   8B /r	MOV r32, r/m32
            enc.add1(0x8B);
        } else if(_declaredType == SONTypeInteger.BOT) {
            // REX.W + 8B /r    MOV r64, r/m64
            enc.add1(0x8B);
        }

        // includes modrm internally
        x86_64_v2.indirectAdr(_scale, idx, ptr, _off, dst, enc);
    }

    // General form: "ldN  dst,[base + idx<<2 + 12]"
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(",");
        asm_address(code,sb);
    }
}
