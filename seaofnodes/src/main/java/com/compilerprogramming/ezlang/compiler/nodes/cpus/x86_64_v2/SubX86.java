package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class SubX86 extends RegX86 {
    SubX86( Node add ) { super(add); }
    @Override public String op() { return "sub"; }
    @Override public String glabel() { return "-"; }
    @Override int opcode() { return 0x2B; }
}
