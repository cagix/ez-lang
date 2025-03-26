package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.IterPeeps;
import com.compilerprogramming.ezlang.compiler.sontypes.*;
import java.util.BitSet;

public class IfNode extends CFGNode implements MultiNode {

    public IfNode(Node ctrl, Node pred) {
        super(ctrl, pred);
        CodeGen.CODE.add(this); // Because idoms are complex, just add it
    }
    public IfNode(IfNode iff) { super(iff); }

    @Override
    public String label() { return "If"; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        sb.append("if( ");
        return in(1)._print0(sb, visited).append(" )");
    }

    public Node ctrl() { return in(0); }
    public Node pred() { return in(1); }

    // No one unique control follows
    @Override public CFGNode uctrl() { return null; }

    @Override
    public SONType compute() {
        // If the If node is not reachable then neither is any following Proj
        if (ctrl()._type != SONType.CONTROL && ctrl()._type != SONType.BOTTOM )
            return SONTypeTuple.IF_NEITHER;
        Node pred = pred();
        SONType t = pred._type;
        // High types mean NEITHER side is reachable.
        // Wait until the type falls to decide which way to go.
        if( t.isHigh() )
            return SONTypeTuple.IF_NEITHER;
        // If constant is 0 then false branch is reachable
        // Else true branch is reachable
        if( t.isConstant() )
            return (t== SONType.NIL || t== SONTypeInteger.ZERO || (t instanceof SONTypeFunPtr tfp && tfp._fidxs==0) ) ? SONTypeTuple.IF_FALSE : SONTypeTuple.IF_TRUE;
        // If adding a zero makes a difference, the predicate must not have a zero/null
        if( !t.makeZero().isa(t) )
            return SONTypeTuple.IF_TRUE;

        return SONTypeTuple.IF_BOTH;
    }

    @Override
    public Node idealize() {
        // Hunt up the immediate dominator tree.  If we find an identical if
        // test on either the true or false branch, that side wins.
        if( !pred()._type.isHighOrConst() )
            for( CFGNode dom = idom(), prior=this; dom!=null;  prior = dom, dom = dom.idom() )
                if( dom.addDep(this) instanceof IfNode iff && iff.pred().addDep(this)==pred() && prior instanceof CProjNode prj ) {
                    setDef(1,con( prj._idx==0 ? 1 : 0 ));
                    return this;
                }
        return null;
    }

    // MachNode variants need to support this and invert the conditional test.
    // The following CProjs will be inverted by the caller.
    public void invert() { throw Utils.TODO(); }

    public static String invert( String bop ) {
        return switch( bop ) {
        case "<"  -> ">=";
        case "<=" -> ">" ;
        case "==" -> "!=";
        case "!=" -> "==";
        case ">"  -> "<=";
        case ">=" -> "<" ;
        default -> throw Utils.TODO();
        };
    }
}
