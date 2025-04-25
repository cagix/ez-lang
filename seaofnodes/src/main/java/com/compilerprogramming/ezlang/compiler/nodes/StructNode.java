package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.sontypes.*;
import java.util.BitSet;

/**
 * Build a compound object
 */
public class StructNode extends Node {

    public SONTypeStruct _ts;

    @Override public String label() { return _ts==null ? "STRUCT?" : _ts.str(); }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        if( _ts==null ) return sb.append("STRUCT?");
        sb.append(_ts._name).append(" {");
        for( int i=0; i<nIns(); i++ ) {
            sb.append(_ts._fields[i]._fname).append(":");
            sb.append((in(i)==null ? SONType.BOTTOM : in(i)._type).print(new SB()));
            sb.append("; ");
        }
        sb.setLength(sb.length()-2);
        return sb.append("}");
    }

    @Override
    public SONTypeStruct compute() {
        if( _ts==null ) return SONTypeStruct.BOT;
        Field[] fs = new Field[_ts._fields.length];
        for( int i=0; i<fs.length; i++ )
            fs[i] = _ts._fields[i].makeFrom(in(i)==null ? SONType.TOP : in(i)._type);
        return SONTypeStruct.make(_ts._name,fs);
    }

    @Override
    public Node idealize() { return null; }

    @Override
    public boolean eq(Node n) { return _ts == ((StructNode)n)._ts; }

    @Override
    int hash() { return _ts==null ? 0 : _ts.hashCode(); }
}
