package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.sontypes.*;
import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.util.BitSet;

/**
 * The Return node has two inputs.  The first input is a control node and the
 * second is the data node that supplies the return value.
 * <p>
 * In this presentation, Return functions as a Stop node, since multiple <code>return</code> statements are not possible.
 * The Stop node will be introduced in Chapter 6 when we implement <code>if</code> statements.
 * <p>
 * The Return's output is the value from the data node.
 */
public class ReturnNode extends CFGNode {

    public FunNode _fun;

    public ReturnNode(Node ctrl, Node mem, Node data, Node rpc, FunNode fun ) {
        super(ctrl, mem, data, rpc);
        _fun = fun;
    }
    public ReturnNode( ReturnNode ret, FunNode fun ) { super(ret);  _fun = fun;  }

    public Node ctrl() { return in(0); }
    public Node mem () { return in(1); }
    public Node expr() { return in(2); }
    public Node rpc () { return in(3); }
    public FunNode fun() { return _fun; }

    @Override
    public String label() { return "Return"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        sb.append("return ");
        if( expr()==null ) sb.append("----");
        else expr()._print0(sb, visited);
        return sb.append(";");
    }

    // No one unique control follows; can be many call end sites
    @Override public CFGNode uctrl() { return null; }

    @Override
    public SONType compute() {
        if( inProgress () ) return SONTypeTuple.RET; // In progress
        if( _fun.isDead() ) return SONTypeTuple.RET.dual(); // Dead another way
        return SONTypeTuple.make(ctrl()._type,mem()._type,expr()._type);
    }

    @Override public Node idealize() {
        if( inProgress () ) return null;
        if( _fun.isDead() ) return null;

//        // Upgrade signature based on return type
//        SONType ret = expr()._type;
//        SONTypeFunPtr fcn = _fun.sig();
//        assert ret.isa(fcn.ret());
//        if( ret != fcn.ret() )
//            _fun.setSig(fcn.makeFrom(ret));

        // If dead (cant be reached; infinite loop), kill the exit values
        if( ctrl()._type== SONType.XCONTROL &&
            !(mem() instanceof ConstantNode && expr() instanceof ConstantNode) ) {
            Node top = new ConstantNode(SONType.TOP).peephole();
            setDef(1,top);
            setDef(2,top);
            return this;
        }

        return null;
    }

    public boolean inProgress() {
        return ctrl().getClass() == RegionNode.class && ((RegionNode)ctrl()).inProgress();
    }

    // Gather parse-time return types for error reporting
    private SONType mt = SONType.TOP;
    private boolean ti=false, tf=false, tp=false, tn=false;

    // Add a return exit to the current parsing function
    void addReturn( Node ctrl, Node rmem, Node expr ) {
        assert inProgress();

        // Gather parse-time return types for error reporting
        SONType t = expr._type;
        mt = mt.meet(t);
        ti |= t instanceof SONTypeInteger x;
        tf |= t instanceof SONTypeFloat x;
        tp |= t instanceof SONTypeMemPtr x;
        tn |= t== SONType.NIL;

        // Merge path into the One True Return
        RegionNode r = (RegionNode)ctrl();
        // Assert that the Phis are in particular outputs; not reordered or shuffled
        PhiNode mem = (PhiNode)r.out(0); assert mem._declaredType == SONTypeMem.BOT;
        PhiNode rez = (PhiNode)r.out(1); assert rez._declaredType == SONType.BOTTOM;
        // Pop "inProgress" null off
        r  ._inputs.pop();
        mem._inputs.pop();
        rez._inputs.pop();
        // Add new return point
        r  .addDef(ctrl);
        mem.addDef(rmem);
        rez.addDef(expr);
        // Back to being inProgress
        r  .addDef(null);
        mem.addDef(null);
        rez.addDef(null);
    }

    @Override public CompilerException err() {
        return expr()._type/*mt*/== SONType.BOTTOM ? mixerr(ti,tf,tp,tn) : null;
    }

    static CompilerException mixerr( boolean ti, boolean tf, boolean tp, boolean tn) {
        if( !ti && !tf && !tp && !tn )
            return Compiler.error("No defined return type");
        SB sb = new SB().p("No common type amongst ");
        if( ti ) sb.p("int and ");
        if( tf ) sb.p("f64 and ");
        if( tp || tn ) sb.p("reference and ");
        return Compiler.error(sb.unchar(5).toString());
    }
}
