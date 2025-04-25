package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.nodes.CallEndNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;

public class CallEndARM extends CallEndNode implements MachNode {
    CallEndARM( CallEndNode cend ) { super(cend); }
}
