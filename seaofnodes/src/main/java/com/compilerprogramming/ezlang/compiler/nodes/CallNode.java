package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.sontypes.Type;
import com.compilerprogramming.ezlang.compiler.sontypes.TypeFunPtr;
import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.util.BitSet;

/**
 *  Call
 */
public class CallNode extends CFGNode {

    public CallNode(Node... nodes) { super(nodes); }
    public CallNode(CallNode call) { super(call); }

    @Override public String label() { return "Call"; }

    @Override public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        String fname = name();
        if( fname == null ) fptr()._print0(sb,visited);
        else sb.append(fname);
        sb.append("( ");
        for( int i=2; i<nIns()-1; i++ )
            in(i)._print0(sb,visited).append(",");
        sb.setLength(sb.length()-1);
        return sb.append(")");
    }
    public String name() {
        if( fptr()._type instanceof TypeFunPtr tfp && tfp.isConstant() ) {
            FunNode fun = CodeGen.CODE.link(tfp);
            if( fun !=null ) return fun._name;
            if( fptr() instanceof ExternNode ex )  return ex._extern;
        }
        return null;
    }



    Node ctrl() { return in(0); }
    Node mem () { return in(1); }
    // Args mapped 1-to-1 on inputs, so conceptually start at 2
    public Node arg(int idx) { return in(idx); }
    // Same arg accounting as TFPs, although the numbering starts at 2
    public int nargs() { return nIns()-3; } // Minus control, memory, fptr
    // args from input 2 to last; last is function input
    public Node fptr() { return _inputs.last(); }
    // Error if not a TFP
    public TypeFunPtr tfp() { return (TypeFunPtr)fptr()._type; }

    // Call is to an externally supplied code
    public boolean external() { return false; }


    // Find the Call End from the Call
    public CallEndNode cend() {
        // Always in use slot 0
        if( nOuts()>0 && out(0) instanceof CallEndNode cend ) {
            assert _cend()==cend;
            return cend;
        } else {
            assert _cend()==null;
            return null;
        }
    }
    private CallEndNode _cend() {
        CallEndNode cend=null;
        for( Node n : _outputs )
            if( n instanceof CallEndNode cend0 )
                { assert cend == null; cend = cend0; }
        return cend;
    }

    // Get the one control following; error to call with more than one e.g. an
    // IfNode or other multi-way branch.
    @Override public CFGNode uctrl() { return cend(); }


    @Override
    public Type compute() {
        return ctrl()._type;
    }

    @Override
    public Node idealize() {
        CallEndNode cend = cend();
        if( cend==null ) return null; // Still building

        // Link: call calls target function.  Linking makes the target FunNode
        // point to this Call, and all his Parms point to the call arguments;
        // also the CallEnd points to the Return.
        Node progress = null;
        if( fptr()._type instanceof TypeFunPtr tfp && tfp.nargs() == nargs() ) {
            // If fidxs is negative, then infinite unknown functions
            long fidxs = tfp.fidxs();
            if( fidxs > 0 ) {
                // Wipe out the return which matching in the linker table
                // Walk the (63 max) bits and link
                for( ; fidxs!=0; fidxs = TypeFunPtr.nextFIDX(fidxs) ) {
                    int fidx = Long.numberOfTrailingZeros(fidxs);
                    TypeFunPtr tfp0 = tfp.makeFrom(fidx);
                    FunNode fun = CodeGen.CODE.link(tfp0);
                    if( fun!=null && !fun._folding && !linked(fun) )
                        progress = link(fun);
                }
            }
        }

        return progress;
    }

    // True if Fun is linked to this Call
    boolean linked( FunNode fun ) {
        for( Node n : fun._inputs )
            if( n == this )
                return true;
        return false;
    }


    // Link so this calls fun
    private Node link( FunNode fun ) {
        assert !linked(fun);
        fun.addDef(this);
        for( Node use : fun._outputs )
            if( use instanceof ParmNode parm )
                parm.addDef(parm._idx==0 ? new ConstantNode(cend()._rpc).peephole() : arg(parm._idx));
        // Call end points to function return
        CodeGen.CODE.add(cend()).addDef(fun.ret());
        return this;
    }

    // Unlink all linked functions
    public void unlink_all() {
        for( int i=0; i<_outputs._len; i++ )
            if( out(i) instanceof FunNode fun ) {
                assert linked(fun);
                int idx = fun._inputs.find(this);
                for( Node use : fun._outputs )
                    if( use instanceof ParmNode )
                        use.delDef(idx);
                fun.delDef(idx);
                cend().delDef(cend()._inputs.find(fun.ret()));
                assert !linked(fun);
                i--;
            }
    }

    @Override
    public CompilerException err() {
        if( !(fptr()._type instanceof TypeFunPtr tfp) )
            throw Utils.TODO();
        if( !tfp.notNull() )
            return Compiler.error( "Might be null calling "+tfp);
        if( nargs() != tfp.nargs() )
            return Compiler.error( "Expecting "+tfp.nargs()+" arguments, but found "+nargs());

        // Check for args
        for( int i=0; i<tfp.nargs(); i++ )
            if( !arg(i+2)._type.isa(tfp.arg(i)) )
                return Compiler.error( "Argument #"+i+" isa "+arg(i+2)._type+", but must be a "+tfp.arg(i));

        if( tfp.fidxs() < 0 )
            throw Utils.TODO(); // Infinite unknown TFPs?  Should be fairly precise CG

        return null;
    }

}
