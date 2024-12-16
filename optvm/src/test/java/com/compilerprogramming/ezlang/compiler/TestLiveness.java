package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.TypeDictionary;
import org.junit.Assert;
import org.junit.Test;

public class TestLiveness {

    TypeDictionary compileSrc(String src) {
        var compiler = new Compiler();
        return  compiler.compileSrc(src);
    }

    @Test
    public void test1() {
        String src = """
                func print(n: Int) {}
                func foo() {
                    var i = 1
                    var s = 1;
                    while (1) {
                        if (i == 5)
                            s = 0;
                        s = s + 1
                        i = i + 1
                        if (i < 10)
                            continue;
                        break;
                    }
                    print(s);
                }
                """;
        var typeDict = compileSrc(src);
        var funcSymbol = typeDict.lookup("foo");
        CompiledFunction func = (CompiledFunction) ((Symbol.FunctionTypeSymbol)funcSymbol).code();
        var liveness = new Liveness();
        liveness.computeLiveness(func);
        String output = Compiler.dumpIR(typeDict, true);
        Assert.assertEquals(output, """
func print(n: Int)
Reg #0 %ret
Reg #1 n
L0:
    arg n
    goto  L1
L1:
func foo()
Reg #0 %ret
Reg #1 i
Reg #2 s
Reg #3 %t3
Reg #4 %t4
Reg #5 %t5
Reg #6 %t6
Reg #7 %t7
L0:
    i = 1
    s = 1
    goto  L2
    #UEVAR   = {}
    #VARKILL = {1, 2}
    #LIVEOUT = {1, 2}
L2:
    if 1 goto L3 else goto L4
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {1, 2}
L3:
    %t3 = i==5
    if %t3 goto L5 else goto L6
    #UEVAR   = {1}
    #VARKILL = {3}
    #LIVEOUT = {1, 2}
L5:
    s = 0
    goto  L6
    #UEVAR   = {}
    #VARKILL = {2}
    #LIVEOUT = {1, 2}
L6:
    %t4 = s+1
    s = %t4
    %t5 = i+1
    i = %t5
    %t6 = i<10
    if %t6 goto L7 else goto L8
    #UEVAR   = {1, 2}
    #VARKILL = {1, 2, 4, 5, 6}
    #LIVEOUT = {1, 2}
L7:
    goto  L2
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {1, 2}
L8:
    goto  L4
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {2}
L4:
    %t7 = s
    call print params %t7
    goto  L1
    #UEVAR   = {2}
    #VARKILL = {7}
    #LIVEOUT = {}
L1:
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {}
""");
    }

    @Test
    public void test2() {
        String src = """
                func foo(a: Int, b: Int) {
                    while (b < 10) {
                        if (b < a) {
                            a = a * 7
                            b = a + 1
                        }
                        else {
                            a = b - 1
                        }
                    }
                }
                """;
        var typeDict = compileSrc(src);
        var funcSymbol = typeDict.lookup("foo");
        CompiledFunction func = (CompiledFunction) ((Symbol.FunctionTypeSymbol)funcSymbol).code();
        var liveness = new Liveness();
        liveness.computeLiveness(func);
        String output = Compiler.dumpIR(typeDict, true);
        Assert.assertEquals(output, """
func foo(a: Int,b: Int)
Reg #0 %ret
Reg #1 a
Reg #2 b
Reg #3 %t3
Reg #4 %t4
Reg #5 %t5
Reg #6 %t6
Reg #7 %t7
L0:
    arg a
    arg b
    goto  L2
    #UEVAR   = {}
    #VARKILL = {1, 2}
    #LIVEOUT = {1, 2}
L2:
    %t3 = b<10
    if %t3 goto L3 else goto L4
    #UEVAR   = {2}
    #VARKILL = {3}
    #LIVEOUT = {1, 2}
L3:
    %t4 = b<a
    if %t4 goto L5 else goto L6
    #UEVAR   = {1, 2}
    #VARKILL = {4}
    #LIVEOUT = {1, 2}
L5:
    %t5 = a*7
    a = %t5
    %t6 = a+1
    b = %t6
    goto  L7
    #UEVAR   = {1}
    #VARKILL = {1, 2, 5, 6}
    #LIVEOUT = {1, 2}
L7:
    goto  L2
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {1, 2}
L6:
    %t7 = b-1
    a = %t7
    goto  L7
    #UEVAR   = {2}
    #VARKILL = {1, 7}
    #LIVEOUT = {1, 2}
L4:
    goto  L1
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {}
L1:
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {}
""");
    }

}
