package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.nodes.ProjNode;

public class ProjX86 extends ProjNode implements MachNode {
    ProjX86( ProjNode p ) { super(p); }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() {
        return ((MachNode)in(0)).outregmap(_idx);
    }
    @Override public void encoding( Encoding enc ) {}
}
