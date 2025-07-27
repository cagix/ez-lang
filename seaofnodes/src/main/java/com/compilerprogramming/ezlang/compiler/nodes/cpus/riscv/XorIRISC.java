package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class XorIRISC extends ImmRISC {
    public XorIRISC( Node and, int imm) { super(and,imm); }
    public XorIRISC( Node and, int imm, boolean pop) { super(and,imm,pop); }
    @Override int opcode() {  return riscv.OP_IMM; }
    @Override int func3() {  return 4; }
    @Override public String glabel() { return "^"; }
    @Override public String op() { return "xori"; }
}
