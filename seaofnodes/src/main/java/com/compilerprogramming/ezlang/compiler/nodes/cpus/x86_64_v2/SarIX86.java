package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.nodes.Node;

// Arithmetic Right Shift
public class SarIX86 extends ImmX86 {
    SarIX86( Node add, int imm ) { super(add,imm); assert x86_64_v2.imm8(imm); }
    @Override public String op() { return "sari"; }
    @Override public String glabel() { return ">>"; }
    @Override int opcode() { return 0xC1; }
    @Override int mod() { return 7; }
}
