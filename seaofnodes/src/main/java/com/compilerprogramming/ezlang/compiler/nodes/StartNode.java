package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.sontypes.*;

import java.util.BitSet;

import static com.compilerprogramming.ezlang.compiler.Utils.TODO;

/**
 * The Start node represents the start of the function.
 *
 * Start initially has 1 input (arg) from outside and the initial control.
 * In ch10 we also add mem aliases as structs get defined; each field in struct
 * adds a distinct alias to Start's tuple.
 */
public class StartNode extends LoopNode implements MultiNode {

    final SONType _arg;

    public StartNode(SONType arg) { super((Node)null); _arg = arg; _type = compute(); }
    public StartNode(StartNode start) { super(start); _arg = start==null ? null : start._arg; }

    @Override public String label() { return "Start"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
      return sb.append(label());
    }

    @Override public boolean blockHead() { return true; }
    @Override public CFGNode cfg0() { return null; }

    // Get the one control following; error to call with more than one e.g. an
    // IfNode or other multi-way branch.  For Start, its "main"
    @Override public CFGNode uctrl() {
        // Find "main", its the start.
        CFGNode C = null;
        for( Node use : _outputs )
            if( use instanceof FunNode fun && fun.sig().isa(CodeGen.CODE._main) )
                { assert C==null; C = fun; }
        return C;
    }


    @Override public SONTypeTuple compute() {
        return SONTypeTuple.make(SONType.CONTROL, SONTypeMem.TOP,_arg);
    }

    @Override public Node idealize() { return null; }

    // No immediate dominator, and idepth==0
    @Override public int idepth() { return CodeGen.CODE.iDepthAt(0); }
    @Override public CFGNode idom(Node dep) { return null; }

}
