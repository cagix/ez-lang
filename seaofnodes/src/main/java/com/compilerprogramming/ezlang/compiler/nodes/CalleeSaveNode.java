package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.Utils;
import com.compilerprogramming.ezlang.compiler.codegen.Encoding;
import com.compilerprogramming.ezlang.compiler.codegen.RegMask;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import java.util.BitSet;
import java.io.ByteArrayOutputStream;

public class CalleeSaveNode extends ProjNode implements MachNode {
    final RegMask _mask;
    public CalleeSaveNode(FunNode fun, int reg, String label) {
        super(fun,reg,label);
        fun._outputs.pop();
        int i=0;
        while( fun.out(i) instanceof PhiNode )  i++;
        fun._outputs.insert(this,i);
        _mask = new RegMask(reg);
    }
    @Override public RegMask regmap(int i) { return null; }
    @Override public RegMask outregmap() { return _mask; }
    @Override public void encoding( Encoding enc ) { }

    @Override public SONType compute() { return SONType.BOTTOM; }
    @Override public Node idealize() { return null; }
}
