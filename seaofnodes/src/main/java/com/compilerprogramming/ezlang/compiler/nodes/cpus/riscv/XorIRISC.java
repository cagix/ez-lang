package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class XorIRISC extends ImmRISC {
    XorIRISC( Node and, int imm) { super(and,imm); }
    @Override int opcode() {  return riscv.I_TYPE; }
    @Override int func3() {  return 4; }
    @Override public String glabel() { return "^"; }
    @Override public String op() { return "xori"; }
}
