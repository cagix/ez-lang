package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.LoadNode;
import com.compilerprogramming.ezlang.compiler.nodes.Node;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFloat;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeMemPtr;

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
        enc(enc, _declaredType, dst, ptr, idx, _off, _scale);
    }

    static void enc( Encoding enc, SONType decl, short dst, short ptr, short idx, int off, int scale ) {
        if( decl == SONTypeFloat.F32) enc.add1(0xF3);
        if( decl == SONTypeFloat.F64) enc.add1(0xF2);

        if( decl.isa(SONTypeFloat.F64) )
            dst -= (short)x86_64_v2.XMM_OFFSET;

        x86_64_v2.rexF(dst, ptr, idx, decl != SONTypeInteger.U32 && decl != SONTypeFloat.F32 && decl != SONTypeFloat.F64, enc);

        if( false ) ;
        else if( decl == SONTypeFloat.F32   ) enc.add1(0x0F).add1(0x10); // F3 0F 10 /r MOVSS xmm1, m32
        else if( decl == SONTypeFloat.F64   ) enc.add1(0x0F).add1(0x10); // F2 0F 10 /r MOVSD xmm1, m64
        else if( decl.isa(SONTypeInteger.I8)) enc.add1(0x0F).add1(0xBE); // sign extend: REX.W + 0F BE /r MOVSX r64, r/m8
        else if( decl == SONTypeInteger.I16 ) enc.add1(0x0F).add1(0xBF); // sign extend: REX.W + 0F BF /r MOVSX r64, r/m16
        else if( decl == SONTypeInteger.I32 ) enc.add1(0x63);            // sign extend: REX.W + 63 /r    MOVSXD r64, r/m32
        else if( decl == SONTypeInteger.U8  ) enc.add1(0x0F).add1(0xB6); // zero extend: REX.W + 0F B6 /r MOVZX r64, r/m8
        else if( decl == SONTypeInteger.U16 ) enc.add1(0x0F).add1(0xB7); // zero extend: REX.W + 0F B7 /r MOVZX r64, r/m16
        // Covers U32, I64/BOT, TMP
        else if( decl.log_size()>=2 )     enc.add1(0x8B);            // zero extend:         8B /r    MOV r32, r/m32
        else throw Utils.TODO();

        // includes modrm internally
        x86_64_v2.indirectAdr(scale, idx, ptr, off, dst, enc);
    }


    // General form: "ldN  dst,[base + idx<<2 + 12]"
    @Override public void asm(CodeGen code, SB sb) {
        sb.p(code.reg(this)).p(",");
        asm_address(code,sb);
    }
}
