package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.*;
import com.compilerprogramming.ezlang.compiler.sontypes.*;
import java.util.BitSet;

public abstract class MemOpRISC extends MemOpNode implements MachNode {
    final int _off;             // Limit 12 bits
    final char _sz = (char)('0'+(1<<_declaredType.log_size()));
    MemOpRISC(MemOpNode mop, Node base, int off, Node val) {
        super(mop,mop);
        assert base._type instanceof SONTypeMemPtr;
        _inputs.setX(2, base ); // Base can be an Add, is no longer raw object base
        _inputs.setX(3, null);  // Never an index
        _inputs.setX(4, val );
        _off = off;
    }

    @Override public String label() { return op(); }
    Node val() { return in(4); } // Only for stores

    @Override public StringBuilder _printMach(StringBuilder sb, BitSet visited) { return sb.append(".").append(_name); }

    @Override public SONType compute() { throw Utils.TODO(); }
    @Override public Node idealize() { throw Utils.TODO(); }

    // func3 is based on load/store size and extend
    int func3() {
        int func3 = -1;
        if( _declaredType == SONTypeInteger. I8 ) func3=0; // LB   SB
        if( _declaredType == SONTypeInteger.I16 ) func3=1; // LH   SH
        if( _declaredType == SONTypeInteger.I32 ) func3=2; // LW   SW
        if( _declaredType == SONTypeInteger.BOT ) func3=3; // LD   SD
        if( _declaredType == SONTypeInteger. U8 ) func3=4; // LBU
        if( _declaredType == SONTypeInteger.BOOL) func3=4; // LBU
        if( _declaredType == SONTypeInteger.U16 ) func3=5; // LHU
        if( _declaredType == SONTypeInteger.U32 ) func3=6; // LWU
        // float
        if(_declaredType == SONTypeFloat.F32) func3 = 2; // fLW   fSW
        if(_declaredType == SONTypeFloat.F64) func3 = 3; // fLD   fSD
        if( _declaredType instanceof SONTypeMemPtr ) func3=6; // 4 byte pointers, assumed unsigned?
        if( func3 == -1 ) throw Utils.TODO();
        return func3;
    }

    // 7 bits, 00 000 11 or 00 001 11 for FP
    int opcode(Encoding enc) {
        if( enc.reg(this) < riscv.F_OFFSET ) return 3;
        else if( this instanceof StoreRISC )
            // opcode is the same for 32 bit and 64 bits
            return 39;
        return 7;
    }
    short xreg(Encoding enc) {
        short xreg = enc.reg(this );
        return xreg < riscv.F_OFFSET ? xreg : (short)(xreg-riscv.F_OFFSET);
    }

    // Register mask allowed on input i.
    @Override public RegMask regmap(int i) {
        // 0 - ctrl
        // 1 - memory
        if( i==2 ) return riscv.RMASK;    // base
        // 2 - index
        if( i==4 ) return riscv.MEM_MASK; // value
        throw Utils.TODO();
    }

    SB asm_address(CodeGen code, SB sb) {
        sb.p("[").p(code.reg(ptr())).p("+");
        return sb.p(_off).p("]");
    }
}
