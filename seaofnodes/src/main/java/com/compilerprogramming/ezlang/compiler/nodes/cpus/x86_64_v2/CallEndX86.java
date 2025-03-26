package com.compilerprogramming.ezlang.compiler.nodes.cpus.x86_64_v2;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.CallEndNode;
import com.compilerprogramming.ezlang.compiler.nodes.MachNode;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFunPtr;

public class CallEndX86 extends CallEndNode implements MachNode {
    final SONTypeFunPtr _tfp;
    CallEndX86( CallEndNode cend ) {
        super(cend);
        _tfp = (SONTypeFunPtr)(cend.call().fptr()._type);
    }
    @Override public String op() { return "cend"; }
    @Override public String label() { return op(); }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return null; }
    @Override public RegMask outregmap(int idx) { return idx == 2 ? x86_64_v2.retMask(_tfp,2) : null; }
    @Override public RegMask killmap() { return x86_64_v2.x86CallerSave(); }
    @Override public void encoding( Encoding enc ) { }
    @Override public void asm(CodeGen code, SB sb) {  }
}
