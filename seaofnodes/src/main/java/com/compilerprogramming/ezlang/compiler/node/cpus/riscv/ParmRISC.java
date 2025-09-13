package com.compilerprogramming.ezlang.compiler.node.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.node.MachNode;
import com.compilerprogramming.ezlang.compiler.node.ParmNode;

public class ParmRISC extends ParmNode implements MachNode {
    final RegMask _rmask;
    ParmRISC(ParmNode parm) {
        super(parm);
        _rmask = riscv.callInMask(fun().sig(),_idx,0);
    }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return _rmask; }
    @Override public void encoding( Encoding enc ) { }
}
