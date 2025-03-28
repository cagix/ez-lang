package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import java.util.BitSet;

public class XCtrlNode extends CFGNode {
    public XCtrlNode() { super(new Node[]{CodeGen.CODE._start}); }
    @Override public String label() { return "Xctrl"; }
    @Override public StringBuilder _print1(StringBuilder sb, BitSet visited) { return sb.append("Xctrl"); }
    @Override public boolean isConst() { return true; }
    @Override  public SONType compute() { return SONType.XCONTROL; }
    @Override public Node idealize() { return null; }
}
