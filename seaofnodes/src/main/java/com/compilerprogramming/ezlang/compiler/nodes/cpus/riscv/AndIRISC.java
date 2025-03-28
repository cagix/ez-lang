package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.nodes.Node;

public class AndIRISC extends ImmRISC {
    AndIRISC( Node and, int imm) { super(and,imm); }
    @Override public String op() { return "andi"; }
    @Override public String glabel() { return "&"; }
    @Override int opcode() {  return riscv.I_TYPE; }
    @Override int func3() {return 7;}
}
