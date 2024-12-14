package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Symbol;
import org.junit.Assert;
import org.junit.Test;

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
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet());
                new SSATransform(functionBuilder);
                sb.append("After SSA\n");
                sb.append("=========\n");
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet());
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
    %t5 = a+b
    c = %t5
    %t6 = c+23
    a = %t6
    %t7 = a+d
    c = %t7
    goto  L1
L1:
After SSA
=========
L0:
    arg d
    a = 42
    b = a
    %t5 = a+b
    c = %t5
    %t6 = c+23
    a_1 = %t6
    %t7 = a_1+d
    c_1 = %t7
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
    %t3 = a+1
    a = %t3
    goto  L4
L4:
    %ret = a
    goto  L1
L1:
L3:
    %t4 = a-1
    a = %t4
    goto  L4
After SSA
=========
L0:
    arg d
    a = 42
    if d goto L2 else goto L3
L2:
    %t3 = a+1
    a_2 = %t3
    goto  L4
L4:
    a_3 = phi(a_2, a_1)
    %ret = a_3
    goto  L1
L1:
L3:
    %t4 = a-1
    a_1 = %t4
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
    %t3 = num>1
    if %t3 goto L3 else goto L4
L3:
    %t4 = result*num
    result = %t4
    %t5 = num-1
    num = %t5
    goto  L2
L4:
    %ret = result
    goto  L1
L1:
After SSA
=========
L0:
    arg num
    result = 1
    goto  L2
L2:
    result_1 = phi(result, result_2)
    num_1 = phi(num, num_2)
    %t3 = num_1>1
    if %t3 goto L3 else goto L4
L3:
    %t4 = result_1*num_1
    result_2 = %t4
    %t5 = num_1-1
    num_2 = %t5
    goto  L2
L4:
    %ret = result_1
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
    arg a
    arg b
    arg c
    arg d
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
    %t10 = k+1
    k = %t10
    goto  L7
L7:
    %t12 = i
    %t13 = j
    %t14 = k
    %t15 = l
    call print params %t12, %t13, %t14, %t15
    goto  L11
L11:
    if 1 goto L12 else goto L13
L12:
    if r goto L14 else goto L15
L14:
    %t16 = l+4
    l = %t16
    goto  L15
L15:
    %t17 = !s
    if %t17 goto L16 else goto L17
L16:
    goto  L13
L13:
    %t18 = i+6
    i = %t18
    %t19 = !t
    if %t19 goto L18 else goto L19
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
    %t11 = k+2
    k = %t11
    goto  L7
After SSA
=========
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
    l_1 = phi(l, l_9)
    k_1 = phi(k, k_4)
    j_1 = phi(j, j_3)
    i_1 = phi(i, i_2)
    if 1 goto L3 else goto L4
L3:
    if p goto L5 else goto L6
L5:
    j_2 = i_1
    if q goto L8 else goto L9
L8:
    l_3 = 2
    goto  L10
L10:
    l_4 = phi(l_3, l_2)
    %t10 = k_1+1
    k_3 = %t10
    goto  L7
L7:
    l_5 = phi(l_4, l_1)
    k_4 = phi(k_3, k_2)
    j_3 = phi(j_2, j_1)
    %t12 = i_1
    %t13 = j_3
    %t14 = k_4
    %t15 = l_5
    call print params %t12, %t13, %t14, %t15
    goto  L11
L11:
    l_6 = phi(l_5, l_8)
    if 1 goto L12 else goto L13
L12:
    if r goto L14 else goto L15
L14:
    %t16 = l_6+4
    l_7 = %t16
    goto  L15
L15:
    l_8 = phi(l_6, l_7)
    %t17 = !s
    if %t17 goto L16 else goto L17
L16:
    goto  L13
L13:
    l_9 = phi(l_6, l_8)
    %t18 = i_1+6
    i_2 = %t18
    %t19 = !t
    if %t19 goto L18 else goto L19
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
    %t11 = k_1+2
    k_2 = %t11
    goto  L7
""", result);
    }
}
