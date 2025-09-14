package com.compilerprogramming.ezlang.compiler.node;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.util.SB;
import com.compilerprogramming.ezlang.compiler.type.Type;
import com.compilerprogramming.ezlang.compiler.type.TypeFunPtr;
import java.util.BitSet;

/**
 * A Constant node represents a constant value.
 * <p>
 * Constants have no semantic inputs. However, we set Start as an input to
 * Constants to enable a forward graph walk.  This edge carries no semantic
 * meaning, and it is present <em>solely</em> to allow visitation.
 * <p>
 * The Constant's value is the value stored in it.
 */

public class ConstantNode extends Node {
    public final Type _con;
    public ConstantNode( Type type ) {
        super(new Node[]{CodeGen.CODE._start});
        _con = _type = type;
    }
    public ConstantNode( ConstantNode con ) { super(con);  _con = con._type;  }

    public static Node make( Type type ) {
        if( type==Type. CONTROL ) return new CtrlNode();
        if( type==Type.XCONTROL ) return new XCtrlNode();
        return new ConstantNode(type);
    }

    @Override public String  label() { return "#"+_con; }
    @Override public String glabel() { return "#"+_con.gprint(new SB()); }
    @Override public String uniqueName() { return "Con_" + _nid; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        if( _con instanceof TypeFunPtr tfp && tfp.isConstant() ) {
            FunNode fun = CodeGen.CODE.link(tfp);
            if( fun!=null && fun._name != null )
                return sb.append("{ ").append(fun._name).append("}");
        }
        return sb.append(_con==null ? "---" : _con.toString());
    }

    @Override public boolean isConst() { return true; }
    @Override public Type compute() { return _con; }
    @Override public Node idealize() { return null; }
    @Override public boolean eq(Node n) { return _con==((ConstantNode)n)._con; }
    @Override int hash() { return _con.hashCode(); }
}
