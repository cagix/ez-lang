package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.Type;
import com.compilerprogramming.ezlang.types.TypeDictionary;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;

public class TestSSATransform {

    String compileSrc(String src) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src);
        StringBuilder sb = new StringBuilder();
        for (Symbol s : typeDict.bindings.values()) {
            if (s instanceof Symbol.FunctionTypeSymbol f) {
                var functionBuilder = (CompiledFunction) f.code();
                sb.append("func ").append(f.name).append("\n");
                sb.append("Before SSA\n");
                sb.append("==========\n");
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet(), false);
                new EnterSSA(functionBuilder);
                sb.append("After SSA\n");
                sb.append("=========\n");
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet(), false);
                new ExitSSA(functionBuilder);
                sb.append("After exiting SSA\n");
                sb.append("=================\n");
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet(), false);
            }
        }
        return sb.toString();
    }

    @Test
    public void test1() {
        String src = """
                func foo(d: Int) {
                    var a = 42;
                    var b = a;
                    var c = a + b;
                    a = c + 23;
                    c = a + d;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    arg d
    a = 42
    b = a
    %t4 = a+b
    c = %t4
    %t5 = c+23
    a = %t5
    %t6 = a+d
    c = %t6
    goto  L1
L1:
After SSA
=========
L0:
    arg d_0
    a_0 = 42
    b_0 = a_0
    %t4_0 = a_0+b_0
    c_0 = %t4_0
    %t5_0 = c_0+23
    a_1 = %t5_0
    %t6_0 = a_1+d_0
    c_1 = %t6_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg d_0
    a_0 = 42
    b_0 = a_0
    %t4_0 = a_0+b_0
    c_0 = %t4_0
    %t5_0 = c_0+23
    a_1 = %t5_0
    %t6_0 = a_1+d_0
    c_1 = %t6_0
    goto  L1
L1:
""", result);

    }

    @Test
    public void test2() {
        String src = """
                func foo(d: Int)->Int {
                    var a = 42
                    if (d)
                    {
                      a = a + 1
                    }
                    else
                    {
                      a = a - 1
                    }
                    return a
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    arg d
    a = 42
    if d goto L2 else goto L3
L2:
    %t2 = a+1
    a = %t2
    goto  L4
L4:
    ret a
    goto  L1
L1:
L3:
    %t3 = a-1
    a = %t3
    goto  L4
After SSA
=========
L0:
    arg d_0
    a_0 = 42
    if d_0 goto L2 else goto L3
L2:
    %t2_0 = a_0+1
    a_2 = %t2_0
    goto  L4
L4:
    a_3 = phi(a_2, a_1)
    ret a_3
    goto  L1
L1:
L3:
    %t3_0 = a_0-1
    a_1 = %t3_0
    goto  L4
After exiting SSA
=================
L0:
    arg d_0
    a_0 = 42
    if d_0 goto L2 else goto L3
L2:
    %t2_0 = a_0+1
    a_2 = %t2_0
    a_3 = a_2
    goto  L4
L4:
    ret a_3
    goto  L1
L1:
L3:
    %t3_0 = a_0-1
    a_1 = %t3_0
    a_3 = a_1
    goto  L4
""", result);

    }

    @Test
    public void test3() {
        String src = """
                func factorial(num: Int)->Int {
                    var result = 1
                    while (num > 1)
                    {
                      result = result * num
                      num = num - 1
                    }
                    return result
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func factorial
Before SSA
==========
L0:
    arg num
    result = 1
    goto  L2
L2:
    %t2 = num>1
    if %t2 goto L3 else goto L4
L3:
    %t3 = result*num
    result = %t3
    %t4 = num-1
    num = %t4
    goto  L2
L4:
    ret result
    goto  L1
L1:
After SSA
=========
L0:
    arg num_0
    result_0 = 1
    goto  L2
L2:
    result_1 = phi(result_0, result_2)
    num_1 = phi(num_0, num_2)
    %t2_0 = num_1>1
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = result_1*num_1
    result_2 = %t3_0
    %t4_0 = num_1-1
    num_2 = %t4_0
    goto  L2
L4:
    ret result_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg num_0
    result_0 = 1
    result_1 = result_0
    num_1 = num_0
    goto  L2
L2:
    %t2_0 = num_1>1
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = result_1*num_1
    result_2 = %t3_0
    %t4_0 = num_1-1
    num_2 = %t4_0
    result_1 = result_2
    num_1 = num_2
    goto  L2
L4:
    ret result_1
    goto  L1
L1:
""", result);

    }

    @Test
    public void test4() {
        String src = """
                func print(a: Int, b: Int, c:Int, d:Int) {}
                func example14_66(p: Int, q: Int, r: Int, s: Int, t: Int) {
                    var i = 1
                    var j = 1
                    var k = 1
                    var l = 1
                    while (1) {
                        if (p) {
                            j = i
                            if (q) {
                                l = 2
                            }
                            else {
                                l = 3
                            }
                            k = k + 1
                        }
                        else {
                            k = k + 2
                        }
                        print(i,j,k,l)
                        while (1) {
                            if (r) {
                                l = l + 4
                            }
                            if (!s)
                                break
                        }
                        i = i + 6
                        if (!t)
                            break
                    }
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func print
Before SSA
==========
L0:
    arg a
    arg b
    arg c
    arg d
    goto  L1
L1:
After SSA
=========
L0:
    arg a_0
    arg b_0
    arg c_0
    arg d_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg a_0
    arg b_0
    arg c_0
    arg d_0
    goto  L1
L1:
func example14_66
Before SSA
==========
L0:
    arg p
    arg q
    arg r
    arg s
    arg t
    i = 1
    j = 1
    k = 1
    l = 1
    goto  L2
L2:
    if 1 goto L3 else goto L4
L3:
    if p goto L5 else goto L6
L5:
    j = i
    if q goto L8 else goto L9
L8:
    l = 2
    goto  L10
L10:
    %t9 = k+1
    k = %t9
    goto  L7
L7:
    %t11 = i
    %t12 = j
    %t13 = k
    %t14 = l
    call print params %t11, %t12, %t13, %t14
    goto  L11
L11:
    if 1 goto L12 else goto L13
L12:
    if r goto L14 else goto L15
L14:
    %t15 = l+4
    l = %t15
    goto  L15
L15:
    %t16 = !s
    if %t16 goto L16 else goto L17
L16:
    goto  L13
L13:
    %t17 = i+6
    i = %t17
    %t18 = !t
    if %t18 goto L18 else goto L19
L18:
    goto  L4
L4:
    goto  L1
L1:
L19:
    goto  L2
L17:
    goto  L11
L9:
    l = 3
    goto  L10
L6:
    %t10 = k+2
    k = %t10
    goto  L7
After SSA
=========
L0:
    arg p_0
    arg q_0
    arg r_0
    arg s_0
    arg t_0
    i_0 = 1
    j_0 = 1
    k_0 = 1
    l_0 = 1
    goto  L2
L2:
    l_1 = phi(l_0, l_9)
    k_1 = phi(k_0, k_4)
    j_1 = phi(j_0, j_3)
    i_1 = phi(i_0, i_2)
    if 1 goto L3 else goto L4
L3:
    if p_0 goto L5 else goto L6
L5:
    j_2 = i_1
    if q_0 goto L8 else goto L9
L8:
    l_3 = 2
    goto  L10
L10:
    l_4 = phi(l_3, l_2)
    %t9_0 = k_1+1
    k_3 = %t9_0
    goto  L7
L7:
    l_5 = phi(l_4, l_1)
    k_4 = phi(k_3, k_2)
    j_3 = phi(j_2, j_1)
    %t11_0 = i_1
    %t12_0 = j_3
    %t13_0 = k_4
    %t14_0 = l_5
    call print params %t11_0, %t12_0, %t13_0, %t14_0
    goto  L11
L11:
    l_6 = phi(l_5, l_8)
    if 1 goto L12 else goto L13
L12:
    if r_0 goto L14 else goto L15
L14:
    %t15_0 = l_6+4
    l_7 = %t15_0
    goto  L15
L15:
    l_8 = phi(l_6, l_7)
    %t16_0 = !s_0
    if %t16_0 goto L16 else goto L17
L16:
    goto  L13
L13:
    l_9 = phi(l_6, l_8)
    %t17_0 = i_1+6
    i_2 = %t17_0
    %t18_0 = !t_0
    if %t18_0 goto L18 else goto L19
L18:
    goto  L4
L4:
    l_10 = phi(l_1, l_9)
    k_5 = phi(k_1, k_4)
    j_4 = phi(j_1, j_3)
    i_3 = phi(i_1, i_2)
    goto  L1
L1:
L19:
    goto  L2
L17:
    goto  L11
L9:
    l_2 = 3
    goto  L10
L6:
    %t10_0 = k_1+2
    k_2 = %t10_0
    goto  L7
After exiting SSA
=================
L0:
    arg p_0
    arg q_0
    arg r_0
    arg s_0
    arg t_0
    i_0 = 1
    j_0 = 1
    k_0 = 1
    l_0 = 1
    l_1 = l_0
    k_1 = k_0
    j_1 = j_0
    i_1 = i_0
    goto  L2
L2:
    l_10 = l_1
    k_5 = k_1
    j_4 = j_1
    i_3 = i_1
    if 1 goto L3 else goto L4
L3:
    if p_0 goto L5 else goto L6
L5:
    j_2 = i_1
    if q_0 goto L8 else goto L9
L8:
    l_3 = 2
    l_4 = l_3
    goto  L10
L10:
    %t9_0 = k_1+1
    k_3 = %t9_0
    l_5 = l_4
    k_4 = k_3
    j_3 = j_2
    goto  L7
L7:
    %t11_0 = i_1
    %t12_0 = j_3
    %t13_0 = k_4
    %t14_0 = l_5
    call print params %t11_0, %t12_0, %t13_0, %t14_0
    l_6 = l_5
    goto  L11
L11:
    l_9 = l_6
    if 1 goto L12 else goto L13
L12:
    l_8 = l_6
    if r_0 goto L14 else goto L15
L14:
    %t15_0 = l_6+4
    l_7 = %t15_0
    l_8 = l_7
    goto  L15
L15:
    %t16_0 = !s_0
    if %t16_0 goto L16 else goto L17
L16:
    l_9 = l_8
    goto  L13
L13:
    %t17_0 = i_1+6
    i_2 = %t17_0
    %t18_0 = !t_0
    if %t18_0 goto L18 else goto L19
L18:
    l_10 = l_9
    k_5 = k_4
    j_4 = j_3
    i_3 = i_2
    goto  L4
L4:
    goto  L1
L1:
L19:
    l_1 = l_9
    k_1 = k_4
    j_1 = j_3
    i_1 = i_2
    goto  L2
L17:
    l_6 = l_8
    goto  L11
L9:
    l_2 = 3
    l_4 = l_2
    goto  L10
L6:
    %t10_0 = k_1+2
    k_2 = %t10_0
    l_5 = l_1
    k_4 = k_2
    j_3 = j_1
    goto  L7
""", result);
    }

    @Test
    public void test5() {
        String src = """
                func bar(arg: Int)->Int {
                    if (arg)
                        return 42;
                    return 0;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func bar
Before SSA
==========
L0:
    arg arg
    if arg goto L2 else goto L3
L2:
    ret 42
    goto  L1
L1:
L3:
    ret 0
    goto  L1
After SSA
=========
L0:
    arg arg_0
    if arg_0 goto L2 else goto L3
L2:
    ret 42
    goto  L1
L1:
L3:
    ret 0
    goto  L1
After exiting SSA
=================
L0:
    arg arg_0
    if arg_0 goto L2 else goto L3
L2:
    ret 42
    goto  L1
L1:
L3:
    ret 0
    goto  L1
""", result);
    }

    /**
     * This test case is based on the example snippet from Briggs paper
     * illustrating the lost copy problem.
     */
    static CompiledFunction buildLostCopyTest() {
        TypeDictionary typeDictionary = new TypeDictionary();
        Type.TypeFunction functionType = new Type.TypeFunction("foo");
        functionType.addArg(new Symbol.ParameterSymbol("p", typeDictionary.INT));
        functionType.setReturnType(typeDictionary.INT);
        CompiledFunction function = new CompiledFunction(functionType);
        RegisterPool regPool = function.registerPool;
        Register p = regPool.newReg("p", typeDictionary.INT);
        Register x1 = regPool.newReg("x1", typeDictionary.INT);
        function.code(new Instruction.ArgInstruction(new Operand.LocalRegisterOperand(p)));
        function.code(new Instruction.Move(
                new Operand.ConstantOperand(1, typeDictionary.INT),
                new Operand.RegisterOperand(x1)));
        BasicBlock B2 = function.createBlock();
        function.startBlock(B2);
        Register x3 = regPool.newReg("x3", typeDictionary.INT);
        Register x2 = regPool.newReg("x2", typeDictionary.INT);
        function.code(new Instruction.Phi(x2, Arrays.asList(x1, x3)));
        function.code(new Instruction.Binary("+",
                new Operand.RegisterOperand(x3),
                new Operand.RegisterOperand(x2),
                new Operand.ConstantOperand(1, typeDictionary.INT)));
        function.code(new Instruction.ConditionalBranch(B2,
                new Operand.RegisterOperand(p), B2, function.exit));
        function.startBlock(function.exit);
        function.code(new Instruction.Ret(new Operand.RegisterOperand(x2)));
        function.isSSA = true;
        return function;
    }

    @Test
    public void testLostCopyProblem() {
        CompiledFunction function = buildLostCopyTest();
        String expected = """
L0:
    arg p
    x1 = 1
    goto  L2
L2:
    x2 = phi(x1, x3)
    x3 = x2+1
    if p goto L2 else goto L1
L1:
    ret x2
""";
        Assert.assertEquals(expected, function.toStr(new StringBuilder(), false).toString());
        new ExitSSA(function);
        expected = """
L0:
    arg p
    x1 = 1
    x2 = x1
    goto  L2
L2:
    x2_4 = x2
    x3 = x2+1
    x2 = x3
    if p goto L2 else goto L1
L1:
    ret x2_4
""";
        Assert.assertEquals(expected, function.toStr(new StringBuilder(), false).toString());
    }

    /**
     * This test case is based on the example snippet from Briggs paper
     * illustrating the swap problem.
     */
    static CompiledFunction buildSwapTest() {
        TypeDictionary typeDictionary = new TypeDictionary();
        Type.TypeFunction functionType = new Type.TypeFunction("foo");
        functionType.addArg(new Symbol.ParameterSymbol("p", typeDictionary.INT));
        functionType.setReturnType(typeDictionary.VOID);
        CompiledFunction function = new CompiledFunction(functionType);
        RegisterPool regPool = function.registerPool;
        Register p = regPool.newReg("p", typeDictionary.INT);
        Register a1 = regPool.newReg("a1", typeDictionary.INT);
        Register a2 = regPool.newReg("a2", typeDictionary.INT);
        Register b1 = regPool.newReg("b1", typeDictionary.INT);
        Register b2 = regPool.newReg("b2", typeDictionary.INT);
        function.code(new Instruction.ArgInstruction(new Operand.LocalRegisterOperand(p)));
        function.code(new Instruction.Move(
                new Operand.ConstantOperand(42, typeDictionary.INT),
                new Operand.RegisterOperand(a1)));
        function.code(new Instruction.Move(
                new Operand.ConstantOperand(24, typeDictionary.INT),
                new Operand.RegisterOperand(b1)));
        BasicBlock B2 = function.createBlock();
        function.startBlock(B2);
        function.code(new Instruction.Phi(a2, Arrays.asList(a1, b2)));
        function.code(new Instruction.Phi(b2, Arrays.asList(b1, a2)));
        function.code(new Instruction.ConditionalBranch(B2,
                new Operand.RegisterOperand(p), B2, function.exit));
        function.startBlock(function.exit);
        function.isSSA = true;
        return function;
    }

    @Test
    public void testSwapProblem() {
        CompiledFunction function = buildSwapTest();
        String expected = """
L0:
    arg p
    a1 = 42
    b1 = 24
    goto  L2
L2:
    a2 = phi(a1, b2)
    b2 = phi(b1, a2)
    if p goto L2 else goto L1
L1:
""";
        Assert.assertEquals(expected, function.toStr(new StringBuilder(), false).toString());
        new ExitSSA(function);
        expected = """
L0:
    arg p
    a1 = 42
    b1 = 24
    a2 = a1
    b2 = b1
    goto  L2
L2:
    a2_5 = a2
    a2 = b2
    b2 = a2_5
    if p goto L2 else goto L1
L1:
""";
        Assert.assertEquals(expected, function.toStr(new StringBuilder(), false).toString());
    }

    @Test
    public void testLiveness() {
        String src = """
                func bar(x: Int)->Int {
                    var y = 0
                    var z = 0
                    while( x>1 ){
                        y = x/2;
                        if (y > 3) {
                            x = x-y;
                        }
                        z = x-4;
                        if (z > 0) {
                            x = x/2;
                        }
                        z = z-1;
                    }
                    return x;
                }

                func foo() {
                    return bar(10);
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testInit() {
        // see issue #16
        String src = """
                func foo(x: Int) {
                    var z: Int
                    while (x > 0) {
                        z = 5
                        if (x == 1)
                            z = z+1
                        x = x - 1
                    }
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    // http://users.csc.calpoly.edu/~akeen/courses/csc431/handouts/references/ssa_example.pdf
    @Test
    public void testSSAExample() {
        // TODO
        String src = """
func foo(x: Int, y: Int)->Int {
   var sum: Int
   
   if (x >= y)
      return 0
      
   sum = 0;
   while (x < y) {
      if (x / 2 * 2 == x) {
         sum = sum + 1
      }
      x = x + 1
   }
   return sum
}
                """;
        String result = compileSrc(src);
        System.out.println(result);

    }

    @Test
    public void testContinue() {
        String src = """
func foo(x: Int)->Int {
   var sum = 0
   var i = 0
   while (i < x) {
      if (i % 2 == 0)
        continue
      if (i / 3 == 1)
        continue
      sum = sum + 1
      i = i + 1
   }
   return sum
}
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    arg x
    sum = 0
    i = 0
    goto  L2
L2:
    %t3 = i<x
    if %t3 goto L3 else goto L4
L3:
    %t4 = i%2
    %t5 = %t4==0
    if %t5 goto L5 else goto L6
L5:
    goto  L2
L6:
    %t6 = i/3
    %t7 = %t6==1
    if %t7 goto L7 else goto L8
L7:
    goto  L2
L8:
    %t8 = sum+1
    sum = %t8
    %t9 = i+1
    i = %t9
    goto  L2
L4:
    ret sum
    goto  L1
L1:
After SSA
=========
L0:
    arg x_0
    sum_0 = 0
    i_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_1, i_1, i_2)
    sum_1 = phi(sum_0, sum_1, sum_1, sum_2)
    %t3_0 = i_1<x_0
    if %t3_0 goto L3 else goto L4
L3:
    %t4_0 = i_1%2
    %t5_0 = %t4_0==0
    if %t5_0 goto L5 else goto L6
L5:
    goto  L2
L6:
    %t6_0 = i_1/3
    %t7_0 = %t6_0==1
    if %t7_0 goto L7 else goto L8
L7:
    goto  L2
L8:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    %t9_0 = i_1+1
    i_2 = %t9_0
    goto  L2
L4:
    ret sum_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg x_0
    sum_0 = 0
    i_0 = 0
    i_1 = i_0
    sum_1 = sum_0
    goto  L2
L2:
    i_1_29 = i_1
    i_1_25 = i_1
    sum_1_27 = sum_1
    %t3_0 = i_1<x_0
    if %t3_0 goto L3 else goto L4
L3:
    %t4_0 = i_1%2
    %t5_0 = %t4_0==0
    if %t5_0 goto L5 else goto L6
L5:
    i_1_28 = i_1
    i_1 = i_1_28
    goto  L2
L6:
    %t6_0 = i_1/3
    %t7_0 = %t6_0==1
    if %t7_0 goto L7 else goto L8
L7:
    i_1_24 = i_1
    i_1 = i_1_24
    sum_1_26 = sum_1
    sum_1 = sum_1_26
    goto  L2
L8:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    %t9_0 = i_1+1
    i_2 = %t9_0
    i_1 = i_2
    sum_1 = sum_2
    goto  L2
L4:
    ret sum_1
    goto  L1
L1:                        
""",
                result);

    }

    @Test
    public void testInfiniteLoop() {
        String src = """
func foo() {
   var i = 0
   while (1) {
        if (1)
            continue;
        else 
            continue;
   }
}
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    i = 0
    goto  L2
L2:
    if 1 goto L3 else goto L4
L3:
    if 1 goto L5 else goto L6
L5:
    goto  L2
L6:
    goto  L2
L4:
    goto  L1
L1:
After SSA
=========
L0:
    i_0 = 0
    goto  L2
L2:
    if 1 goto L3 else goto L4
L3:
    if 1 goto L5 else goto L6
L5:
    goto  L2
L6:
    goto  L2
L4:
    goto  L1
L1:
After exiting SSA
=================
L0:
    i_0 = 0
    goto  L2
L2:
    if 1 goto L3 else goto L4
L3:
    if 1 goto L5 else goto L6
L5:
    goto  L2
L6:
    goto  L2
L4:
    goto  L1
L1:                
""",
        result);

    }

    @Test
    public void testParallelAssign() {
        String src = """
                func foo(n: Int)->Int {
                   var a = 1
                   var b = 2
                   
                   while (n > 0) {
                        var t = a
                        a = b
                        b = t
                        n = n - 1
                   }
                   return a
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA1() {
        String src = """
                func foo()->Int {
                    var a = 5
                    var b = 10
                    var c = a + b
                    return c
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA2() {
        String src = """
                func foo()->Int {
                    var a = 5
                    a = a + 1;
                    return a
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA3() {
        String src = """
                func foo()->Int {
                    var a = 5
                    if (a > 3) {
                        a = 10
                    } else {
                        a = 20
                    }
                    return a
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA4() {
        String src = """
                func foo()->Int {
                    var a = 0
                    while (a < 5) {
                        a = a + 1;
                    }
                    return a
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA5() {
        String src = """
                func foo()->Int {
                    var a = 0
                    var b = 10
                    if (b > 5) {
                        if (a < 5) {
                            a = 5
                        } else {
                            a = 15
                        }
                    } else {
                        a = 20
                    }
                    return a
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA6() {
        String src = """
                func foo()->Int {
                    var arr = new [Int] {1, 2};
                    arr[0] = 10
                    var x = arr[0]
                    return x
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA7() {
        String src = """
        func add(x: Int, y : Int)->Int {
            return x + y
        }
        func main()->Int {
            var a = 5
            var b = 10
            var c = add(a, b)
            return c
        }
""";
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA8() {
        String src = """
        func main()->Int {
            var a = 0
            var b = 1
            while (a < 10) {
                a = a + 2
            }
            while (b < 20) {
                b = b + 3
            }
            return a + b
        }
""";
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA9() {
        String src = """
        func main()->Int {
            var a = 0
            var b = 0
            var i = 0
            var j = 0
            while (i < 3) {
                j = 0
                while (j < 2) {
                    a = a + 1
                    i = j + 1
                }
                b = b + 1;
                i = i + 1    
            }
            return a + b
        }
""";
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA10() {
        String src = """
        func main()->Int {
            var sum = 0
            var i = 0
            var j = 0
            while (i < 5) {
                j = 0
                while (j < 5) {
                    if (j % 2 == 0) 
                        sum = sum + j
                    j = j + 1    
                }
                i = i + 1    
            }
            return sum
        }
""";
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA11() {
        String src = """
        func main()->Int {
            var a = 0
            var i = 0
            var j = 0
            while (i < 3) {
                j = 0
                while (j < 3) {
                    if (i == j) 
                        a = a + i + j
                    else if (i > j)
                        a = a - 1
                    j = j + 1        
                }
                i = i + 1    
            }
            return a
        }
""";
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA12() {
        String src = """
        func main()->Int {
            var count = 0
            var i = 0
            var j = 0
            while (i < 5) {
                j = 0
                while (j < 5) {
                    if (i + j > 5)
                        break
                    if (i == j) {
                        j = j + 1
                        continue
                    }
                    count = count + 1
                    j = j + 1
                }
                i = i + 1
            }
            return count
        }
""";
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
    public void testSSA13() {
        String src = """
        func main()->Int {
            var outerSum = 0
            var innerSum = 0
            var i = 0
            var j = 0
            while (i < 4) {
                j = 0
                while (j < 4) {
                    if ((i + j) % 2 == 0)
                        innerSum = innerSum + j
                    j = j + 1
                }
                outerSum = outerSum + innerSum
                i = i + 1
            }
            return outerSum
        }
""";
        String result = compileSrc(src);
        System.out.println(result);
    }

}