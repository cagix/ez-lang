package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class ShrIX86 extends ImmX86 {
    ShrIX86( Node add, int imm ) { super(add,imm); }
    @Override public String op() { return "shri"; }
    @Override public String glabel() { return ">>>"; }
    @Override int opcode() { return 0xC1; }
    @Override int mod() { return 5; }
}
