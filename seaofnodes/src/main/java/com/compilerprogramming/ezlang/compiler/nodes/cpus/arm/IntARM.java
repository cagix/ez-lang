package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.ConstantNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeInteger;

// Integer constants
public class IntARM extends ConstantNode implements MachNode {
    IntARM( ConstantNode con ) { super(con); }

    @Override public String op() { return "ldi"; }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return arm.WMASK; }

    @Override public boolean isClone() { return true; }
    @Override public IntARM copy() { return new IntARM(this); }

    @Override public void encoding( Encoding enc ) {
        short self = enc.reg(this);
        long x = _con==SONType.NIL ? 0 : ((SONTypeInteger)_con).value();
        int nb0 = 0;
        int nb1 = 0;
        // Count number of 0000 and FFFF blocks
        for (int i=0; i<64; i+=16) {
            int block = (int)(x >> i) & 0xFFFF;
            if (block == 0) nb0++;
            if (block == 0xFFFF) nb1++;
        }
        int pattern;
        int op;
        if(nb0 >= nb1) {
            // More 0 blocks then F blocks, use movz
            pattern = 0;
            op = arm.OP_MOVZ;
        } else {
            // More F blocks then 0 blocks, use movn
            pattern = 0xFFFF;
            op = arm.OP_MOVN;
        }
        int invert = pattern;
        for (int i=0; i<4; i++) {
            int block = (int)x & 0xFFFF;
            x >>= 16;
            if (block != pattern) {
                enc.add4(arm.mov(op, i, block ^ invert, self));
                op = arm.OP_MOVK;
                invert = 0;
            }
        }
        if (op != arm.OP_MOVK) {
            // All blocks are the same, special case
            enc.add4(arm.mov(op, 0, 0, self));
        }
    }

    // Human-readable form appended to the SB.  Things like the encoding,
    // indentation, leading address or block labels not printed here.
    // Just something like "ld4\tR17=[R18+12] // Load array base".
    // General form: "op\tdst=src+src"
    @Override public void asm(CodeGen code, SB sb) {
        String reg = code.reg(this);
        _con.print(sb.p(reg).p(" = #"));
    }
}
