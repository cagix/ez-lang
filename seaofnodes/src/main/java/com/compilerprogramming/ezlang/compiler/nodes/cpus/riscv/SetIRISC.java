package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.nodes.Node;

// corresponds to slti,sltiu
public class SetIRISC extends ImmRISC {
    final boolean _unsigned;    // slti vs sltiu
    public SetIRISC( Node src, int imm12, boolean unsigned ) {
        super(src,imm12);
        _unsigned = unsigned;
    }
    @Override public String op() { return "slt" + (_unsigned ? "u":"") + "i"; }
    @Override public String glabel() { return  "<"+ (_unsigned ? "u":""); }
    @Override int opcode() { return 0b0010011; }
    @Override int func3() { return _unsigned ? 0b011 : 0b010; }
}
