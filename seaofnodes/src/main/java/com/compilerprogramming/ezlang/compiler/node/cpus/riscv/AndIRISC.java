package com.compilerprogramming.ezlang.compiler.node.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.node.Node;

public class AndIRISC extends ImmRISC {
    public AndIRISC( Node and, int imm) { super(and,imm); }
    @Override public String op() { return "andi"; }
    @Override public String glabel() { return "&"; }
    @Override int opcode() {  return riscv.OP_IMM; }
    @Override int func3() {return 7;}
}
