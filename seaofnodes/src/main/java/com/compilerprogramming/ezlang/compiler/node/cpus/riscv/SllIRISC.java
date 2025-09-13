package com.compilerprogramming.ezlang.compiler.node.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.node.Node;

public class SllIRISC extends ImmRISC {
    public SllIRISC( Node and, int imm) { super(and,imm); }
    @Override int opcode() {  return riscv.OP_IMM; }
    @Override int func3() {  return 1; }
    @Override public String glabel() { return "<<"; }
    @Override public String op() { return "slli"; }
}
