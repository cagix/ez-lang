package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.codegen.RegMaskRW;
import com.compilerprogramming.ezlang.compiler.sontypes.*;
import java.util.BitSet;

/**
 *  CallEnd
 */
public class CallEndNode extends CFGNode implements MultiNode {

    // When set true, this Call/CallEnd/Fun/Return is being trivially inlined
    private boolean _folding;
    public final SONTypeRPC _rpc;

    public CallEndNode(CallNode call) { super(new Node[]{call}); _rpc = SONTypeRPC.constant(_nid); }
    public CallEndNode(CallEndNode cend) { super(cend); _rpc = cend._rpc; }

    @Override public String label() { return "CallEnd"; }
    @Override public boolean blockHead() { return true; }

    public CallNode call() { return (CallNode)in(0); }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        sb.append("cend( ");
        sb.append( in(0) instanceof CallNode ? "Call, " : "----, ");
        for( int i=1; i<nIns()-1; i++ )
            in(i)._print0(sb,visited).append(",");
        sb.setLength(sb.length()-1);
        return sb.append(")");
    }

    @Override
    public SONType compute() {
        if( !(in(0) instanceof CallNode call) )
            return SONTypeTuple.RET.dual();
        SONType ret = SONType.BOTTOM;
        SONTypeMem mem = SONTypeMem.BOT;
        if( addDep(call.fptr())._type instanceof SONTypeFunPtr tfp ) {
            ret = tfp.ret();
            // Here, if I can figure out I've found *all* callers, then I can meet
            // across the linked returns and join with the function return type.
            if( tfp.isConstant() && nIns()>1 ) {
                assert nIns()==2;     // Linked exactly once for a constant
                ret = ((SONTypeTuple)in(1)._type).ret(); // Return type
            }
        }
        return SONTypeTuple.make(call._type, SONTypeMem.BOT,ret);
    }

    @Override
    public Node idealize() {

        // Trivial inlining: call site calls a single function; single function
        // is only called by this call site.
        if( !_folding && nIns()==2 && in(0) instanceof CallNode call ) {
            Node fptr = call.fptr();
            if( fptr.nOuts() == 1 && // Only user is this call
                fptr instanceof ConstantNode && // We have an immediate call
                // Function is being called, and its not-null
                fptr._type instanceof SONTypeFunPtr tfp && tfp.notNull() &&
                // Arguments are correct
                call.err()==null ) {
                ReturnNode ret = (ReturnNode)in(1);
                FunNode fun = ret.fun();
                // Expecting Start, and the Call
                if( fun.nIns()==3 ) {
                    assert fun.in(1) instanceof StartNode && fun.in(2)==call;
                    // Disallow self-recursive inlining (loop unrolling by another name)
                    CFGNode idom = call;
                    while( !(idom instanceof FunNode fun2) )
                        idom = idom.idom();
                    if( idom != fun ) {
                        // Trivial inline: rewrite
                        _folding = true;
                        // Rewrite Fun so the normal RegionNode ideal collapses
                        fun._folding = true;
                        fun.setDef(1, Compiler.XCTRL); // No default/unknown StartNode caller
                        fun.setDef(2,call.ctrl());  // Bypass the Call;
                        fun.ret().setDef(3,null);   // Return is folding also
                        CodeGen.CODE.addAll(fun._outputs);
                        return this;
                    }
                } else {
                    addDep(fun);
                }
            } else { // Function ptr has multiple users (so maybe multiple call sites)
                addDep(fptr);
            }
        }

        return null;
    }

    @Override public Node pcopy(int idx) {
        return _folding ? in(1).in(idx) : null;
    }

    // ------------
    // MachNode specifics, shared across all CPUs
    public int _xslot;
    private RegMask _retMask;
    private RegMask _kills;
    public void cacheRegs(CodeGen code) {
        // Return mask depends on TFP (either GPR or FPR)
        _retMask = code._mach.retMask(call().tfp());
        // Kill mask is all caller-saves, and any mirror stack slots for args
        // in registers.
        RegMaskRW kills = code._callerSave.copy();
        // Start of stack slots
        int maxReg = code._mach.regs().length;
        // Incoming function arg slots, all low numbered in the RA
        int fslot = fun()._maxArgSlot;
        // Killed slots for this calls outgoing args
        int xslot = code._mach.maxArgSlot(call().tfp());
        _xslot = (maxReg+fslot)+xslot;
        for( int i=0; i<xslot; i++ )
            kills.set((maxReg+fslot)+i);
        _kills = kills;
    }
    public String op() { return "cend"; }
    public RegMask regmap(int i) { return null; }
    public RegMask outregmap() { return null; }
    public RegMask outregmap(int idx) { return idx==2  ? _retMask : null; }
    public RegMask killmap() { return _kills; }
    public void encoding( Encoding enc ) { }
    public void asm(CodeGen code, SB sb) {  }
}
