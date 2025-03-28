package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.sontypes.*;

import java.util.BitSet;

/**
 *  Allocation!  Allocate a chunk of memory, and pre-zero it.
 *  The inputs include control and size, and ALL aliases being set.
 *  The output is large tuple, one for every alias plus the created pointer.
 *  New is expected to be followed by projections for every alias.
 */
public class NewNode extends Node implements MultiNode {

    public final SONTypeMemPtr _ptr;
    public final int _len;

    public NewNode(SONTypeMemPtr ptr, Node... nodes) {
        super(nodes);
        assert !ptr.nullable();
        _ptr = ptr;
        _len = ptr._obj._fields.length;
        // Control in slot 0
        assert nodes[0]._type== SONType.CONTROL || nodes[0]._type == SONType.XCONTROL;
        // Malloc-length in slot 1
        assert nodes[1]._type instanceof SONTypeInteger || nodes[1]._type== SONType.NIL;
        for( int i=0; i<_len; i++ )
            // Memory slices for all fields.
            assert nodes[2+i]._type.isa(SONTypeMem.BOT);
    }

    public NewNode(NewNode nnn) { super(nnn); _ptr = nnn._ptr; _len = nnn._len; }

    public Node mem (int idx) { return in(idx+2); }

    @Override public String label() { return "new_"+glabel(); }
    @Override public String glabel() {
        return _ptr._obj.isAry() ? "ary_"+_ptr._obj._fields[1]._type.str() : _ptr._obj.str();
    }

    @Override
    public StringBuilder _print1(StringBuilder sb, BitSet visited) {
        sb.append("new ");
        return sb.append(_ptr._obj.str());
    }

    int findAlias(int alias) {
        int idx = _ptr._obj.findAlias(alias);
        assert idx!= -1;        // Error, caller should be calling
        return idx+2;           // Skip ctrl, size
    }

    // 0 - ctrl
    // 1 - byte size
    // 2-len+2 - aliases, one per field
    // len+2 - 2*len+2 - initial values, one per field
    public Node size() { return in(1); }

    @Override
    public SONTypeTuple compute() {
        Field[] fs = _ptr._obj._fields;
        SONType[] ts = new SONType[fs.length+2];
        ts[0] = SONType.CONTROL;
        ts[1] = _ptr;
        for( int i=0; i<fs.length; i++ ) {
            SONType mt = in(i+2)._type;
            SONTypeMem mem = mt== SONType.TOP ? SONTypeMem.TOP : (SONTypeMem)mt;
            SONType tfld = mem._t.meet(mem._t.makeZero());
            SONType tfld2 = tfld.join(fs[i]._type);
            ts[i+2] = SONTypeMem.make(fs[i]._alias,tfld2);
        }
        return SONTypeTuple.make(ts);
    }

    @Override
    public Node idealize() { return null; }

    @Override
    boolean eq(Node n) { return this == n; }

    @Override
    int hash() { return _ptr.hashCode(); }
}
