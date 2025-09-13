package com.compilerprogramming.ezlang.compiler.node.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.node.Node;

public class SraIRISC extends ImmRISC {
    public SraIRISC( Node and, int imm) {
        super(and,imm | (0x20 << 5));
    }
    @Override int opcode() {  return riscv.OP_IMM; }
    @Override int func3() {  return 5;}
    @Override public String glabel() { return ">>"; }
    @Override public String op() { return "srai"; }
}
