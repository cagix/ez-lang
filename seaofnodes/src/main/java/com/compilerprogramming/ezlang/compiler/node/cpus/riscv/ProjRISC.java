package com.compilerprogramming.ezlang.compiler.node.cpus.riscv;

import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.node.MachNode;
import com.compilerprogramming.ezlang.compiler.node.ProjNode;

public class ProjRISC extends ProjNode implements MachNode {
    ProjRISC(ProjNode p) { super(p); }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() {
        return ((MachNode)in(0)).outregmap(_idx);
    }
    @Override public void encoding( Encoding enc ) { }
}
