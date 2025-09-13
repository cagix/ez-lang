package com.compilerprogramming.ezlang.compiler.node.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.node.Node;

public class OrIX86 extends ImmX86 {
    OrIX86( Node add, int imm ) { super(add,imm); }
    @Override public String op() { return "ori"; }
    @Override public String glabel() { return "|"; }
    @Override int opcode() { return 0x81; }
    @Override int mod() { return 1; }
}
