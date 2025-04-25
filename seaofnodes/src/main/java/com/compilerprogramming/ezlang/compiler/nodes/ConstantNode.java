package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import com.compilerprogramming.ezlang.compiler.sontypes.SONTypeFunPtr;
import java.util.BitSet;

/**
 * A Constant node represents a constant value.  At present, the only constants
 * that we allow are integer literals; therefore Constants contain an integer
 * value.  As we add other types of constants, we will refactor how we represent
 * Constants.
 * <p>
 * Constants have no semantic inputs. However, we set Start as an input to
 * Constants to enable a forward graph walk.  This edge carries no semantic
 * meaning, and it is present <em>solely</em> to allow visitation.
 * <p>
 * The Constant's value is the value stored in it.
 */

public class ConstantNode extends Node {
    public final SONType _con;
    public ConstantNode( SONType type ) {
        super(new Node[]{CodeGen.CODE._start});
        _con = _type = type;
    }
    public ConstantNode( Node con, SONType t ) { super(con);  _con = t;  }
    public ConstantNode( ConstantNode con ) { this(con,con._type);  }

    public static Node make( SONType type ) {
        if( type== SONType. CONTROL ) return new CtrlNode();
        if( type== SONType.XCONTROL ) return new XCtrlNode();
        return new ConstantNode(type);
    }

    @Override public String  label() { return "#"+_con; }
    @Override public String glabel() { return _con.gprint(new SB().p("#")).toString(); }

    @Override
    public String uniqueName() { return "Con_" + _nid; }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        if( _con instanceof SONTypeFunPtr tfp && tfp.isConstant() ) {
            FunNode fun = CodeGen.CODE.link(tfp);
            if( fun!=null && fun._name != null )
                return sb.append("{ ").append(fun._name).append("}");
        }
        return sb.append(_con==null ? "---" : _con.print(new SB()));
    }

    @Override public boolean isConst() { return true; }

    @Override
    public SONType compute() { return _con; }

    @Override
    public Node idealize() { return null; }

    @Override
    public boolean eq(Node n) {
        ConstantNode con = (ConstantNode)n; // Contract
        return _con==con._con;
    }

    @Override
    int hash() { return _con.hashCode(); }
}
