package com.compilerprogramming.ezlang.compiler.nodes.cpus.arm;

import com.compilerprogramming.ezlang.compiler.*;
import com.compilerprogramming.ezlang.compiler.codegen.*;
import com.compilerprogramming.ezlang.compiler.nodes.*;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFunPtr;

public class CallARM extends CallNode implements MachNode, RIPRelSize {
    final SONTypeFunPtr _tfp;
    final String _name;

    CallARM(CallNode call, SONTypeFunPtr tfp) {
        super(call);
        _inputs.pop(); // Pop constant target
        assert tfp.isConstant();
        _tfp = tfp;
        _name = CodeGen.CODE.link(tfp)._name;
    }

    @Override public String op() { return "call"; }
    @Override public String label() { return op(); }
    @Override public String name() { return _name; }
    @Override public SONTypeFunPtr tfp() { return _tfp; }
    @Override public RegMask regmap(int i) { return arm.callInMask(_tfp,i); }
    @Override public RegMask outregmap() { return null; }

    @Override public void encoding( Encoding enc ) {
        enc.relo(this);
        // BL
        enc.add4(arm.b(0b100101,0)); // Target patched at link time
    }

    // Delta is from opcode start, but X86 measures from the end of the 5-byte encoding
    @Override public byte encSize(int delta) { return 4; }

    // Delta is from opcode start
    @Override public void patch( Encoding enc, int opStart, int opLen, int delta ) {
        enc.patch4(opStart,arm.b(0b100101,delta));
    }

    @Override public void asm(CodeGen code, SB sb) {
        sb.p(_name);
        for( int i=0; i<nargs(); i++ )
            sb.p(code.reg(arg(i+2))).p("  ");
        sb.unchar(2);
    }
}
