package com.compilerprogramming.ezlang.compiler.node.cpus.arm;

import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.node.MachNode;
import com.compilerprogramming.ezlang.compiler.node.ParmNode;

public class ParmARM extends ParmNode implements MachNode {
    final RegMask _rmask;
    ParmARM(ParmNode parm) {
        super(parm);
        _rmask = arm.callInMask(fun().sig(),_idx,0);
    }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return _rmask; }
    @Override public void encoding( Encoding enc ) { }
}
