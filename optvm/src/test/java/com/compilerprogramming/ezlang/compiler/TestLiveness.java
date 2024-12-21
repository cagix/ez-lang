package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.Type;
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
        func.livenessAnalysis();
        String output = Compiler.dumpIR(typeDict, true);
        Assert.assertEquals("""
func print(n: Int)
Reg #0 n
L0:
    arg n
    goto  L1
L1:
func foo()
Reg #0 i
Reg #1 s
Reg #2 %t2
Reg #3 %t3
Reg #4 %t4
Reg #5 %t5
Reg #6 %t6
L0:
    i = 1
    s = 1
    goto  L2
    #UEVAR   = {}
    #VARKILL = {0, 1}
    #LIVEOUT = {0, 1}
L2:
    if 1 goto L3 else goto L4
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {0, 1}
L3:
    %t2 = i==5
    if %t2 goto L5 else goto L6
    #UEVAR   = {0}
    #VARKILL = {2}
    #LIVEOUT = {0, 1}
L5:
    s = 0
    goto  L6
    #UEVAR   = {}
    #VARKILL = {1}
    #LIVEOUT = {0, 1}
L6:
    %t3 = s+1
    s = %t3
    %t4 = i+1
    i = %t4
    %t5 = i<10
    if %t5 goto L7 else goto L8
    #UEVAR   = {0, 1}
    #VARKILL = {0, 1, 3, 4, 5}
    #LIVEOUT = {0, 1}
L7:
    goto  L2
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {0, 1}
L8:
    goto  L4
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {1}
L4:
    %t6 = s
    call print params %t6
    goto  L1
    #UEVAR   = {1}
    #VARKILL = {6}
    #LIVEOUT = {}
L1:
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {}
""", output);
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
        func.livenessAnalysis();
        String output = Compiler.dumpIR(typeDict, true);
        Assert.assertEquals("""
func foo(a: Int,b: Int)
Reg #0 a
Reg #1 b
Reg #2 %t2
Reg #3 %t3
Reg #4 %t4
Reg #5 %t5
Reg #6 %t6
L0:
    arg a
    arg b
    goto  L2
    #UEVAR   = {}
    #VARKILL = {0, 1}
    #LIVEOUT = {0, 1}
L2:
    %t2 = b<10
    if %t2 goto L3 else goto L4
    #UEVAR   = {1}
    #VARKILL = {2}
    #LIVEOUT = {0, 1}
L3:
    %t3 = b<a
    if %t3 goto L5 else goto L6
    #UEVAR   = {0, 1}
    #VARKILL = {3}
    #LIVEOUT = {0, 1}
L5:
    %t4 = a*7
    a = %t4
    %t5 = a+1
    b = %t5
    goto  L7
    #UEVAR   = {0}
    #VARKILL = {0, 1, 4, 5}
    #LIVEOUT = {0, 1}
L7:
    goto  L2
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {0, 1}
L6:
    %t6 = b-1
    a = %t6
    goto  L7
    #UEVAR   = {1}
    #VARKILL = {0, 6}
    #LIVEOUT = {0, 1}
L4:
    goto  L1
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {}
L1:
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {}
""", output);
    }

    /* page 448 Engineering a Compiler */
    static CompiledFunction buildTest3() {
        TypeDictionary typeDictionary = new TypeDictionary();
        Type.TypeFunction functionType = new Type.TypeFunction("foo");
        functionType.setReturnType(typeDictionary.INT);
        CompiledFunction function = new CompiledFunction(functionType);
        RegisterPool regPool = function.registerPool;
        Register i = regPool.newReg("i", typeDictionary.INT);
        Register s = regPool.newReg("s", typeDictionary.INT);
        function.code(new Instruction.Move(
                new Operand.ConstantOperand(1, typeDictionary.INT),
                new Operand.RegisterOperand(i)));
        BasicBlock b1 = function.createBlock();
        BasicBlock b2 = function.createBlock();
        BasicBlock b3 = function.createBlock();
        BasicBlock b4 = function.createBlock();
        function.jumpTo(b1);
        function.startBlock(b1);
        function.code(new Instruction.ConditionalBranch(
                function.currentBlock,
                new Operand.RegisterOperand(i),
                b2, b3));
        function.startBlock(b2);
        function.code(new Instruction.Move(
                new Operand.ConstantOperand(0, typeDictionary.INT),
                new Operand.RegisterOperand(s)));
        function.jumpTo(b3);
        function.startBlock(b3);
        function.code(new Instruction.Binary(
                "+",
                new Operand.RegisterOperand(s),
                new Operand.RegisterOperand(s),
                new Operand.RegisterOperand(i)));
        function.code(new Instruction.Binary(
                "+",
                new Operand.RegisterOperand(i),
                new Operand.RegisterOperand(i),
                new Operand.ConstantOperand(1, typeDictionary.INT)));
        function.code(new Instruction.ConditionalBranch(
                function.currentBlock,
                new Operand.RegisterOperand(i),
                b1, b4));
        function.startBlock(b4);
        function.code(new Instruction.Ret(new Operand.RegisterOperand(s)));
        function.startBlock(function.exit);
        function.isSSA = false;

        System.out.println(function.toStr(new StringBuilder(), true));

        return function;
    }

    @Test
    public void test3() {
        CompiledFunction function = buildTest3();
        function.livenessAnalysis();
        String actual = function.toStr(new StringBuilder(), true).toString();
        Assert.assertEquals("""
func foo()->Int
Reg #0 i
Reg #1 s
L0:
    i = 1
    goto  L2
    #UEVAR   = {}
    #VARKILL = {0}
    #LIVEOUT = {0, 1}
L2:
    if i goto L3 else goto L4
    #UEVAR   = {0}
    #VARKILL = {}
    #LIVEOUT = {0, 1}
L3:
    s = 0
    goto  L4
    #UEVAR   = {}
    #VARKILL = {1}
    #LIVEOUT = {0, 1}
L4:
    s = s+i
    i = i+1
    if i goto L2 else goto L5
    #UEVAR   = {0, 1}
    #VARKILL = {0, 1}
    #LIVEOUT = {0, 1}
L5:
    ret s
    goto  L1
    #UEVAR   = {1}
    #VARKILL = {}
    #LIVEOUT = {}
L1:
    #UEVAR   = {}
    #VARKILL = {}
    #LIVEOUT = {}
""", actual);
    }


}
