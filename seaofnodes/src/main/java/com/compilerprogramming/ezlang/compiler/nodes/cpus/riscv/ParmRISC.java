package com.compilerprogramming.ezlang.compiler.nodes.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.nodes.ParmNode;

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
