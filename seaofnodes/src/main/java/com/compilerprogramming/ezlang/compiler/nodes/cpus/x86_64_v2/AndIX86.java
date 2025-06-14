package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class AndIX86 extends ImmX86 {
    AndIX86( Node add, int imm ) { super(add,imm); }
    @Override public String op() { return "andi"; }
    @Override public String glabel() { return "&"; }
    @Override int opcode() { return 0x81; }
    @Override int mod() { return 4; }
}
