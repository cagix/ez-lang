package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Symbol;
import org.junit.Assert;
import org.junit.Test;

import java.util.BitSet;

public class TestSCCP {

    String compileSrc(String src) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src);
        StringBuilder sb = new StringBuilder();
        for (Symbol s : typeDict.bindings.values()) {
            if (s instanceof Symbol.FunctionTypeSymbol f) {
                var functionBuilder = (CompiledFunction) f.code();
                new EnterSSA(functionBuilder);
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet(), false);
                sb.append(new SparseConditionalConstantPropagation().constantPropagation(functionBuilder).toString());
            }
        }
        return sb.toString();
    }

    @Test
    public void test1() {
        String src = """
func foo()->Int {
    var i = 1
    if (i == 0)
        i = 2
    else
        i = 3
    return i
}            
""";
        String actual = compileSrc(src);
        String expected = """
L0:
    i_0 = 1
    %t1_0 = i_0==0
    if %t1_0 goto L2 else goto L3
L2:
    i_2 = 2
    goto  L4
L4:
    i_3 = phi(i_2, i_1)
    ret i_3
    goto  L1
L1:
L3:
    i_1 = 3
    goto  L4
Flow edges:
L0->L3=Executable
L4->L1=Executable
L3->L4=Executable
Lattices:
i_0=1
%t1_0=0
i_1=3
i_3=3
""";
        Assert.assertEquals(expected, actual);
    }
}
