package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.nodes.CallEndNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;

public class CallEndX86 extends CallEndNode implements MachNode {
    CallEndX86( CallEndNode cend ) { super(cend); }
}
