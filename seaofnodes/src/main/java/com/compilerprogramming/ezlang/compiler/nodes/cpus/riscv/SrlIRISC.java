package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class SrlIRISC extends ImmRISC {
    SrlIRISC( Node and, int imm, boolean pop) { super(and,imm,pop); }
    @Override int opcode() {  return riscv.I_TYPE; }
    @Override int func3() {  return 5; }
    @Override public String glabel() { return ">>>"; }
    @Override public String op() { return "srli"; }
}
