package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.nodes.ParmNode;

public class ParmX86 extends ParmNode implements MachNode {
    final RegMask _rmask;
    ParmX86( ParmNode parm ) {
        super(parm);
        _rmask = x86_64_v2.callInMask(fun().sig(),_idx,1/*RPC*/);
    }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return _rmask; }
    @Override public void encoding( Encoding enc ) { }
}
