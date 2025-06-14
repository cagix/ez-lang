package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class XorX86 extends RegX86 {
    XorX86( Node add ) { super(add); }
    @Override public String op() { return "xor"; }
    @Override public String glabel() { return "^"; }
    @Override int opcode() { return 0x33; }
    @Override public boolean commutes() { return true; }
}
