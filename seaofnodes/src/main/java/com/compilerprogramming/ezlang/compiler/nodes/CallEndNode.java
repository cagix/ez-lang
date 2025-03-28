package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
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
        if( call.fptr().addDep(this)._type instanceof SONTypeFunPtr tfp ) {
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
                    fun.addDep(this);
                }
            } else { // Function ptr has multiple users (so maybe multiple call sites)
                fptr.addDep(this);
            }
        }

        return null;
    }

    @Override public Node pcopy(int idx) {
        return _folding ? in(1).in(idx) : null;
    }
}
