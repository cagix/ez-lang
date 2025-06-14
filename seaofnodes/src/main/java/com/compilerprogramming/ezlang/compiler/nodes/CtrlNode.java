package com.compilerprogramming.ezlang.compiler.nodes;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import com.compilerprogramming.ezlang.compiler.sontypes.SONType;
import java.util.BitSet;

public class CtrlNode extends CFGNode {
    public CtrlNode() { super(CodeGen.CODE._start); }
    @Override public String label() { return "Ctrl"; }
    @Override public StringBuilder _print1(StringBuilder sb, BitSet visited) { return sb.append("Cctrl"); }
    @Override public boolean isConst() { return true; }
    @Override  public SONType compute() { return SONType.CONTROL; }
    @Override public Node idealize() { return null; }
}
