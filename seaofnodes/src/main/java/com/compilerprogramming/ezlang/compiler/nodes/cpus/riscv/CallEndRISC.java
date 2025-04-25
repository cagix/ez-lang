package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.nodes.CallEndNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;

public class CallEndRISC extends CallEndNode implements MachNode {
    CallEndRISC( CallEndNode cend ) { super(cend); }
}
