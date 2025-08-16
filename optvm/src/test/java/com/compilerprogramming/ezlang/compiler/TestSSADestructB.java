package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.EZType;
import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.TypeDictionary;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;

public class TestSSADestructB {

    static EnumSet<Options> ssaExitOptions = EnumSet.of(Options.SSA_DESTRUCTION_BOISSINOT_NOCOALESCE,Options.DUMP_SSA_TO_CSSA,Options.DUMP_CSSA_PHI_REMOVAL);

    String compileSrc(String src) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src);
        StringBuilder sb = new StringBuilder();
        for (Symbol s : typeDict.bindings.values()) {
            if (s instanceof Symbol.FunctionTypeSymbol f) {
                var functionBuilder = (CompiledFunction) f.code();
                functionBuilder.setDumpTarget(sb);
                sb.append("func ").append(f.name).append("\n");
                sb.append("Before SSA\n");
                sb.append("==========\n");
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet(), false);
                //functionBuilder.toDot(sb,false);
                new EnterSSA(functionBuilder, Options.NONE);
                //new EnterSSA(functionBuilder, EnumSet.of(DUMP_PRE_SSA_DOMFRONTIERS));
                sb.append("After SSA\n");
                sb.append("=========\n");
                BasicBlock.toStr(sb, functionBuilder.entry, new BitSet(), false);
                //functionBuilder.toDot(sb,false);
                new ExitSSA(functionBuilder, ssaExitOptions);
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
After converting from SSA to CSSA
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
After removing phis from CSSA
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
After converting from SSA to CSSA
L0:
    arg d_0
    a_0 = 42
    if d_0 goto L2 else goto L3
L2:
    %t2_0 = a_0+1
    a_2 = %t2_0
    (%t11) = (a_2)
    goto  L4
L4:
    %t13 = phi(%t11, %t12)
    (a_3) = (%t13)
    ret a_3
    goto  L1
L1:
L3:
    %t3_0 = a_0-1
    a_1 = %t3_0
    (%t12) = (a_1)
    goto  L4
After removing phis from CSSA
L0:
    arg d_0
    a_0 = 42
    if d_0 goto L2 else goto L3
L2:
    %t2_0 = a_0+1
    a_2 = %t2_0
    (%t11) = (a_2)
    %t13 = %t11
    goto  L4
L4:
    (a_3) = (%t13)
    ret a_3
    goto  L1
L1:
L3:
    %t3_0 = a_0-1
    a_1 = %t3_0
    (%t12) = (a_1)
    %t13 = %t12
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
    %t11 = a_2
    %t13 = %t11
    goto  L4
L4:
    a_3 = %t13
    ret a_3
    goto  L1
L1:
L3:
    %t3_0 = a_0-1
    a_1 = %t3_0
    %t12 = a_1
    %t13 = %t12
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
After converting from SSA to CSSA
L0:
    arg num_0
    result_0 = 1
    (%t14,%t17) = (result_0,num_0)
    goto  L2
L2:
    %t16 = phi(%t14, %t15)
    %t19 = phi(%t17, %t18)
    (result_1,num_1) = (%t16,%t19)
    %t2_0 = num_1>1
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = result_1*num_1
    result_2 = %t3_0
    %t4_0 = num_1-1
    num_2 = %t4_0
    (%t15,%t18) = (result_2,num_2)
    goto  L2
L4:
    ret result_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg num_0
    result_0 = 1
    (%t14,%t17) = (result_0,num_0)
    %t16 = %t14
    %t19 = %t17
    goto  L2
L2:
    (result_1,num_1) = (%t16,%t19)
    %t2_0 = num_1>1
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = result_1*num_1
    result_2 = %t3_0
    %t4_0 = num_1-1
    num_2 = %t4_0
    (%t15,%t18) = (result_2,num_2)
    %t16 = %t15
    %t19 = %t18
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
    %t14 = result_0
    %t17 = num_0
    %t16 = %t14
    %t19 = %t17
    goto  L2
L2:
    result_1 = %t16
    num_1 = %t19
    %t2_0 = num_1>1
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = result_1*num_1
    result_2 = %t3_0
    %t4_0 = num_1-1
    num_2 = %t4_0
    %t15 = result_2
    %t18 = num_2
    %t16 = %t15
    %t19 = %t18
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
After converting from SSA to CSSA
L0:
    arg a_0
    arg b_0
    arg c_0
    arg d_0
    goto  L1
L1:
After removing phis from CSSA
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
After converting from SSA to CSSA
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
    (%t77,%t80,%t83,%t86) = (l_0,k_0,j_0,i_0)
    goto  L2
L2:
    %t79 = phi(%t77, %t78)
    %t82 = phi(%t80, %t81)
    %t85 = phi(%t83, %t84)
    %t88 = phi(%t86, %t87)
    (l_1,k_1,j_1,i_1) = (%t79,%t82,%t85,%t88)
    if 1 goto L3 else goto L4
L3:
    if p_0 goto L5 else goto L6
L5:
    j_2 = i_1
    if q_0 goto L8 else goto L9
L8:
    l_3 = 2
    (%t74) = (l_3)
    goto  L10
L10:
    %t76 = phi(%t74, %t75)
    (l_4) = (%t76)
    %t9_0 = k_1+1
    k_3 = %t9_0
    (%t65,%t68,%t71) = (l_4,k_3,j_2)
    goto  L7
L7:
    %t67 = phi(%t65, %t66)
    %t70 = phi(%t68, %t69)
    %t73 = phi(%t71, %t72)
    (l_5,k_4,j_3) = (%t67,%t70,%t73)
    %t11_0 = i_1
    %t12_0 = j_3
    %t13_0 = k_4
    %t14_0 = l_5
    call print params %t11_0, %t12_0, %t13_0, %t14_0
    (%t62) = (l_5)
    goto  L11
L11:
    %t64 = phi(%t62, %t63)
    (l_6) = (%t64)
    (%t56) = (l_6)
    if 1 goto L12 else goto L13
L12:
    (%t59) = (l_6)
    if r_0 goto L14 else goto L15
L14:
    %t15_0 = l_6+4
    l_7 = %t15_0
    (%t60) = (l_7)
    goto  L15
L15:
    %t61 = phi(%t59, %t60)
    (l_8) = (%t61)
    %t16_0 = !s_0
    if %t16_0 goto L16 else goto L17
L16:
    (%t57) = (l_8)
    goto  L13
L13:
    %t58 = phi(%t56, %t57)
    (l_9) = (%t58)
    %t17_0 = i_1+6
    i_2 = %t17_0
    %t18_0 = !t_0
    if %t18_0 goto L18 else goto L19
L18:
    goto  L4
L4:
    goto  L1
L1:
L19:
    (%t78,%t81,%t84,%t87) = (l_9,k_4,j_3,i_2)
    goto  L2
L17:
    (%t63) = (l_8)
    goto  L11
L9:
    l_2 = 3
    (%t75) = (l_2)
    goto  L10
L6:
    %t10_0 = k_1+2
    k_2 = %t10_0
    (%t66,%t69,%t72) = (l_1,k_2,j_1)
    goto  L7
After removing phis from CSSA
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
    (%t77,%t80,%t83,%t86) = (l_0,k_0,j_0,i_0)
    %t79 = %t77
    %t82 = %t80
    %t85 = %t83
    %t88 = %t86
    goto  L2
L2:
    (l_1,k_1,j_1,i_1) = (%t79,%t82,%t85,%t88)
    if 1 goto L3 else goto L4
L3:
    if p_0 goto L5 else goto L6
L5:
    j_2 = i_1
    if q_0 goto L8 else goto L9
L8:
    l_3 = 2
    (%t74) = (l_3)
    %t76 = %t74
    goto  L10
L10:
    (l_4) = (%t76)
    %t9_0 = k_1+1
    k_3 = %t9_0
    (%t65,%t68,%t71) = (l_4,k_3,j_2)
    %t67 = %t65
    %t70 = %t68
    %t73 = %t71
    goto  L7
L7:
    (l_5,k_4,j_3) = (%t67,%t70,%t73)
    %t11_0 = i_1
    %t12_0 = j_3
    %t13_0 = k_4
    %t14_0 = l_5
    call print params %t11_0, %t12_0, %t13_0, %t14_0
    (%t62) = (l_5)
    %t64 = %t62
    goto  L11
L11:
    (l_6) = (%t64)
    (%t56) = (l_6)
    %t58 = %t56
    if 1 goto L12 else goto L13
L12:
    (%t59) = (l_6)
    %t61 = %t59
    if r_0 goto L14 else goto L15
L14:
    %t15_0 = l_6+4
    l_7 = %t15_0
    (%t60) = (l_7)
    %t61 = %t60
    goto  L15
L15:
    (l_8) = (%t61)
    %t16_0 = !s_0
    if %t16_0 goto L16 else goto L17
L16:
    (%t57) = (l_8)
    %t58 = %t57
    goto  L13
L13:
    (l_9) = (%t58)
    %t17_0 = i_1+6
    i_2 = %t17_0
    %t18_0 = !t_0
    if %t18_0 goto L18 else goto L19
L18:
    goto  L4
L4:
    goto  L1
L1:
L19:
    (%t78,%t81,%t84,%t87) = (l_9,k_4,j_3,i_2)
    %t79 = %t78
    %t82 = %t81
    %t85 = %t84
    %t88 = %t87
    goto  L2
L17:
    (%t63) = (l_8)
    %t64 = %t63
    goto  L11
L9:
    l_2 = 3
    (%t75) = (l_2)
    %t76 = %t75
    goto  L10
L6:
    %t10_0 = k_1+2
    k_2 = %t10_0
    (%t66,%t69,%t72) = (l_1,k_2,j_1)
    %t67 = %t66
    %t70 = %t69
    %t73 = %t72
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
    %t77 = l_0
    %t80 = k_0
    %t83 = j_0
    %t86 = i_0
    %t79 = %t77
    %t82 = %t80
    %t85 = %t83
    %t88 = %t86
    goto  L2
L2:
    l_1 = %t79
    k_1 = %t82
    j_1 = %t85
    i_1 = %t88
    if 1 goto L3 else goto L4
L3:
    if p_0 goto L5 else goto L6
L5:
    j_2 = i_1
    if q_0 goto L8 else goto L9
L8:
    l_3 = 2
    %t74 = l_3
    %t76 = %t74
    goto  L10
L10:
    l_4 = %t76
    %t9_0 = k_1+1
    k_3 = %t9_0
    %t65 = l_4
    %t68 = k_3
    %t71 = j_2
    %t67 = %t65
    %t70 = %t68
    %t73 = %t71
    goto  L7
L7:
    l_5 = %t67
    k_4 = %t70
    j_3 = %t73
    %t11_0 = i_1
    %t12_0 = j_3
    %t13_0 = k_4
    %t14_0 = l_5
    call print params %t11_0, %t12_0, %t13_0, %t14_0
    %t62 = l_5
    %t64 = %t62
    goto  L11
L11:
    l_6 = %t64
    %t56 = l_6
    %t58 = %t56
    if 1 goto L12 else goto L13
L12:
    %t59 = l_6
    %t61 = %t59
    if r_0 goto L14 else goto L15
L14:
    %t15_0 = l_6+4
    l_7 = %t15_0
    %t60 = l_7
    %t61 = %t60
    goto  L15
L15:
    l_8 = %t61
    %t16_0 = !s_0
    if %t16_0 goto L16 else goto L17
L16:
    %t57 = l_8
    %t58 = %t57
    goto  L13
L13:
    l_9 = %t58
    %t17_0 = i_1+6
    i_2 = %t17_0
    %t18_0 = !t_0
    if %t18_0 goto L18 else goto L19
L18:
    goto  L4
L4:
    goto  L1
L1:
L19:
    %t78 = l_9
    %t81 = k_4
    %t84 = j_3
    %t87 = i_2
    %t79 = %t78
    %t82 = %t81
    %t85 = %t84
    %t88 = %t87
    goto  L2
L17:
    %t63 = l_8
    %t64 = %t63
    goto  L11
L9:
    l_2 = 3
    %t75 = l_2
    %t76 = %t75
    goto  L10
L6:
    %t10_0 = k_1+2
    k_2 = %t10_0
    %t66 = l_1
    %t69 = k_2
    %t72 = j_1
    %t67 = %t66
    %t70 = %t69
    %t73 = %t72
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
After converting from SSA to CSSA
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
After removing phis from CSSA
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
        EZType.EZTypeFunction functionType = new EZType.EZTypeFunction("foo");
        var argSymbol = new Symbol.ParameterSymbol("p", typeDictionary.INT);
        functionType.addArg(argSymbol);
        functionType.setReturnType(typeDictionary.INT);
        CompiledFunction function = new CompiledFunction(functionType, typeDictionary);
        RegisterPool regPool = function.registerPool;
        Register p = regPool.newReg("p", typeDictionary.INT);
        Register x1 = regPool.newReg("x1", typeDictionary.INT);
        function.code(new Instruction.ArgInstruction(new Operand.LocalRegisterOperand(p, argSymbol)));
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
        function.currentBlock.addSuccessor(B2);
        function.currentBlock.addSuccessor(function.exit);
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
        var sb = new StringBuilder();
        function.setDumpTarget(sb);
        new ExitSSA(function, ssaExitOptions);
        expected = """
After converting from SSA to CSSA
L0:
    arg p
    x1 = 1
    (%t4) = (x1)
    goto  L2
L2:
    %t6 = phi(%t4, %t5)
    (x2) = (%t6)
    x3 = x2+1
    (%t5) = (x3)
    if p goto L2 else goto L1
L1:
    ret x2
After removing phis from CSSA
L0:
    arg p
    x1 = 1
    (%t4) = (x1)
    %t6 = %t4
    goto  L2
L2:
    (x2) = (%t6)
    x3 = x2+1
    (%t5) = (x3)
    %t6 = %t5
    if p goto L2 else goto L1
L1:
    ret x2
After sequencing parallel copies
L0:
    arg p
    x1 = 1
    %t4 = x1
    %t6 = %t4
    goto  L2
L2:
    x2 = %t6
    x3 = x2+1
    %t5 = x3
    %t6 = %t5
    if p goto L2 else goto L1
L1:
    ret x2
""";
        sb.append("After sequencing parallel copies\n");
        Assert.assertEquals(expected, function.toStr(sb, false).toString());
    }

    /**
     * This test case is based on the example snippet from Briggs paper
     * illustrating the swap problem.
     */
    static CompiledFunction buildSwapTest() {
        TypeDictionary typeDictionary = new TypeDictionary();
        EZType.EZTypeFunction functionType = new EZType.EZTypeFunction("foo");
        var argSymbol = new Symbol.ParameterSymbol("p", typeDictionary.INT);
        functionType.addArg(argSymbol);
        functionType.setReturnType(typeDictionary.VOID);
        CompiledFunction function = new CompiledFunction(functionType, typeDictionary);
        RegisterPool regPool = function.registerPool;
        Register p = regPool.newReg("p", typeDictionary.INT);
        Register a1 = regPool.newReg("a1", typeDictionary.INT);
        Register a2 = regPool.newReg("a2", typeDictionary.INT);
        Register b1 = regPool.newReg("b1", typeDictionary.INT);
        Register b2 = regPool.newReg("b2", typeDictionary.INT);
        function.code(new Instruction.ArgInstruction(new Operand.LocalRegisterOperand(p, argSymbol)));
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
        function.currentBlock.addSuccessor(B2);
        function.currentBlock.addSuccessor(function.exit);
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
        var sb = new StringBuilder();
        function.setDumpTarget(sb);
        new ExitSSA(function, ssaExitOptions);
        expected = """
After converting from SSA to CSSA
L0:
    arg p
    a1 = 42
    b1 = 24
    (%t5,%t8) = (a1,b1)
    goto  L2
L2:
    %t7 = phi(%t5, %t6)
    %t10 = phi(%t8, %t9)
    (a2,b2) = (%t7,%t10)
    (%t6,%t9) = (b2,a2)
    if p goto L2 else goto L1
L1:
After removing phis from CSSA
L0:
    arg p
    a1 = 42
    b1 = 24
    (%t5,%t8) = (a1,b1)
    %t7 = %t5
    %t10 = %t8
    goto  L2
L2:
    (a2,b2) = (%t7,%t10)
    (%t6,%t9) = (b2,a2)
    %t7 = %t6
    %t10 = %t9
    if p goto L2 else goto L1
L1:
After sequencing parallel copies
L0:
    arg p
    a1 = 42
    b1 = 24
    %t5 = a1
    %t8 = b1
    %t7 = %t5
    %t10 = %t8
    goto  L2
L2:
    a2 = %t7
    b2 = %t10
    %t6 = b2
    %t9 = a2
    %t7 = %t6
    %t10 = %t9
    if p goto L2 else goto L1
L1:
""";
        sb.append("After sequencing parallel copies\n");
        Assert.assertEquals(expected, function.toStr(sb, false).toString());
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

                func foo()->Int {
                    return bar(10);
                }
                """;
        String result = compileSrc(src);
        System.out.println(result);
    }

    @Test
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    arg x
    goto  L2
L2:
    %t2 = x>0
    if %t2 goto L3 else goto L4
L3:
    z = 5
    %t3 = x==1
    if %t3 goto L5 else goto L6
L5:
    %t4 = z+1
    z = %t4
    goto  L6
L6:
    %t5 = x-1
    x = %t5
    goto  L2
L4:
    goto  L1
L1:
After SSA
=========
L0:
    arg x_0
    goto  L2
L2:
    x_1 = phi(x_0, x_2)
    %t2_0 = x_1>0
    if %t2_0 goto L3 else goto L4
L3:
    z_0 = 5
    %t3_0 = x_1==1
    if %t3_0 goto L5 else goto L6
L5:
    %t4_0 = z_0+1
    z_1 = %t4_0
    goto  L6
L6:
    %t5_0 = x_1-1
    x_2 = %t5_0
    goto  L2
L4:
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    arg x_0
    (%t15) = (x_0)
    goto  L2
L2:
    %t17 = phi(%t15, %t16)
    (x_1) = (%t17)
    %t2_0 = x_1>0
    if %t2_0 goto L3 else goto L4
L3:
    z_0 = 5
    %t3_0 = x_1==1
    if %t3_0 goto L5 else goto L6
L5:
    %t4_0 = z_0+1
    z_1 = %t4_0
    goto  L6
L6:
    %t5_0 = x_1-1
    x_2 = %t5_0
    (%t16) = (x_2)
    goto  L2
L4:
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg x_0
    (%t15) = (x_0)
    %t17 = %t15
    goto  L2
L2:
    (x_1) = (%t17)
    %t2_0 = x_1>0
    if %t2_0 goto L3 else goto L4
L3:
    z_0 = 5
    %t3_0 = x_1==1
    if %t3_0 goto L5 else goto L6
L5:
    %t4_0 = z_0+1
    z_1 = %t4_0
    goto  L6
L6:
    %t5_0 = x_1-1
    x_2 = %t5_0
    (%t16) = (x_2)
    %t17 = %t16
    goto  L2
L4:
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg x_0
    %t15 = x_0
    %t17 = %t15
    goto  L2
L2:
    x_1 = %t17
    %t2_0 = x_1>0
    if %t2_0 goto L3 else goto L4
L3:
    z_0 = 5
    %t3_0 = x_1==1
    if %t3_0 goto L5 else goto L6
L5:
    %t4_0 = z_0+1
    z_1 = %t4_0
    goto  L6
L6:
    %t5_0 = x_1-1
    x_2 = %t5_0
    %t16 = x_2
    %t17 = %t16
    goto  L2
L4:
    goto  L1
L1:
""", result);
    }

    // http://users.csc.calpoly.edu/~akeen/courses/csc431/handouts/references/ssa_example.pdf
    @Test
    public void testSSAExample() {
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    arg x
    arg y
    %t3 = x>=y
    if %t3 goto L2 else goto L3
L2:
    ret 0
    goto  L1
L1:
L3:
    sum = 0
    goto  L4
L4:
    %t4 = x<y
    if %t4 goto L5 else goto L6
L5:
    %t5 = x/2
    %t6 = %t5*2
    %t7 = %t6==x
    if %t7 goto L7 else goto L8
L7:
    %t8 = sum+1
    sum = %t8
    goto  L8
L8:
    %t9 = x+1
    x = %t9
    goto  L4
L6:
    ret sum
    goto  L1
After SSA
=========
L0:
    arg x_0
    arg y_0
    %t3_0 = x_0>=y_0
    if %t3_0 goto L2 else goto L3
L2:
    ret 0
    goto  L1
L1:
L3:
    sum_0 = 0
    goto  L4
L4:
    sum_1 = phi(sum_0, sum_3)
    x_1 = phi(x_0, x_2)
    %t4_0 = x_1<y_0
    if %t4_0 goto L5 else goto L6
L5:
    %t5_0 = x_1/2
    %t6_0 = %t5_0*2
    %t7_0 = %t6_0==x_1
    if %t7_0 goto L7 else goto L8
L7:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    goto  L8
L8:
    sum_3 = phi(sum_1, sum_2)
    %t9_0 = x_1+1
    x_2 = %t9_0
    goto  L4
L6:
    ret sum_1
    goto  L1
After converting from SSA to CSSA
L0:
    arg x_0
    arg y_0
    %t3_0 = x_0>=y_0
    if %t3_0 goto L2 else goto L3
L2:
    ret 0
    goto  L1
L1:
L3:
    sum_0 = 0
    (%t28,%t31) = (sum_0,x_0)
    goto  L4
L4:
    %t30 = phi(%t28, %t29)
    %t33 = phi(%t31, %t32)
    (sum_1,x_1) = (%t30,%t33)
    %t4_0 = x_1<y_0
    if %t4_0 goto L5 else goto L6
L5:
    %t5_0 = x_1/2
    %t6_0 = %t5_0*2
    %t7_0 = %t6_0==x_1
    (%t25) = (sum_1)
    if %t7_0 goto L7 else goto L8
L7:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    (%t26) = (sum_2)
    goto  L8
L8:
    %t27 = phi(%t25, %t26)
    (sum_3) = (%t27)
    %t9_0 = x_1+1
    x_2 = %t9_0
    (%t29,%t32) = (sum_3,x_2)
    goto  L4
L6:
    ret sum_1
    goto  L1
After removing phis from CSSA
L0:
    arg x_0
    arg y_0
    %t3_0 = x_0>=y_0
    if %t3_0 goto L2 else goto L3
L2:
    ret 0
    goto  L1
L1:
L3:
    sum_0 = 0
    (%t28,%t31) = (sum_0,x_0)
    %t30 = %t28
    %t33 = %t31
    goto  L4
L4:
    (sum_1,x_1) = (%t30,%t33)
    %t4_0 = x_1<y_0
    if %t4_0 goto L5 else goto L6
L5:
    %t5_0 = x_1/2
    %t6_0 = %t5_0*2
    %t7_0 = %t6_0==x_1
    (%t25) = (sum_1)
    %t27 = %t25
    if %t7_0 goto L7 else goto L8
L7:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    (%t26) = (sum_2)
    %t27 = %t26
    goto  L8
L8:
    (sum_3) = (%t27)
    %t9_0 = x_1+1
    x_2 = %t9_0
    (%t29,%t32) = (sum_3,x_2)
    %t30 = %t29
    %t33 = %t32
    goto  L4
L6:
    ret sum_1
    goto  L1
After exiting SSA
=================
L0:
    arg x_0
    arg y_0
    %t3_0 = x_0>=y_0
    if %t3_0 goto L2 else goto L3
L2:
    ret 0
    goto  L1
L1:
L3:
    sum_0 = 0
    %t28 = sum_0
    %t31 = x_0
    %t30 = %t28
    %t33 = %t31
    goto  L4
L4:
    sum_1 = %t30
    x_1 = %t33
    %t4_0 = x_1<y_0
    if %t4_0 goto L5 else goto L6
L5:
    %t5_0 = x_1/2
    %t6_0 = %t5_0*2
    %t7_0 = %t6_0==x_1
    %t25 = sum_1
    %t27 = %t25
    if %t7_0 goto L7 else goto L8
L7:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    %t26 = sum_2
    %t27 = %t26
    goto  L8
L8:
    sum_3 = %t27
    %t9_0 = x_1+1
    x_2 = %t9_0
    %t29 = sum_3
    %t32 = x_2
    %t30 = %t29
    %t33 = %t32
    goto  L4
L6:
    ret sum_1
    goto  L1
""", result);
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
After converting from SSA to CSSA
L0:
    arg x_0
    sum_0 = 0
    i_0 = 0
    (%t24,%t29) = (i_0,sum_0)
    goto  L2
L2:
    %t28 = phi(%t24, %t25, %t26, %t27)
    %t33 = phi(%t29, %t30, %t31, %t32)
    (i_1,sum_1) = (%t28,%t33)
    %t3_0 = i_1<x_0
    if %t3_0 goto L3 else goto L4
L3:
    %t4_0 = i_1%2
    %t5_0 = %t4_0==0
    if %t5_0 goto L5 else goto L6
L5:
    (%t25,%t30) = (i_1,sum_1)
    goto  L2
L6:
    %t6_0 = i_1/3
    %t7_0 = %t6_0==1
    if %t7_0 goto L7 else goto L8
L7:
    (%t26,%t31) = (i_1,sum_1)
    goto  L2
L8:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t27,%t32) = (i_2,sum_2)
    goto  L2
L4:
    ret sum_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg x_0
    sum_0 = 0
    i_0 = 0
    (%t24,%t29) = (i_0,sum_0)
    %t28 = %t24
    %t33 = %t29
    goto  L2
L2:
    (i_1,sum_1) = (%t28,%t33)
    %t3_0 = i_1<x_0
    if %t3_0 goto L3 else goto L4
L3:
    %t4_0 = i_1%2
    %t5_0 = %t4_0==0
    if %t5_0 goto L5 else goto L6
L5:
    (%t25,%t30) = (i_1,sum_1)
    %t28 = %t25
    %t33 = %t30
    goto  L2
L6:
    %t6_0 = i_1/3
    %t7_0 = %t6_0==1
    if %t7_0 goto L7 else goto L8
L7:
    (%t26,%t31) = (i_1,sum_1)
    %t28 = %t26
    %t33 = %t31
    goto  L2
L8:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t27,%t32) = (i_2,sum_2)
    %t28 = %t27
    %t33 = %t32
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
    %t24 = i_0
    %t29 = sum_0
    %t28 = %t24
    %t33 = %t29
    goto  L2
L2:
    i_1 = %t28
    sum_1 = %t33
    %t3_0 = i_1<x_0
    if %t3_0 goto L3 else goto L4
L3:
    %t4_0 = i_1%2
    %t5_0 = %t4_0==0
    if %t5_0 goto L5 else goto L6
L5:
    %t25 = i_1
    %t30 = sum_1
    %t28 = %t25
    %t33 = %t30
    goto  L2
L6:
    %t6_0 = i_1/3
    %t7_0 = %t6_0==1
    if %t7_0 goto L7 else goto L8
L7:
    %t26 = i_1
    %t31 = sum_1
    %t28 = %t26
    %t33 = %t31
    goto  L2
L8:
    %t8_0 = sum_1+1
    sum_2 = %t8_0
    %t9_0 = i_1+1
    i_2 = %t9_0
    %t27 = i_2
    %t32 = sum_2
    %t28 = %t27
    %t33 = %t32
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
After converting from SSA to CSSA
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
After removing phis from CSSA
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    arg n
    a = 1
    b = 2
    goto  L2
L2:
    %t4 = n>0
    if %t4 goto L3 else goto L4
L3:
    t = a
    a = b
    b = t
    %t5 = n-1
    n = %t5
    goto  L2
L4:
    ret a
    goto  L1
L1:
After SSA
=========
L0:
    arg n_0
    a_0 = 1
    b_0 = 2
    goto  L2
L2:
    b_1 = phi(b_0, b_2)
    a_1 = phi(a_0, a_2)
    n_1 = phi(n_0, n_2)
    %t4_0 = n_1>0
    if %t4_0 goto L3 else goto L4
L3:
    t_0 = a_1
    a_2 = b_1
    b_2 = t_0
    %t5_0 = n_1-1
    n_2 = %t5_0
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    arg n_0
    a_0 = 1
    b_0 = 2
    (%t18,%t21,%t24) = (b_0,a_0,n_0)
    goto  L2
L2:
    %t20 = phi(%t18, %t19)
    %t23 = phi(%t21, %t22)
    %t26 = phi(%t24, %t25)
    (b_1,a_1,n_1) = (%t20,%t23,%t26)
    %t4_0 = n_1>0
    if %t4_0 goto L3 else goto L4
L3:
    t_0 = a_1
    a_2 = b_1
    b_2 = t_0
    %t5_0 = n_1-1
    n_2 = %t5_0
    (%t19,%t22,%t25) = (b_2,a_2,n_2)
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg n_0
    a_0 = 1
    b_0 = 2
    (%t18,%t21,%t24) = (b_0,a_0,n_0)
    %t20 = %t18
    %t23 = %t21
    %t26 = %t24
    goto  L2
L2:
    (b_1,a_1,n_1) = (%t20,%t23,%t26)
    %t4_0 = n_1>0
    if %t4_0 goto L3 else goto L4
L3:
    t_0 = a_1
    a_2 = b_1
    b_2 = t_0
    %t5_0 = n_1-1
    n_2 = %t5_0
    (%t19,%t22,%t25) = (b_2,a_2,n_2)
    %t20 = %t19
    %t23 = %t22
    %t26 = %t25
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg n_0
    a_0 = 1
    b_0 = 2
    %t18 = b_0
    %t21 = a_0
    %t24 = n_0
    %t20 = %t18
    %t23 = %t21
    %t26 = %t24
    goto  L2
L2:
    b_1 = %t20
    a_1 = %t23
    n_1 = %t26
    %t4_0 = n_1>0
    if %t4_0 goto L3 else goto L4
L3:
    t_0 = a_1
    a_2 = b_1
    b_2 = t_0
    %t5_0 = n_1-1
    n_2 = %t5_0
    %t19 = b_2
    %t22 = a_2
    %t25 = n_2
    %t20 = %t19
    %t23 = %t22
    %t26 = %t25
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    a = 5
    b = 10
    %t3 = a+b
    c = %t3
    ret c
    goto  L1
L1:
After SSA
=========
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0+b_0
    c_0 = %t3_0
    ret c_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0+b_0
    c_0 = %t3_0
    ret c_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0+b_0
    c_0 = %t3_0
    ret c_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0+b_0
    c_0 = %t3_0
    ret c_0
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    a = 5
    %t1 = a+1
    a = %t1
    ret a
    goto  L1
L1:
After SSA
=========
L0:
    a_0 = 5
    %t1_0 = a_0+1
    a_1 = %t1_0
    ret a_1
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    a_0 = 5
    %t1_0 = a_0+1
    a_1 = %t1_0
    ret a_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    a_0 = 5
    %t1_0 = a_0+1
    a_1 = %t1_0
    ret a_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    a_0 = 5
    %t1_0 = a_0+1
    a_1 = %t1_0
    ret a_1
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    a = 5
    %t1 = a>3
    if %t1 goto L2 else goto L3
L2:
    a = 10
    goto  L4
L4:
    ret a
    goto  L1
L1:
L3:
    a = 20
    goto  L4
After SSA
=========
L0:
    a_0 = 5
    %t1_0 = a_0>3
    if %t1_0 goto L2 else goto L3
L2:
    a_2 = 10
    goto  L4
L4:
    a_3 = phi(a_2, a_1)
    ret a_3
    goto  L1
L1:
L3:
    a_1 = 20
    goto  L4
After converting from SSA to CSSA
L0:
    a_0 = 5
    %t1_0 = a_0>3
    if %t1_0 goto L2 else goto L3
L2:
    a_2 = 10
    (%t7) = (a_2)
    goto  L4
L4:
    %t9 = phi(%t7, %t8)
    (a_3) = (%t9)
    ret a_3
    goto  L1
L1:
L3:
    a_1 = 20
    (%t8) = (a_1)
    goto  L4
After removing phis from CSSA
L0:
    a_0 = 5
    %t1_0 = a_0>3
    if %t1_0 goto L2 else goto L3
L2:
    a_2 = 10
    (%t7) = (a_2)
    %t9 = %t7
    goto  L4
L4:
    (a_3) = (%t9)
    ret a_3
    goto  L1
L1:
L3:
    a_1 = 20
    (%t8) = (a_1)
    %t9 = %t8
    goto  L4
After exiting SSA
=================
L0:
    a_0 = 5
    %t1_0 = a_0>3
    if %t1_0 goto L2 else goto L3
L2:
    a_2 = 10
    %t7 = a_2
    %t9 = %t7
    goto  L4
L4:
    a_3 = %t9
    ret a_3
    goto  L1
L1:
L3:
    a_1 = 20
    %t8 = a_1
    %t9 = %t8
    goto  L4
""", result);
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    a = 0
    goto  L2
L2:
    %t1 = a<5
    if %t1 goto L3 else goto L4
L3:
    %t2 = a+1
    a = %t2
    goto  L2
L4:
    ret a
    goto  L1
L1:
After SSA
=========
L0:
    a_0 = 0
    goto  L2
L2:
    a_1 = phi(a_0, a_2)
    %t1_0 = a_1<5
    if %t1_0 goto L3 else goto L4
L3:
    %t2_0 = a_1+1
    a_2 = %t2_0
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    a_0 = 0
    (%t8) = (a_0)
    goto  L2
L2:
    %t10 = phi(%t8, %t9)
    (a_1) = (%t10)
    %t1_0 = a_1<5
    if %t1_0 goto L3 else goto L4
L3:
    %t2_0 = a_1+1
    a_2 = %t2_0
    (%t9) = (a_2)
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    a_0 = 0
    (%t8) = (a_0)
    %t10 = %t8
    goto  L2
L2:
    (a_1) = (%t10)
    %t1_0 = a_1<5
    if %t1_0 goto L3 else goto L4
L3:
    %t2_0 = a_1+1
    a_2 = %t2_0
    (%t9) = (a_2)
    %t10 = %t9
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    a_0 = 0
    %t8 = a_0
    %t10 = %t8
    goto  L2
L2:
    a_1 = %t10
    %t1_0 = a_1<5
    if %t1_0 goto L3 else goto L4
L3:
    %t2_0 = a_1+1
    a_2 = %t2_0
    %t9 = a_2
    %t10 = %t9
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    a = 0
    b = 10
    %t2 = b>5
    if %t2 goto L2 else goto L3
L2:
    %t3 = a<5
    if %t3 goto L5 else goto L6
L5:
    a = 5
    goto  L7
L7:
    goto  L4
L4:
    ret a
    goto  L1
L1:
L6:
    a = 15
    goto  L7
L3:
    a = 20
    goto  L4
After SSA
=========
L0:
    a_0 = 0
    b_0 = 10
    %t2_0 = b_0>5
    if %t2_0 goto L2 else goto L3
L2:
    %t3_0 = a_0<5
    if %t3_0 goto L5 else goto L6
L5:
    a_3 = 5
    goto  L7
L7:
    a_4 = phi(a_3, a_2)
    goto  L4
L4:
    a_5 = phi(a_4, a_1)
    ret a_5
    goto  L1
L1:
L6:
    a_2 = 15
    goto  L7
L3:
    a_1 = 20
    goto  L4
After converting from SSA to CSSA
L0:
    a_0 = 0
    b_0 = 10
    %t2_0 = b_0>5
    if %t2_0 goto L2 else goto L3
L2:
    %t3_0 = a_0<5
    if %t3_0 goto L5 else goto L6
L5:
    a_3 = 5
    (%t16) = (a_3)
    goto  L7
L7:
    %t18 = phi(%t16, %t17)
    (a_4) = (%t18)
    (%t13) = (a_4)
    goto  L4
L4:
    %t15 = phi(%t13, %t14)
    (a_5) = (%t15)
    ret a_5
    goto  L1
L1:
L6:
    a_2 = 15
    (%t17) = (a_2)
    goto  L7
L3:
    a_1 = 20
    (%t14) = (a_1)
    goto  L4
After removing phis from CSSA
L0:
    a_0 = 0
    b_0 = 10
    %t2_0 = b_0>5
    if %t2_0 goto L2 else goto L3
L2:
    %t3_0 = a_0<5
    if %t3_0 goto L5 else goto L6
L5:
    a_3 = 5
    (%t16) = (a_3)
    %t18 = %t16
    goto  L7
L7:
    (a_4) = (%t18)
    (%t13) = (a_4)
    %t15 = %t13
    goto  L4
L4:
    (a_5) = (%t15)
    ret a_5
    goto  L1
L1:
L6:
    a_2 = 15
    (%t17) = (a_2)
    %t18 = %t17
    goto  L7
L3:
    a_1 = 20
    (%t14) = (a_1)
    %t15 = %t14
    goto  L4
After exiting SSA
=================
L0:
    a_0 = 0
    b_0 = 10
    %t2_0 = b_0>5
    if %t2_0 goto L2 else goto L3
L2:
    %t3_0 = a_0<5
    if %t3_0 goto L5 else goto L6
L5:
    a_3 = 5
    %t16 = a_3
    %t18 = %t16
    goto  L7
L7:
    a_4 = %t18
    %t13 = a_4
    %t15 = %t13
    goto  L4
L4:
    a_5 = %t15
    ret a_5
    goto  L1
L1:
L6:
    a_2 = 15
    %t17 = a_2
    %t18 = %t17
    goto  L7
L3:
    a_1 = 20
    %t14 = a_1
    %t15 = %t14
    goto  L4
""", result);
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
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    %t2 = New([Int], len=2)
    %t2[0] = 1
    %t2[1] = 2
    arr = %t2
    arr[0] = 10
    %t3 = arr[0]
    x = %t3
    ret x
    goto  L1
L1:
After SSA
=========
L0:
    %t2_0 = New([Int], len=2)
    %t2_0[0] = 1
    %t2_0[1] = 2
    arr_0 = %t2_0
    arr_0[0] = 10
    %t3_0 = arr_0[0]
    x_0 = %t3_0
    ret x_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    %t2_0 = New([Int], len=2)
    %t2_0[0] = 1
    %t2_0[1] = 2
    arr_0 = %t2_0
    arr_0[0] = 10
    %t3_0 = arr_0[0]
    x_0 = %t3_0
    ret x_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    %t2_0 = New([Int], len=2)
    %t2_0[0] = 1
    %t2_0[1] = 2
    arr_0 = %t2_0
    arr_0[0] = 10
    %t3_0 = arr_0[0]
    x_0 = %t3_0
    ret x_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    %t2_0 = New([Int], len=2)
    %t2_0[0] = 1
    %t2_0[1] = 2
    arr_0 = %t2_0
    arr_0[0] = 10
    %t3_0 = arr_0[0]
    x_0 = %t3_0
    ret x_0
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func add
Before SSA
==========
L0:
    arg x
    arg y
    %t2 = x+y
    ret %t2
    goto  L1
L1:
After SSA
=========
L0:
    arg x_0
    arg y_0
    %t2_0 = x_0+y_0
    ret %t2_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    arg x_0
    arg y_0
    %t2_0 = x_0+y_0
    ret %t2_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg x_0
    arg y_0
    %t2_0 = x_0+y_0
    ret %t2_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg x_0
    arg y_0
    %t2_0 = x_0+y_0
    ret %t2_0
    goto  L1
L1:
func main
Before SSA
==========
L0:
    a = 5
    b = 10
    %t3 = a
    %t4 = b
    %t5 = call add params %t3, %t4
    c = %t5
    ret c
    goto  L1
L1:
After SSA
=========
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0
    %t4_0 = b_0
    %t5_0 = call add params %t3_0, %t4_0
    c_0 = %t5_0
    ret c_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0
    %t4_0 = b_0
    %t5_0 = call add params %t3_0, %t4_0
    c_0 = %t5_0
    ret c_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0
    %t4_0 = b_0
    %t5_0 = call add params %t3_0, %t4_0
    c_0 = %t5_0
    ret c_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    a_0 = 5
    b_0 = 10
    %t3_0 = a_0
    %t4_0 = b_0
    %t5_0 = call add params %t3_0, %t4_0
    c_0 = %t5_0
    ret c_0
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func main
Before SSA
==========
L0:
    a = 0
    b = 1
    goto  L2
L2:
    %t2 = a<10
    if %t2 goto L3 else goto L4
L3:
    %t3 = a+2
    a = %t3
    goto  L2
L4:
    goto  L5
L5:
    %t4 = b<20
    if %t4 goto L6 else goto L7
L6:
    %t5 = b+3
    b = %t5
    goto  L5
L7:
    %t6 = a+b
    ret %t6
    goto  L1
L1:
After SSA
=========
L0:
    a_0 = 0
    b_0 = 1
    goto  L2
L2:
    a_1 = phi(a_0, a_2)
    %t2_0 = a_1<10
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = a_1+2
    a_2 = %t3_0
    goto  L2
L4:
    goto  L5
L5:
    b_1 = phi(b_0, b_2)
    %t4_0 = b_1<20
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = b_1+3
    b_2 = %t5_0
    goto  L5
L7:
    %t6_0 = a_1+b_1
    ret %t6_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    a_0 = 0
    b_0 = 1
    (%t21) = (a_0)
    goto  L2
L2:
    %t23 = phi(%t21, %t22)
    (a_1) = (%t23)
    %t2_0 = a_1<10
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = a_1+2
    a_2 = %t3_0
    (%t22) = (a_2)
    goto  L2
L4:
    (%t18) = (b_0)
    goto  L5
L5:
    %t20 = phi(%t18, %t19)
    (b_1) = (%t20)
    %t4_0 = b_1<20
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = b_1+3
    b_2 = %t5_0
    (%t19) = (b_2)
    goto  L5
L7:
    %t6_0 = a_1+b_1
    ret %t6_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    a_0 = 0
    b_0 = 1
    (%t21) = (a_0)
    %t23 = %t21
    goto  L2
L2:
    (a_1) = (%t23)
    %t2_0 = a_1<10
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = a_1+2
    a_2 = %t3_0
    (%t22) = (a_2)
    %t23 = %t22
    goto  L2
L4:
    (%t18) = (b_0)
    %t20 = %t18
    goto  L5
L5:
    (b_1) = (%t20)
    %t4_0 = b_1<20
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = b_1+3
    b_2 = %t5_0
    (%t19) = (b_2)
    %t20 = %t19
    goto  L5
L7:
    %t6_0 = a_1+b_1
    ret %t6_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    a_0 = 0
    b_0 = 1
    %t21 = a_0
    %t23 = %t21
    goto  L2
L2:
    a_1 = %t23
    %t2_0 = a_1<10
    if %t2_0 goto L3 else goto L4
L3:
    %t3_0 = a_1+2
    a_2 = %t3_0
    %t22 = a_2
    %t23 = %t22
    goto  L2
L4:
    %t18 = b_0
    %t20 = %t18
    goto  L5
L5:
    b_1 = %t20
    %t4_0 = b_1<20
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = b_1+3
    b_2 = %t5_0
    %t19 = b_2
    %t20 = %t19
    goto  L5
L7:
    %t6_0 = a_1+b_1
    ret %t6_0
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func main
Before SSA
==========
L0:
    a = 0
    b = 0
    i = 0
    j = 0
    goto  L2
L2:
    %t4 = i<3
    if %t4 goto L3 else goto L4
L3:
    j = 0
    goto  L5
L5:
    %t5 = j<2
    if %t5 goto L6 else goto L7
L6:
    %t6 = a+1
    a = %t6
    %t7 = j+1
    i = %t7
    goto  L5
L7:
    %t8 = b+1
    b = %t8
    %t9 = i+1
    i = %t9
    goto  L2
L4:
    %t10 = a+b
    ret %t10
    goto  L1
L1:
After SSA
=========
L0:
    a_0 = 0
    b_0 = 0
    i_0 = 0
    j_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_3)
    b_1 = phi(b_0, b_2)
    a_1 = phi(a_0, a_2)
    %t4_0 = i_1<3
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    goto  L5
L5:
    i_2 = phi(i_1, i_4)
    a_2 = phi(a_1, a_3)
    %t5_0 = j_1<2
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = a_2+1
    a_3 = %t6_0
    %t7_0 = j_1+1
    i_4 = %t7_0
    goto  L5
L7:
    %t8_0 = b_1+1
    b_2 = %t8_0
    %t9_0 = i_2+1
    i_3 = %t9_0
    goto  L2
L4:
    %t10_0 = a_1+b_1
    ret %t10_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    a_0 = 0
    b_0 = 0
    i_0 = 0
    j_0 = 0
    (%t38,%t41,%t44) = (i_0,b_0,a_0)
    goto  L2
L2:
    %t40 = phi(%t38, %t39)
    %t43 = phi(%t41, %t42)
    %t46 = phi(%t44, %t45)
    (i_1,b_1,a_1) = (%t40,%t43,%t46)
    %t4_0 = i_1<3
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t32,%t35) = (i_1,a_1)
    goto  L5
L5:
    %t34 = phi(%t32, %t33)
    %t37 = phi(%t35, %t36)
    (i_2,a_2) = (%t34,%t37)
    %t5_0 = j_1<2
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = a_2+1
    a_3 = %t6_0
    %t7_0 = j_1+1
    i_4 = %t7_0
    (%t33,%t36) = (i_4,a_3)
    goto  L5
L7:
    %t8_0 = b_1+1
    b_2 = %t8_0
    %t9_0 = i_2+1
    i_3 = %t9_0
    (%t39,%t42,%t45) = (i_3,b_2,a_2)
    goto  L2
L4:
    %t10_0 = a_1+b_1
    ret %t10_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    a_0 = 0
    b_0 = 0
    i_0 = 0
    j_0 = 0
    (%t38,%t41,%t44) = (i_0,b_0,a_0)
    %t40 = %t38
    %t43 = %t41
    %t46 = %t44
    goto  L2
L2:
    (i_1,b_1,a_1) = (%t40,%t43,%t46)
    %t4_0 = i_1<3
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t32,%t35) = (i_1,a_1)
    %t34 = %t32
    %t37 = %t35
    goto  L5
L5:
    (i_2,a_2) = (%t34,%t37)
    %t5_0 = j_1<2
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = a_2+1
    a_3 = %t6_0
    %t7_0 = j_1+1
    i_4 = %t7_0
    (%t33,%t36) = (i_4,a_3)
    %t34 = %t33
    %t37 = %t36
    goto  L5
L7:
    %t8_0 = b_1+1
    b_2 = %t8_0
    %t9_0 = i_2+1
    i_3 = %t9_0
    (%t39,%t42,%t45) = (i_3,b_2,a_2)
    %t40 = %t39
    %t43 = %t42
    %t46 = %t45
    goto  L2
L4:
    %t10_0 = a_1+b_1
    ret %t10_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    a_0 = 0
    b_0 = 0
    i_0 = 0
    j_0 = 0
    %t38 = i_0
    %t41 = b_0
    %t44 = a_0
    %t40 = %t38
    %t43 = %t41
    %t46 = %t44
    goto  L2
L2:
    i_1 = %t40
    b_1 = %t43
    a_1 = %t46
    %t4_0 = i_1<3
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    %t32 = i_1
    %t35 = a_1
    %t34 = %t32
    %t37 = %t35
    goto  L5
L5:
    i_2 = %t34
    a_2 = %t37
    %t5_0 = j_1<2
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = a_2+1
    a_3 = %t6_0
    %t7_0 = j_1+1
    i_4 = %t7_0
    %t33 = i_4
    %t36 = a_3
    %t34 = %t33
    %t37 = %t36
    goto  L5
L7:
    %t8_0 = b_1+1
    b_2 = %t8_0
    %t9_0 = i_2+1
    i_3 = %t9_0
    %t39 = i_3
    %t42 = b_2
    %t45 = a_2
    %t40 = %t39
    %t43 = %t42
    %t46 = %t45
    goto  L2
L4:
    %t10_0 = a_1+b_1
    ret %t10_0
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func main
Before SSA
==========
L0:
    sum = 0
    i = 0
    j = 0
    goto  L2
L2:
    %t3 = i<5
    if %t3 goto L3 else goto L4
L3:
    j = 0
    goto  L5
L5:
    %t4 = j<5
    if %t4 goto L6 else goto L7
L6:
    %t5 = j%2
    %t6 = %t5==0
    if %t6 goto L8 else goto L9
L8:
    %t7 = sum+j
    sum = %t7
    goto  L9
L9:
    %t8 = j+1
    j = %t8
    goto  L5
L7:
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
    sum_0 = 0
    i_0 = 0
    j_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_2)
    sum_1 = phi(sum_0, sum_2)
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    goto  L5
L5:
    j_2 = phi(j_1, j_3)
    sum_2 = phi(sum_1, sum_4)
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = j_2%2
    %t6_0 = %t5_0==0
    if %t6_0 goto L8 else goto L9
L8:
    %t7_0 = sum_2+j_2
    sum_3 = %t7_0
    goto  L9
L9:
    sum_4 = phi(sum_2, sum_3)
    %t8_0 = j_2+1
    j_3 = %t8_0
    goto  L5
L7:
    %t9_0 = i_1+1
    i_2 = %t9_0
    goto  L2
L4:
    ret sum_1
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    sum_0 = 0
    i_0 = 0
    j_0 = 0
    (%t38,%t41) = (i_0,sum_0)
    goto  L2
L2:
    %t40 = phi(%t38, %t39)
    %t43 = phi(%t41, %t42)
    (i_1,sum_1) = (%t40,%t43)
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t32,%t35) = (j_1,sum_1)
    goto  L5
L5:
    %t34 = phi(%t32, %t33)
    %t37 = phi(%t35, %t36)
    (j_2,sum_2) = (%t34,%t37)
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = j_2%2
    %t6_0 = %t5_0==0
    (%t29) = (sum_2)
    if %t6_0 goto L8 else goto L9
L8:
    %t7_0 = sum_2+j_2
    sum_3 = %t7_0
    (%t30) = (sum_3)
    goto  L9
L9:
    %t31 = phi(%t29, %t30)
    (sum_4) = (%t31)
    %t8_0 = j_2+1
    j_3 = %t8_0
    (%t33,%t36) = (j_3,sum_4)
    goto  L5
L7:
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t39,%t42) = (i_2,sum_2)
    goto  L2
L4:
    ret sum_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    sum_0 = 0
    i_0 = 0
    j_0 = 0
    (%t38,%t41) = (i_0,sum_0)
    %t40 = %t38
    %t43 = %t41
    goto  L2
L2:
    (i_1,sum_1) = (%t40,%t43)
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t32,%t35) = (j_1,sum_1)
    %t34 = %t32
    %t37 = %t35
    goto  L5
L5:
    (j_2,sum_2) = (%t34,%t37)
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = j_2%2
    %t6_0 = %t5_0==0
    (%t29) = (sum_2)
    %t31 = %t29
    if %t6_0 goto L8 else goto L9
L8:
    %t7_0 = sum_2+j_2
    sum_3 = %t7_0
    (%t30) = (sum_3)
    %t31 = %t30
    goto  L9
L9:
    (sum_4) = (%t31)
    %t8_0 = j_2+1
    j_3 = %t8_0
    (%t33,%t36) = (j_3,sum_4)
    %t34 = %t33
    %t37 = %t36
    goto  L5
L7:
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t39,%t42) = (i_2,sum_2)
    %t40 = %t39
    %t43 = %t42
    goto  L2
L4:
    ret sum_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    sum_0 = 0
    i_0 = 0
    j_0 = 0
    %t38 = i_0
    %t41 = sum_0
    %t40 = %t38
    %t43 = %t41
    goto  L2
L2:
    i_1 = %t40
    sum_1 = %t43
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    %t32 = j_1
    %t35 = sum_1
    %t34 = %t32
    %t37 = %t35
    goto  L5
L5:
    j_2 = %t34
    sum_2 = %t37
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = j_2%2
    %t6_0 = %t5_0==0
    %t29 = sum_2
    %t31 = %t29
    if %t6_0 goto L8 else goto L9
L8:
    %t7_0 = sum_2+j_2
    sum_3 = %t7_0
    %t30 = sum_3
    %t31 = %t30
    goto  L9
L9:
    sum_4 = %t31
    %t8_0 = j_2+1
    j_3 = %t8_0
    %t33 = j_3
    %t36 = sum_4
    %t34 = %t33
    %t37 = %t36
    goto  L5
L7:
    %t9_0 = i_1+1
    i_2 = %t9_0
    %t39 = i_2
    %t42 = sum_2
    %t40 = %t39
    %t43 = %t42
    goto  L2
L4:
    ret sum_1
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func main
Before SSA
==========
L0:
    a = 0
    i = 0
    j = 0
    goto  L2
L2:
    %t3 = i<3
    if %t3 goto L3 else goto L4
L3:
    j = 0
    goto  L5
L5:
    %t4 = j<3
    if %t4 goto L6 else goto L7
L6:
    %t5 = i==j
    if %t5 goto L8 else goto L9
L8:
    %t6 = a+i
    %t7 = %t6+j
    a = %t7
    goto  L10
L10:
    %t10 = j+1
    j = %t10
    goto  L5
L9:
    %t8 = i>j
    if %t8 goto L11 else goto L12
L11:
    %t9 = a-1
    a = %t9
    goto  L12
L12:
    goto  L10
L7:
    %t11 = i+1
    i = %t11
    goto  L2
L4:
    ret a
    goto  L1
L1:
After SSA
=========
L0:
    a_0 = 0
    i_0 = 0
    j_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_2)
    a_1 = phi(a_0, a_2)
    %t3_0 = i_1<3
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    goto  L5
L5:
    j_2 = phi(j_1, j_3)
    a_2 = phi(a_1, a_6)
    %t4_0 = j_2<3
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1==j_2
    if %t5_0 goto L8 else goto L9
L8:
    %t6_0 = a_2+i_1
    %t7_0 = %t6_0+j_2
    a_5 = %t7_0
    goto  L10
L10:
    a_6 = phi(a_5, a_4)
    %t10_0 = j_2+1
    j_3 = %t10_0
    goto  L5
L9:
    %t8_0 = i_1>j_2
    if %t8_0 goto L11 else goto L12
L11:
    %t9_0 = a_2-1
    a_3 = %t9_0
    goto  L12
L12:
    a_4 = phi(a_2, a_3)
    goto  L10
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    a_0 = 0
    i_0 = 0
    j_0 = 0
    (%t47,%t50) = (i_0,a_0)
    goto  L2
L2:
    %t49 = phi(%t47, %t48)
    %t52 = phi(%t50, %t51)
    (i_1,a_1) = (%t49,%t52)
    %t3_0 = i_1<3
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t41,%t44) = (j_1,a_1)
    goto  L5
L5:
    %t43 = phi(%t41, %t42)
    %t46 = phi(%t44, %t45)
    (j_2,a_2) = (%t43,%t46)
    %t4_0 = j_2<3
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1==j_2
    if %t5_0 goto L8 else goto L9
L8:
    %t6_0 = a_2+i_1
    %t7_0 = %t6_0+j_2
    a_5 = %t7_0
    (%t35) = (a_5)
    goto  L10
L10:
    %t37 = phi(%t35, %t36)
    (a_6) = (%t37)
    %t10_0 = j_2+1
    j_3 = %t10_0
    (%t42,%t45) = (j_3,a_6)
    goto  L5
L9:
    %t8_0 = i_1>j_2
    (%t38) = (a_2)
    if %t8_0 goto L11 else goto L12
L11:
    %t9_0 = a_2-1
    a_3 = %t9_0
    (%t39) = (a_3)
    goto  L12
L12:
    %t40 = phi(%t38, %t39)
    (a_4) = (%t40)
    (%t36) = (a_4)
    goto  L10
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    (%t48,%t51) = (i_2,a_2)
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    a_0 = 0
    i_0 = 0
    j_0 = 0
    (%t47,%t50) = (i_0,a_0)
    %t49 = %t47
    %t52 = %t50
    goto  L2
L2:
    (i_1,a_1) = (%t49,%t52)
    %t3_0 = i_1<3
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t41,%t44) = (j_1,a_1)
    %t43 = %t41
    %t46 = %t44
    goto  L5
L5:
    (j_2,a_2) = (%t43,%t46)
    %t4_0 = j_2<3
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1==j_2
    if %t5_0 goto L8 else goto L9
L8:
    %t6_0 = a_2+i_1
    %t7_0 = %t6_0+j_2
    a_5 = %t7_0
    (%t35) = (a_5)
    %t37 = %t35
    goto  L10
L10:
    (a_6) = (%t37)
    %t10_0 = j_2+1
    j_3 = %t10_0
    (%t42,%t45) = (j_3,a_6)
    %t43 = %t42
    %t46 = %t45
    goto  L5
L9:
    %t8_0 = i_1>j_2
    (%t38) = (a_2)
    %t40 = %t38
    if %t8_0 goto L11 else goto L12
L11:
    %t9_0 = a_2-1
    a_3 = %t9_0
    (%t39) = (a_3)
    %t40 = %t39
    goto  L12
L12:
    (a_4) = (%t40)
    (%t36) = (a_4)
    %t37 = %t36
    goto  L10
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    (%t48,%t51) = (i_2,a_2)
    %t49 = %t48
    %t52 = %t51
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    a_0 = 0
    i_0 = 0
    j_0 = 0
    %t47 = i_0
    %t50 = a_0
    %t49 = %t47
    %t52 = %t50
    goto  L2
L2:
    i_1 = %t49
    a_1 = %t52
    %t3_0 = i_1<3
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    %t41 = j_1
    %t44 = a_1
    %t43 = %t41
    %t46 = %t44
    goto  L5
L5:
    j_2 = %t43
    a_2 = %t46
    %t4_0 = j_2<3
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1==j_2
    if %t5_0 goto L8 else goto L9
L8:
    %t6_0 = a_2+i_1
    %t7_0 = %t6_0+j_2
    a_5 = %t7_0
    %t35 = a_5
    %t37 = %t35
    goto  L10
L10:
    a_6 = %t37
    %t10_0 = j_2+1
    j_3 = %t10_0
    %t42 = j_3
    %t45 = a_6
    %t43 = %t42
    %t46 = %t45
    goto  L5
L9:
    %t8_0 = i_1>j_2
    %t38 = a_2
    %t40 = %t38
    if %t8_0 goto L11 else goto L12
L11:
    %t9_0 = a_2-1
    a_3 = %t9_0
    %t39 = a_3
    %t40 = %t39
    goto  L12
L12:
    a_4 = %t40
    %t36 = a_4
    %t37 = %t36
    goto  L10
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    %t48 = i_2
    %t51 = a_2
    %t49 = %t48
    %t52 = %t51
    goto  L2
L4:
    ret a_1
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func main
Before SSA
==========
L0:
    count = 0
    i = 0
    j = 0
    goto  L2
L2:
    %t3 = i<5
    if %t3 goto L3 else goto L4
L3:
    j = 0
    goto  L5
L5:
    %t4 = j<5
    if %t4 goto L6 else goto L7
L6:
    %t5 = i+j
    %t6 = %t5>5
    if %t6 goto L8 else goto L9
L8:
    goto  L7
L7:
    %t11 = i+1
    i = %t11
    goto  L2
L9:
    %t7 = i==j
    if %t7 goto L10 else goto L11
L10:
    %t8 = j+1
    j = %t8
    goto  L5
L11:
    %t9 = count+1
    count = %t9
    %t10 = j+1
    j = %t10
    goto  L5
L4:
    ret count
    goto  L1
L1:
After SSA
=========
L0:
    count_0 = 0
    i_0 = 0
    j_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_2)
    count_1 = phi(count_0, count_2)
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    goto  L5
L5:
    j_2 = phi(j_1, j_4, j_3)
    count_2 = phi(count_1, count_2, count_3)
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1+j_2
    %t6_0 = %t5_0>5
    if %t6_0 goto L8 else goto L9
L8:
    goto  L7
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    goto  L2
L9:
    %t7_0 = i_1==j_2
    if %t7_0 goto L10 else goto L11
L10:
    %t8_0 = j_2+1
    j_4 = %t8_0
    goto  L5
L11:
    %t9_0 = count_2+1
    count_3 = %t9_0
    %t10_0 = j_2+1
    j_3 = %t10_0
    goto  L5
L4:
    ret count_1
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    count_0 = 0
    i_0 = 0
    j_0 = 0
    (%t41,%t44) = (i_0,count_0)
    goto  L2
L2:
    %t43 = phi(%t41, %t42)
    %t46 = phi(%t44, %t45)
    (i_1,count_1) = (%t43,%t46)
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t33,%t37) = (j_1,count_1)
    goto  L5
L5:
    %t36 = phi(%t33, %t34, %t35)
    %t40 = phi(%t37, %t38, %t39)
    (j_2,count_2) = (%t36,%t40)
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1+j_2
    %t6_0 = %t5_0>5
    if %t6_0 goto L8 else goto L9
L8:
    goto  L7
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    (%t42,%t45) = (i_2,count_2)
    goto  L2
L9:
    %t7_0 = i_1==j_2
    if %t7_0 goto L10 else goto L11
L10:
    %t8_0 = j_2+1
    j_4 = %t8_0
    (%t34,%t38) = (j_4,count_2)
    goto  L5
L11:
    %t9_0 = count_2+1
    count_3 = %t9_0
    %t10_0 = j_2+1
    j_3 = %t10_0
    (%t35,%t39) = (j_3,count_3)
    goto  L5
L4:
    ret count_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    count_0 = 0
    i_0 = 0
    j_0 = 0
    (%t41,%t44) = (i_0,count_0)
    %t43 = %t41
    %t46 = %t44
    goto  L2
L2:
    (i_1,count_1) = (%t43,%t46)
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t33,%t37) = (j_1,count_1)
    %t36 = %t33
    %t40 = %t37
    goto  L5
L5:
    (j_2,count_2) = (%t36,%t40)
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1+j_2
    %t6_0 = %t5_0>5
    if %t6_0 goto L8 else goto L9
L8:
    goto  L7
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    (%t42,%t45) = (i_2,count_2)
    %t43 = %t42
    %t46 = %t45
    goto  L2
L9:
    %t7_0 = i_1==j_2
    if %t7_0 goto L10 else goto L11
L10:
    %t8_0 = j_2+1
    j_4 = %t8_0
    (%t34,%t38) = (j_4,count_2)
    %t36 = %t34
    %t40 = %t38
    goto  L5
L11:
    %t9_0 = count_2+1
    count_3 = %t9_0
    %t10_0 = j_2+1
    j_3 = %t10_0
    (%t35,%t39) = (j_3,count_3)
    %t36 = %t35
    %t40 = %t39
    goto  L5
L4:
    ret count_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    count_0 = 0
    i_0 = 0
    j_0 = 0
    %t41 = i_0
    %t44 = count_0
    %t43 = %t41
    %t46 = %t44
    goto  L2
L2:
    i_1 = %t43
    count_1 = %t46
    %t3_0 = i_1<5
    if %t3_0 goto L3 else goto L4
L3:
    j_1 = 0
    %t33 = j_1
    %t37 = count_1
    %t36 = %t33
    %t40 = %t37
    goto  L5
L5:
    j_2 = %t36
    count_2 = %t40
    %t4_0 = j_2<5
    if %t4_0 goto L6 else goto L7
L6:
    %t5_0 = i_1+j_2
    %t6_0 = %t5_0>5
    if %t6_0 goto L8 else goto L9
L8:
    goto  L7
L7:
    %t11_0 = i_1+1
    i_2 = %t11_0
    %t42 = i_2
    %t45 = count_2
    %t43 = %t42
    %t46 = %t45
    goto  L2
L9:
    %t7_0 = i_1==j_2
    if %t7_0 goto L10 else goto L11
L10:
    %t8_0 = j_2+1
    j_4 = %t8_0
    %t34 = j_4
    %t38 = count_2
    %t36 = %t34
    %t40 = %t38
    goto  L5
L11:
    %t9_0 = count_2+1
    count_3 = %t9_0
    %t10_0 = j_2+1
    j_3 = %t10_0
    %t35 = j_3
    %t39 = count_3
    %t36 = %t35
    %t40 = %t39
    goto  L5
L4:
    ret count_1
    goto  L1
L1:
""", result);
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
        Assert.assertEquals("""
func main
Before SSA
==========
L0:
    outerSum = 0
    innerSum = 0
    i = 0
    j = 0
    goto  L2
L2:
    %t4 = i<4
    if %t4 goto L3 else goto L4
L3:
    j = 0
    goto  L5
L5:
    %t5 = j<4
    if %t5 goto L6 else goto L7
L6:
    %t6 = i+j
    %t7 = %t6%2
    %t8 = %t7==0
    if %t8 goto L8 else goto L9
L8:
    %t9 = innerSum+j
    innerSum = %t9
    goto  L9
L9:
    %t10 = j+1
    j = %t10
    goto  L5
L7:
    %t11 = outerSum+innerSum
    outerSum = %t11
    %t12 = i+1
    i = %t12
    goto  L2
L4:
    ret outerSum
    goto  L1
L1:
After SSA
=========
L0:
    outerSum_0 = 0
    innerSum_0 = 0
    i_0 = 0
    j_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_2)
    innerSum_1 = phi(innerSum_0, innerSum_2)
    outerSum_1 = phi(outerSum_0, outerSum_2)
    %t4_0 = i_1<4
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    goto  L5
L5:
    j_2 = phi(j_1, j_3)
    innerSum_2 = phi(innerSum_1, innerSum_4)
    %t5_0 = j_2<4
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = i_1+j_2
    %t7_0 = %t6_0%2
    %t8_0 = %t7_0==0
    if %t8_0 goto L8 else goto L9
L8:
    %t9_0 = innerSum_2+j_2
    innerSum_3 = %t9_0
    goto  L9
L9:
    innerSum_4 = phi(innerSum_2, innerSum_3)
    %t10_0 = j_2+1
    j_3 = %t10_0
    goto  L5
L7:
    %t11_0 = outerSum_1+innerSum_2
    outerSum_2 = %t11_0
    %t12_0 = i_1+1
    i_2 = %t12_0
    goto  L2
L4:
    ret outerSum_1
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    outerSum_0 = 0
    innerSum_0 = 0
    i_0 = 0
    j_0 = 0
    (%t46,%t49,%t52) = (i_0,innerSum_0,outerSum_0)
    goto  L2
L2:
    %t48 = phi(%t46, %t47)
    %t51 = phi(%t49, %t50)
    %t54 = phi(%t52, %t53)
    (i_1,innerSum_1,outerSum_1) = (%t48,%t51,%t54)
    %t4_0 = i_1<4
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t40,%t43) = (j_1,innerSum_1)
    goto  L5
L5:
    %t42 = phi(%t40, %t41)
    %t45 = phi(%t43, %t44)
    (j_2,innerSum_2) = (%t42,%t45)
    %t5_0 = j_2<4
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = i_1+j_2
    %t7_0 = %t6_0%2
    %t8_0 = %t7_0==0
    (%t37) = (innerSum_2)
    if %t8_0 goto L8 else goto L9
L8:
    %t9_0 = innerSum_2+j_2
    innerSum_3 = %t9_0
    (%t38) = (innerSum_3)
    goto  L9
L9:
    %t39 = phi(%t37, %t38)
    (innerSum_4) = (%t39)
    %t10_0 = j_2+1
    j_3 = %t10_0
    (%t41,%t44) = (j_3,innerSum_4)
    goto  L5
L7:
    %t11_0 = outerSum_1+innerSum_2
    outerSum_2 = %t11_0
    %t12_0 = i_1+1
    i_2 = %t12_0
    (%t47,%t50,%t53) = (i_2,innerSum_2,outerSum_2)
    goto  L2
L4:
    ret outerSum_1
    goto  L1
L1:
After removing phis from CSSA
L0:
    outerSum_0 = 0
    innerSum_0 = 0
    i_0 = 0
    j_0 = 0
    (%t46,%t49,%t52) = (i_0,innerSum_0,outerSum_0)
    %t48 = %t46
    %t51 = %t49
    %t54 = %t52
    goto  L2
L2:
    (i_1,innerSum_1,outerSum_1) = (%t48,%t51,%t54)
    %t4_0 = i_1<4
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    (%t40,%t43) = (j_1,innerSum_1)
    %t42 = %t40
    %t45 = %t43
    goto  L5
L5:
    (j_2,innerSum_2) = (%t42,%t45)
    %t5_0 = j_2<4
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = i_1+j_2
    %t7_0 = %t6_0%2
    %t8_0 = %t7_0==0
    (%t37) = (innerSum_2)
    %t39 = %t37
    if %t8_0 goto L8 else goto L9
L8:
    %t9_0 = innerSum_2+j_2
    innerSum_3 = %t9_0
    (%t38) = (innerSum_3)
    %t39 = %t38
    goto  L9
L9:
    (innerSum_4) = (%t39)
    %t10_0 = j_2+1
    j_3 = %t10_0
    (%t41,%t44) = (j_3,innerSum_4)
    %t42 = %t41
    %t45 = %t44
    goto  L5
L7:
    %t11_0 = outerSum_1+innerSum_2
    outerSum_2 = %t11_0
    %t12_0 = i_1+1
    i_2 = %t12_0
    (%t47,%t50,%t53) = (i_2,innerSum_2,outerSum_2)
    %t48 = %t47
    %t51 = %t50
    %t54 = %t53
    goto  L2
L4:
    ret outerSum_1
    goto  L1
L1:
After exiting SSA
=================
L0:
    outerSum_0 = 0
    innerSum_0 = 0
    i_0 = 0
    j_0 = 0
    %t46 = i_0
    %t49 = innerSum_0
    %t52 = outerSum_0
    %t48 = %t46
    %t51 = %t49
    %t54 = %t52
    goto  L2
L2:
    i_1 = %t48
    innerSum_1 = %t51
    outerSum_1 = %t54
    %t4_0 = i_1<4
    if %t4_0 goto L3 else goto L4
L3:
    j_1 = 0
    %t40 = j_1
    %t43 = innerSum_1
    %t42 = %t40
    %t45 = %t43
    goto  L5
L5:
    j_2 = %t42
    innerSum_2 = %t45
    %t5_0 = j_2<4
    if %t5_0 goto L6 else goto L7
L6:
    %t6_0 = i_1+j_2
    %t7_0 = %t6_0%2
    %t8_0 = %t7_0==0
    %t37 = innerSum_2
    %t39 = %t37
    if %t8_0 goto L8 else goto L9
L8:
    %t9_0 = innerSum_2+j_2
    innerSum_3 = %t9_0
    %t38 = innerSum_3
    %t39 = %t38
    goto  L9
L9:
    innerSum_4 = %t39
    %t10_0 = j_2+1
    j_3 = %t10_0
    %t41 = j_3
    %t44 = innerSum_4
    %t42 = %t41
    %t45 = %t44
    goto  L5
L7:
    %t11_0 = outerSum_1+innerSum_2
    outerSum_2 = %t11_0
    %t12_0 = i_1+1
    i_2 = %t12_0
    %t47 = i_2
    %t50 = innerSum_2
    %t53 = outerSum_2
    %t48 = %t47
    %t51 = %t50
    %t54 = %t53
    goto  L2
L4:
    ret outerSum_1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testSSA14() {
        String src = """
                func foo()->Int 
                {
                    return 1 && 2
                }
""";
        String result = compileSrc(src);
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    if 1 goto L2 else goto L3
L2:
    %t0 = 2
    goto  L4
L4:
    ret %t0
    goto  L1
L1:
L3:
    %t0 = 0
    goto  L4
After SSA
=========
L0:
    if 1 goto L2 else goto L3
L2:
    %t0_1 = 2
    goto  L4
L4:
    %t0_2 = phi(%t0_1, %t0_0)
    ret %t0_2
    goto  L1
L1:
L3:
    %t0_0 = 0
    goto  L4
After converting from SSA to CSSA
L0:
    if 1 goto L2 else goto L3
L2:
    %t0_1 = 2
    (%t4) = (%t0_1)
    goto  L4
L4:
    %t6 = phi(%t4, %t5)
    (%t0_2) = (%t6)
    ret %t0_2
    goto  L1
L1:
L3:
    %t0_0 = 0
    (%t5) = (%t0_0)
    goto  L4
After removing phis from CSSA
L0:
    if 1 goto L2 else goto L3
L2:
    %t0_1 = 2
    (%t4) = (%t0_1)
    %t6 = %t4
    goto  L4
L4:
    (%t0_2) = (%t6)
    ret %t0_2
    goto  L1
L1:
L3:
    %t0_0 = 0
    (%t5) = (%t0_0)
    %t6 = %t5
    goto  L4
After exiting SSA
=================
L0:
    if 1 goto L2 else goto L3
L2:
    %t0_1 = 2
    %t4 = %t0_1
    %t6 = %t4
    goto  L4
L4:
    %t0_2 = %t6
    ret %t0_2
    goto  L1
L1:
L3:
    %t0_0 = 0
    %t5 = %t0_0
    %t6 = %t5
    goto  L4
""", result);
    }

    @Test
    public void testSSA15() {
        String src = """
                struct Foo
                {
                    var i: Int
                }
                func foo()->Int
                {
                    var f = new [Foo?] { new Foo{i = 1}, null }
                    return null == f[1] && 1 == f[0].i
                }
""";
        String result = compileSrc(src);
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    %t1 = New([Foo?], len=2)
    %t2 = New(Foo)
    %t2.i = 1
    %t1[0] = %t2
    %t1[1] = null
    f = %t1
    %t3 = f[1]
    %t4 = null==%t3
    if %t4 goto L2 else goto L3
L2:
    %t5 = f[0]
    %t6 = %t5.i
    %t7 = 1==%t6
    goto  L4
L4:
    ret %t7
    goto  L1
L1:
L3:
    %t7 = 0
    goto  L4
After SSA
=========
L0:
    %t1_0 = New([Foo?], len=2)
    %t2_0 = New(Foo)
    %t2_0.i = 1
    %t1_0[0] = %t2_0
    %t1_0[1] = null
    f_0 = %t1_0
    %t3_0 = f_0[1]
    %t4_0 = null==%t3_0
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = f_0[0]
    %t6_0 = %t5_0.i
    %t7_1 = 1==%t6_0
    goto  L4
L4:
    %t7_2 = phi(%t7_1, %t7_0)
    ret %t7_2
    goto  L1
L1:
L3:
    %t7_0 = 0
    goto  L4
After converting from SSA to CSSA
L0:
    %t1_0 = New([Foo?], len=2)
    %t2_0 = New(Foo)
    %t2_0.i = 1
    %t1_0[0] = %t2_0
    %t1_0[1] = null
    f_0 = %t1_0
    %t3_0 = f_0[1]
    %t4_0 = null==%t3_0
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = f_0[0]
    %t6_0 = %t5_0.i
    %t7_1 = 1==%t6_0
    (%t18) = (%t7_1)
    goto  L4
L4:
    %t20 = phi(%t18, %t19)
    (%t7_2) = (%t20)
    ret %t7_2
    goto  L1
L1:
L3:
    %t7_0 = 0
    (%t19) = (%t7_0)
    goto  L4
After removing phis from CSSA
L0:
    %t1_0 = New([Foo?], len=2)
    %t2_0 = New(Foo)
    %t2_0.i = 1
    %t1_0[0] = %t2_0
    %t1_0[1] = null
    f_0 = %t1_0
    %t3_0 = f_0[1]
    %t4_0 = null==%t3_0
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = f_0[0]
    %t6_0 = %t5_0.i
    %t7_1 = 1==%t6_0
    (%t18) = (%t7_1)
    %t20 = %t18
    goto  L4
L4:
    (%t7_2) = (%t20)
    ret %t7_2
    goto  L1
L1:
L3:
    %t7_0 = 0
    (%t19) = (%t7_0)
    %t20 = %t19
    goto  L4
After exiting SSA
=================
L0:
    %t1_0 = New([Foo?], len=2)
    %t2_0 = New(Foo)
    %t2_0.i = 1
    %t1_0[0] = %t2_0
    %t1_0[1] = null
    f_0 = %t1_0
    %t3_0 = f_0[1]
    %t4_0 = null==%t3_0
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = f_0[0]
    %t6_0 = %t5_0.i
    %t7_1 = 1==%t6_0
    %t18 = %t7_1
    %t20 = %t18
    goto  L4
L4:
    %t7_2 = %t20
    ret %t7_2
    goto  L1
L1:
L3:
    %t7_0 = 0
    %t19 = %t7_0
    %t20 = %t19
    goto  L4
""", result);
    }


    @Test
    public void testSSA17() {
        String src = """
func merge(begin: Int, middle: Int, end: Int)
{
    if (begin < end) {
        var cond = 0
        if (begin < middle) {
            if (begin >= end)          cond = 1;
        }
        if (cond)
        {
            cond = 0
        }
    }
}
""";
        String result = compileSrc(src);
        Assert.assertEquals("""
func merge
Before SSA
==========
L0:
    arg begin
    arg middle
    arg end
    %t4 = begin<end
    if %t4 goto L2 else goto L3
L2:
    cond = 0
    %t5 = begin<middle
    if %t5 goto L4 else goto L5
L4:
    %t6 = begin>=end
    if %t6 goto L6 else goto L7
L6:
    cond = 1
    goto  L7
L7:
    goto  L5
L5:
    if cond goto L8 else goto L9
L8:
    cond = 0
    goto  L9
L9:
    goto  L3
L3:
    goto  L1
L1:
After SSA
=========
L0:
    arg begin_0
    arg middle_0
    arg end_0
    %t4_0 = begin_0<end_0
    if %t4_0 goto L2 else goto L3
L2:
    cond_0 = 0
    %t5_0 = begin_0<middle_0
    if %t5_0 goto L4 else goto L5
L4:
    %t6_0 = begin_0>=end_0
    if %t6_0 goto L6 else goto L7
L6:
    cond_1 = 1
    goto  L7
L7:
    cond_2 = phi(cond_0, cond_1)
    goto  L5
L5:
    cond_3 = phi(cond_0, cond_2)
    if cond_3 goto L8 else goto L9
L8:
    cond_4 = 0
    goto  L9
L9:
    goto  L3
L3:
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    arg begin_0
    arg middle_0
    arg end_0
    %t4_0 = begin_0<end_0
    if %t4_0 goto L2 else goto L3
L2:
    cond_0 = 0
    %t5_0 = begin_0<middle_0
    (%t18) = (cond_0)
    if %t5_0 goto L4 else goto L5
L4:
    %t6_0 = begin_0>=end_0
    (%t21) = (cond_0)
    if %t6_0 goto L6 else goto L7
L6:
    cond_1 = 1
    (%t22) = (cond_1)
    goto  L7
L7:
    %t23 = phi(%t21, %t22)
    (cond_2) = (%t23)
    (%t19) = (cond_2)
    goto  L5
L5:
    %t20 = phi(%t18, %t19)
    (cond_3) = (%t20)
    if cond_3 goto L8 else goto L9
L8:
    cond_4 = 0
    goto  L9
L9:
    goto  L3
L3:
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg begin_0
    arg middle_0
    arg end_0
    %t4_0 = begin_0<end_0
    if %t4_0 goto L2 else goto L3
L2:
    cond_0 = 0
    %t5_0 = begin_0<middle_0
    (%t18) = (cond_0)
    %t20 = %t18
    if %t5_0 goto L4 else goto L5
L4:
    %t6_0 = begin_0>=end_0
    (%t21) = (cond_0)
    %t23 = %t21
    if %t6_0 goto L6 else goto L7
L6:
    cond_1 = 1
    (%t22) = (cond_1)
    %t23 = %t22
    goto  L7
L7:
    (cond_2) = (%t23)
    (%t19) = (cond_2)
    %t20 = %t19
    goto  L5
L5:
    (cond_3) = (%t20)
    if cond_3 goto L8 else goto L9
L8:
    cond_4 = 0
    goto  L9
L9:
    goto  L3
L3:
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg begin_0
    arg middle_0
    arg end_0
    %t4_0 = begin_0<end_0
    if %t4_0 goto L2 else goto L3
L2:
    cond_0 = 0
    %t5_0 = begin_0<middle_0
    %t18 = cond_0
    %t20 = %t18
    if %t5_0 goto L4 else goto L5
L4:
    %t6_0 = begin_0>=end_0
    %t21 = cond_0
    %t23 = %t21
    if %t6_0 goto L6 else goto L7
L6:
    cond_1 = 1
    %t22 = cond_1
    %t23 = %t22
    goto  L7
L7:
    cond_2 = %t23
    %t19 = cond_2
    %t20 = %t19
    goto  L5
L5:
    cond_3 = %t20
    if cond_3 goto L8 else goto L9
L8:
    cond_4 = 0
    goto  L9
L9:
    goto  L3
L3:
    goto  L1
L1:
""", result);
    }

    @Test
    public void testSSA18()
    {
        String src = """
                func foo(len: Int, val: Int, x: Int, y: Int)->[Int] {
                    if (x > y) {
                        len=len+x
                        val=val+x
                    }
                    return new [Int]{len=len,value=val} 
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func foo
Before SSA
==========
L0:
    arg len
    arg val
    arg x
    arg y
    %t4 = x>y
    if %t4 goto L2 else goto L3
L2:
    %t5 = len+x
    len = %t5
    %t6 = val+x
    val = %t6
    goto  L3
L3:
    %t7 = New([Int], len=len, initValue=val)
    ret %t7
    goto  L1
L1:
After SSA
=========
L0:
    arg len_0
    arg val_0
    arg x_0
    arg y_0
    %t4_0 = x_0>y_0
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = len_0+x_0
    len_1 = %t5_0
    %t6_0 = val_0+x_0
    val_1 = %t6_0
    goto  L3
L3:
    val_2 = phi(val_0, val_1)
    len_2 = phi(len_0, len_1)
    %t7_0 = New([Int], len=len_2, initValue=val_2)
    ret %t7_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    arg len_0
    arg val_0
    arg x_0
    arg y_0
    %t4_0 = x_0>y_0
    (%t20,%t23) = (val_0,len_0)
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = len_0+x_0
    len_1 = %t5_0
    %t6_0 = val_0+x_0
    val_1 = %t6_0
    (%t21,%t24) = (val_1,len_1)
    goto  L3
L3:
    %t22 = phi(%t20, %t21)
    %t25 = phi(%t23, %t24)
    (val_2,len_2) = (%t22,%t25)
    %t7_0 = New([Int], len=len_2, initValue=val_2)
    ret %t7_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg len_0
    arg val_0
    arg x_0
    arg y_0
    %t4_0 = x_0>y_0
    (%t20,%t23) = (val_0,len_0)
    %t22 = %t20
    %t25 = %t23
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = len_0+x_0
    len_1 = %t5_0
    %t6_0 = val_0+x_0
    val_1 = %t6_0
    (%t21,%t24) = (val_1,len_1)
    %t22 = %t21
    %t25 = %t24
    goto  L3
L3:
    (val_2,len_2) = (%t22,%t25)
    %t7_0 = New([Int], len=len_2, initValue=val_2)
    ret %t7_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg len_0
    arg val_0
    arg x_0
    arg y_0
    %t4_0 = x_0>y_0
    %t20 = val_0
    %t23 = len_0
    %t22 = %t20
    %t25 = %t23
    if %t4_0 goto L2 else goto L3
L2:
    %t5_0 = len_0+x_0
    len_1 = %t5_0
    %t6_0 = val_0+x_0
    val_1 = %t6_0
    %t21 = val_1
    %t24 = len_1
    %t22 = %t21
    %t25 = %t24
    goto  L3
L3:
    val_2 = %t22
    len_2 = %t25
    %t7_0 = New([Int], len=len_2, initValue=val_2)
    ret %t7_0
    goto  L1
L1:
""", result);
    }

    @Test
    public void testSSA19()
    {
        String src = """
func bug(N: Int)
{
    var p=2
    while( p < N ) {
        if (p) {
            p = p + 1
        }
    }
    while ( p < N ) {
        p = p + 1
    }
}
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func bug
Before SSA
==========
L0:
    arg N
    p = 2
    goto  L2
L2:
    %t2 = p<N
    if %t2 goto L3 else goto L4
L3:
    if p goto L5 else goto L6
L5:
    %t3 = p+1
    p = %t3
    goto  L6
L6:
    goto  L2
L4:
    goto  L7
L7:
    %t4 = p<N
    if %t4 goto L8 else goto L9
L8:
    %t5 = p+1
    p = %t5
    goto  L7
L9:
    goto  L1
L1:
After SSA
=========
L0:
    arg N_0
    p_0 = 2
    goto  L2
L2:
    p_1 = phi(p_0, p_5)
    %t2_0 = p_1<N_0
    if %t2_0 goto L3 else goto L4
L3:
    if p_1 goto L5 else goto L6
L5:
    %t3_0 = p_1+1
    p_4 = %t3_0
    goto  L6
L6:
    p_5 = phi(p_1, p_4)
    goto  L2
L4:
    goto  L7
L7:
    p_2 = phi(p_1, p_3)
    %t4_0 = p_2<N_0
    if %t4_0 goto L8 else goto L9
L8:
    %t5_0 = p_2+1
    p_3 = %t5_0
    goto  L7
L9:
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    arg N_0
    p_0 = 2
    (%t23) = (p_0)
    goto  L2
L2:
    %t25 = phi(%t23, %t24)
    (p_1) = (%t25)
    %t2_0 = p_1<N_0
    if %t2_0 goto L3 else goto L4
L3:
    (%t17) = (p_1)
    if p_1 goto L5 else goto L6
L5:
    %t3_0 = p_1+1
    p_4 = %t3_0
    (%t18) = (p_4)
    goto  L6
L6:
    %t19 = phi(%t17, %t18)
    (p_5) = (%t19)
    (%t24) = (p_5)
    goto  L2
L4:
    (%t20) = (p_1)
    goto  L7
L7:
    %t22 = phi(%t20, %t21)
    (p_2) = (%t22)
    %t4_0 = p_2<N_0
    if %t4_0 goto L8 else goto L9
L8:
    %t5_0 = p_2+1
    p_3 = %t5_0
    (%t21) = (p_3)
    goto  L7
L9:
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg N_0
    p_0 = 2
    (%t23) = (p_0)
    %t25 = %t23
    goto  L2
L2:
    (p_1) = (%t25)
    %t2_0 = p_1<N_0
    if %t2_0 goto L3 else goto L4
L3:
    (%t17) = (p_1)
    %t19 = %t17
    if p_1 goto L5 else goto L6
L5:
    %t3_0 = p_1+1
    p_4 = %t3_0
    (%t18) = (p_4)
    %t19 = %t18
    goto  L6
L6:
    (p_5) = (%t19)
    (%t24) = (p_5)
    %t25 = %t24
    goto  L2
L4:
    (%t20) = (p_1)
    %t22 = %t20
    goto  L7
L7:
    (p_2) = (%t22)
    %t4_0 = p_2<N_0
    if %t4_0 goto L8 else goto L9
L8:
    %t5_0 = p_2+1
    p_3 = %t5_0
    (%t21) = (p_3)
    %t22 = %t21
    goto  L7
L9:
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg N_0
    p_0 = 2
    %t23 = p_0
    %t25 = %t23
    goto  L2
L2:
    p_1 = %t25
    %t2_0 = p_1<N_0
    if %t2_0 goto L3 else goto L4
L3:
    %t17 = p_1
    %t19 = %t17
    if p_1 goto L5 else goto L6
L5:
    %t3_0 = p_1+1
    p_4 = %t3_0
    %t18 = p_4
    %t19 = %t18
    goto  L6
L6:
    p_5 = %t19
    %t24 = p_5
    %t25 = %t24
    goto  L2
L4:
    %t20 = p_1
    %t22 = %t20
    goto  L7
L7:
    p_2 = %t22
    %t4_0 = p_2<N_0
    if %t4_0 goto L8 else goto L9
L8:
    %t5_0 = p_2+1
    p_3 = %t5_0
    %t21 = p_3
    %t22 = %t21
    goto  L7
L9:
    goto  L1
L1:
""", result);
    }

    @Test
    public void testSSA20()
    {
        String src = """
func sieve(N: Int)->[Int]
{
    // The main Sieve array
    var ary = new [Int]{len=N,value=0}
    // The primes less than N
    var primes = new [Int]{len=N/2,value=0}
    // Number of primes so far, searching at index p
    var nprimes = 0
    var p=2
    // Find primes while p^2 < N
    while( p*p < N ) {
        // skip marked non-primes
        while( ary[p] ) {
            p = p + 1
        }
        // p is now a prime
        primes[nprimes] = p
        nprimes = nprimes+1
        // Mark out the rest non-primes
        var i = p + p
        while( i < N ) {
            ary[i] = 1
            i = i + p
        }
        p = p + 1
    }

    // Now just collect the remaining primes, no more marking
    while ( p < N ) {
        if( !ary[p] ) {
            primes[nprimes] = p
            nprimes = nprimes + 1
        }
        p = p + 1
    }

    // Copy/shrink the result array
    var rez = new [Int]{len=nprimes,value=0}
    var j = 0
    while( j < nprimes ) {
        rez[j] = primes[j]
        j = j + 1
    }
    return rez
}
func eq(a: [Int], b: [Int], n: Int)->Int
{
    var result = 1
    var i = 0
    while (i < n)
    {
        if (a[i] != b[i])
        {
            result = 0
            break
        }
        i = i + 1
    }
    return result
}

func main()->Int
{
    var rez = sieve(20)
    var expected = new [Int]{2,3,5,7,11,13,17,19}
    return eq(rez,expected,8)
}
""";
        String result = compileSrc(src);
        Assert.assertEquals("""
func sieve
Before SSA
==========
L0:
    arg N
    %t8 = New([Int], len=N, initValue=0)
    ary = %t8
    %t10 = N/2
    %t9 = New([Int], len=%t10, initValue=0)
    primes = %t9
    nprimes = 0
    p = 2
    goto  L2
L2:
    %t11 = p*p
    %t12 = %t11<N
    if %t12 goto L3 else goto L4
L3:
    goto  L5
L5:
    %t13 = ary[p]
    if %t13 goto L6 else goto L7
L6:
    %t14 = p+1
    p = %t14
    goto  L5
L7:
    primes[nprimes] = p
    %t15 = nprimes+1
    nprimes = %t15
    %t16 = p+p
    i = %t16
    goto  L8
L8:
    %t17 = i<N
    if %t17 goto L9 else goto L10
L9:
    ary[i] = 1
    %t18 = i+p
    i = %t18
    goto  L8
L10:
    %t19 = p+1
    p = %t19
    goto  L2
L4:
    goto  L11
L11:
    %t20 = p<N
    if %t20 goto L12 else goto L13
L12:
    %t21 = ary[p]
    %t22 = !%t21
    if %t22 goto L14 else goto L15
L14:
    primes[nprimes] = p
    %t23 = nprimes+1
    nprimes = %t23
    goto  L15
L15:
    %t24 = p+1
    p = %t24
    goto  L11
L13:
    %t25 = New([Int], len=nprimes, initValue=0)
    rez = %t25
    j = 0
    goto  L16
L16:
    %t26 = j<nprimes
    if %t26 goto L17 else goto L18
L17:
    %t27 = primes[j]
    rez[j] = %t27
    %t28 = j+1
    j = %t28
    goto  L16
L18:
    ret rez
    goto  L1
L1:
After SSA
=========
L0:
    arg N_0
    %t8_0 = New([Int], len=N_0, initValue=0)
    ary_0 = %t8_0
    %t10_0 = N_0/2
    %t9_0 = New([Int], len=%t10_0, initValue=0)
    primes_0 = %t9_0
    nprimes_0 = 0
    p_0 = 2
    goto  L2
L2:
    p_1 = phi(p_0, p_5)
    nprimes_1 = phi(nprimes_0, nprimes_5)
    %t11_0 = p_1*p_1
    %t12_0 = %t11_0<N_0
    if %t12_0 goto L3 else goto L4
L3:
    goto  L5
L5:
    p_4 = phi(p_1, p_6)
    %t13_0 = ary_0[p_4]
    if %t13_0 goto L6 else goto L7
L6:
    %t14_0 = p_4+1
    p_6 = %t14_0
    goto  L5
L7:
    primes_0[nprimes_1] = p_4
    %t15_0 = nprimes_1+1
    nprimes_5 = %t15_0
    %t16_0 = p_4+p_4
    i_0 = %t16_0
    goto  L8
L8:
    i_1 = phi(i_0, i_2)
    %t17_0 = i_1<N_0
    if %t17_0 goto L9 else goto L10
L9:
    ary_0[i_1] = 1
    %t18_0 = i_1+p_4
    i_2 = %t18_0
    goto  L8
L10:
    %t19_0 = p_4+1
    p_5 = %t19_0
    goto  L2
L4:
    goto  L11
L11:
    p_2 = phi(p_1, p_3)
    nprimes_2 = phi(nprimes_1, nprimes_4)
    %t20_0 = p_2<N_0
    if %t20_0 goto L12 else goto L13
L12:
    %t21_0 = ary_0[p_2]
    %t22_0 = !%t21_0
    if %t22_0 goto L14 else goto L15
L14:
    primes_0[nprimes_2] = p_2
    %t23_0 = nprimes_2+1
    nprimes_3 = %t23_0
    goto  L15
L15:
    nprimes_4 = phi(nprimes_2, nprimes_3)
    %t24_0 = p_2+1
    p_3 = %t24_0
    goto  L11
L13:
    %t25_0 = New([Int], len=nprimes_2, initValue=0)
    rez_0 = %t25_0
    j_0 = 0
    goto  L16
L16:
    j_1 = phi(j_0, j_2)
    %t26_0 = j_1<nprimes_2
    if %t26_0 goto L17 else goto L18
L17:
    %t27_0 = primes_0[j_1]
    rez_0[j_1] = %t27_0
    %t28_0 = j_1+1
    j_2 = %t28_0
    goto  L16
L18:
    ret rez_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    arg N_0
    %t8_0 = New([Int], len=N_0, initValue=0)
    ary_0 = %t8_0
    %t10_0 = N_0/2
    %t9_0 = New([Int], len=%t10_0, initValue=0)
    primes_0 = %t9_0
    nprimes_0 = 0
    p_0 = 2
    (%t91,%t94) = (p_0,nprimes_0)
    goto  L2
L2:
    %t93 = phi(%t91, %t92)
    %t96 = phi(%t94, %t95)
    (p_1,nprimes_1) = (%t93,%t96)
    %t11_0 = p_1*p_1
    %t12_0 = %t11_0<N_0
    if %t12_0 goto L3 else goto L4
L3:
    (%t76) = (p_1)
    goto  L5
L5:
    %t78 = phi(%t76, %t77)
    (p_4) = (%t78)
    %t13_0 = ary_0[p_4]
    if %t13_0 goto L6 else goto L7
L6:
    %t14_0 = p_4+1
    p_6 = %t14_0
    (%t77) = (p_6)
    goto  L5
L7:
    primes_0[nprimes_1] = p_4
    %t15_0 = nprimes_1+1
    nprimes_5 = %t15_0
    %t16_0 = p_4+p_4
    i_0 = %t16_0
    (%t73) = (i_0)
    goto  L8
L8:
    %t75 = phi(%t73, %t74)
    (i_1) = (%t75)
    %t17_0 = i_1<N_0
    if %t17_0 goto L9 else goto L10
L9:
    ary_0[i_1] = 1
    %t18_0 = i_1+p_4
    i_2 = %t18_0
    (%t74) = (i_2)
    goto  L8
L10:
    %t19_0 = p_4+1
    p_5 = %t19_0
    (%t92,%t95) = (p_5,nprimes_5)
    goto  L2
L4:
    (%t85,%t88) = (p_1,nprimes_1)
    goto  L11
L11:
    %t87 = phi(%t85, %t86)
    %t90 = phi(%t88, %t89)
    (p_2,nprimes_2) = (%t87,%t90)
    %t20_0 = p_2<N_0
    if %t20_0 goto L12 else goto L13
L12:
    %t21_0 = ary_0[p_2]
    %t22_0 = !%t21_0
    (%t79) = (nprimes_2)
    if %t22_0 goto L14 else goto L15
L14:
    primes_0[nprimes_2] = p_2
    %t23_0 = nprimes_2+1
    nprimes_3 = %t23_0
    (%t80) = (nprimes_3)
    goto  L15
L15:
    %t81 = phi(%t79, %t80)
    (nprimes_4) = (%t81)
    %t24_0 = p_2+1
    p_3 = %t24_0
    (%t86,%t89) = (p_3,nprimes_4)
    goto  L11
L13:
    %t25_0 = New([Int], len=nprimes_2, initValue=0)
    rez_0 = %t25_0
    j_0 = 0
    (%t82) = (j_0)
    goto  L16
L16:
    %t84 = phi(%t82, %t83)
    (j_1) = (%t84)
    %t26_0 = j_1<nprimes_2
    if %t26_0 goto L17 else goto L18
L17:
    %t27_0 = primes_0[j_1]
    rez_0[j_1] = %t27_0
    %t28_0 = j_1+1
    j_2 = %t28_0
    (%t83) = (j_2)
    goto  L16
L18:
    ret rez_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    arg N_0
    %t8_0 = New([Int], len=N_0, initValue=0)
    ary_0 = %t8_0
    %t10_0 = N_0/2
    %t9_0 = New([Int], len=%t10_0, initValue=0)
    primes_0 = %t9_0
    nprimes_0 = 0
    p_0 = 2
    (%t91,%t94) = (p_0,nprimes_0)
    %t93 = %t91
    %t96 = %t94
    goto  L2
L2:
    (p_1,nprimes_1) = (%t93,%t96)
    %t11_0 = p_1*p_1
    %t12_0 = %t11_0<N_0
    if %t12_0 goto L3 else goto L4
L3:
    (%t76) = (p_1)
    %t78 = %t76
    goto  L5
L5:
    (p_4) = (%t78)
    %t13_0 = ary_0[p_4]
    if %t13_0 goto L6 else goto L7
L6:
    %t14_0 = p_4+1
    p_6 = %t14_0
    (%t77) = (p_6)
    %t78 = %t77
    goto  L5
L7:
    primes_0[nprimes_1] = p_4
    %t15_0 = nprimes_1+1
    nprimes_5 = %t15_0
    %t16_0 = p_4+p_4
    i_0 = %t16_0
    (%t73) = (i_0)
    %t75 = %t73
    goto  L8
L8:
    (i_1) = (%t75)
    %t17_0 = i_1<N_0
    if %t17_0 goto L9 else goto L10
L9:
    ary_0[i_1] = 1
    %t18_0 = i_1+p_4
    i_2 = %t18_0
    (%t74) = (i_2)
    %t75 = %t74
    goto  L8
L10:
    %t19_0 = p_4+1
    p_5 = %t19_0
    (%t92,%t95) = (p_5,nprimes_5)
    %t93 = %t92
    %t96 = %t95
    goto  L2
L4:
    (%t85,%t88) = (p_1,nprimes_1)
    %t87 = %t85
    %t90 = %t88
    goto  L11
L11:
    (p_2,nprimes_2) = (%t87,%t90)
    %t20_0 = p_2<N_0
    if %t20_0 goto L12 else goto L13
L12:
    %t21_0 = ary_0[p_2]
    %t22_0 = !%t21_0
    (%t79) = (nprimes_2)
    %t81 = %t79
    if %t22_0 goto L14 else goto L15
L14:
    primes_0[nprimes_2] = p_2
    %t23_0 = nprimes_2+1
    nprimes_3 = %t23_0
    (%t80) = (nprimes_3)
    %t81 = %t80
    goto  L15
L15:
    (nprimes_4) = (%t81)
    %t24_0 = p_2+1
    p_3 = %t24_0
    (%t86,%t89) = (p_3,nprimes_4)
    %t87 = %t86
    %t90 = %t89
    goto  L11
L13:
    %t25_0 = New([Int], len=nprimes_2, initValue=0)
    rez_0 = %t25_0
    j_0 = 0
    (%t82) = (j_0)
    %t84 = %t82
    goto  L16
L16:
    (j_1) = (%t84)
    %t26_0 = j_1<nprimes_2
    if %t26_0 goto L17 else goto L18
L17:
    %t27_0 = primes_0[j_1]
    rez_0[j_1] = %t27_0
    %t28_0 = j_1+1
    j_2 = %t28_0
    (%t83) = (j_2)
    %t84 = %t83
    goto  L16
L18:
    ret rez_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    arg N_0
    %t8_0 = New([Int], len=N_0, initValue=0)
    ary_0 = %t8_0
    %t10_0 = N_0/2
    %t9_0 = New([Int], len=%t10_0, initValue=0)
    primes_0 = %t9_0
    nprimes_0 = 0
    p_0 = 2
    %t91 = p_0
    %t94 = nprimes_0
    %t93 = %t91
    %t96 = %t94
    goto  L2
L2:
    p_1 = %t93
    nprimes_1 = %t96
    %t11_0 = p_1*p_1
    %t12_0 = %t11_0<N_0
    if %t12_0 goto L3 else goto L4
L3:
    %t76 = p_1
    %t78 = %t76
    goto  L5
L5:
    p_4 = %t78
    %t13_0 = ary_0[p_4]
    if %t13_0 goto L6 else goto L7
L6:
    %t14_0 = p_4+1
    p_6 = %t14_0
    %t77 = p_6
    %t78 = %t77
    goto  L5
L7:
    primes_0[nprimes_1] = p_4
    %t15_0 = nprimes_1+1
    nprimes_5 = %t15_0
    %t16_0 = p_4+p_4
    i_0 = %t16_0
    %t73 = i_0
    %t75 = %t73
    goto  L8
L8:
    i_1 = %t75
    %t17_0 = i_1<N_0
    if %t17_0 goto L9 else goto L10
L9:
    ary_0[i_1] = 1
    %t18_0 = i_1+p_4
    i_2 = %t18_0
    %t74 = i_2
    %t75 = %t74
    goto  L8
L10:
    %t19_0 = p_4+1
    p_5 = %t19_0
    %t92 = p_5
    %t95 = nprimes_5
    %t93 = %t92
    %t96 = %t95
    goto  L2
L4:
    %t85 = p_1
    %t88 = nprimes_1
    %t87 = %t85
    %t90 = %t88
    goto  L11
L11:
    p_2 = %t87
    nprimes_2 = %t90
    %t20_0 = p_2<N_0
    if %t20_0 goto L12 else goto L13
L12:
    %t21_0 = ary_0[p_2]
    %t22_0 = !%t21_0
    %t79 = nprimes_2
    %t81 = %t79
    if %t22_0 goto L14 else goto L15
L14:
    primes_0[nprimes_2] = p_2
    %t23_0 = nprimes_2+1
    nprimes_3 = %t23_0
    %t80 = nprimes_3
    %t81 = %t80
    goto  L15
L15:
    nprimes_4 = %t81
    %t24_0 = p_2+1
    p_3 = %t24_0
    %t86 = p_3
    %t89 = nprimes_4
    %t87 = %t86
    %t90 = %t89
    goto  L11
L13:
    %t25_0 = New([Int], len=nprimes_2, initValue=0)
    rez_0 = %t25_0
    j_0 = 0
    %t82 = j_0
    %t84 = %t82
    goto  L16
L16:
    j_1 = %t84
    %t26_0 = j_1<nprimes_2
    if %t26_0 goto L17 else goto L18
L17:
    %t27_0 = primes_0[j_1]
    rez_0[j_1] = %t27_0
    %t28_0 = j_1+1
    j_2 = %t28_0
    %t83 = j_2
    %t84 = %t83
    goto  L16
L18:
    ret rez_0
    goto  L1
L1:
func eq
Before SSA
==========
L0:
    arg a
    arg b
    arg n
    result = 1
    i = 0
    goto  L2
L2:
    %t5 = i<n
    if %t5 goto L3 else goto L4
L3:
    %t6 = a[i]
    %t7 = b[i]
    %t8 = %t6!=%t7
    if %t8 goto L5 else goto L6
L5:
    result = 0
    goto  L4
L4:
    ret result
    goto  L1
L1:
L6:
    %t9 = i+1
    i = %t9
    goto  L2
After SSA
=========
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_2)
    %t5_0 = i_1<n_0
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    goto  L4
L4:
    result_2 = phi(result_0, result_1)
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    goto  L2
After converting from SSA to CSSA
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    (%t27) = (i_0)
    goto  L2
L2:
    %t29 = phi(%t27, %t28)
    (i_1) = (%t29)
    %t5_0 = i_1<n_0
    (%t24) = (result_0)
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    (%t25) = (result_1)
    goto  L4
L4:
    %t26 = phi(%t24, %t25)
    (result_2) = (%t26)
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t28) = (i_2)
    goto  L2
After removing phis from CSSA
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    (%t27) = (i_0)
    %t29 = %t27
    goto  L2
L2:
    (i_1) = (%t29)
    %t5_0 = i_1<n_0
    (%t24) = (result_0)
    %t26 = %t24
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    (%t25) = (result_1)
    %t26 = %t25
    goto  L4
L4:
    (result_2) = (%t26)
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t28) = (i_2)
    %t29 = %t28
    goto  L2
After exiting SSA
=================
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    %t27 = i_0
    %t29 = %t27
    goto  L2
L2:
    i_1 = %t29
    %t5_0 = i_1<n_0
    %t24 = result_0
    %t26 = %t24
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    %t25 = result_1
    %t26 = %t25
    goto  L4
L4:
    result_2 = %t26
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    %t28 = i_2
    %t29 = %t28
    goto  L2
func main
Before SSA
==========
L0:
    %t2 = 20
    %t3 = call sieve params %t2
    rez = %t3
    %t4 = New([Int], len=8)
    %t4[0] = 2
    %t4[1] = 3
    %t4[2] = 5
    %t4[3] = 7
    %t4[4] = 11
    %t4[5] = 13
    %t4[6] = 17
    %t4[7] = 19
    expected = %t4
    %t5 = rez
    %t6 = expected
    %t7 = 8
    %t8 = call eq params %t5, %t6, %t7
    ret %t8
    goto  L1
L1:
After SSA
=========
L0:
    %t2_0 = 20
    %t3_0 = call sieve params %t2_0
    rez_0 = %t3_0
    %t4_0 = New([Int], len=8)
    %t4_0[0] = 2
    %t4_0[1] = 3
    %t4_0[2] = 5
    %t4_0[3] = 7
    %t4_0[4] = 11
    %t4_0[5] = 13
    %t4_0[6] = 17
    %t4_0[7] = 19
    expected_0 = %t4_0
    %t5_0 = rez_0
    %t6_0 = expected_0
    %t7_0 = 8
    %t8_0 = call eq params %t5_0, %t6_0, %t7_0
    ret %t8_0
    goto  L1
L1:
After converting from SSA to CSSA
L0:
    %t2_0 = 20
    %t3_0 = call sieve params %t2_0
    rez_0 = %t3_0
    %t4_0 = New([Int], len=8)
    %t4_0[0] = 2
    %t4_0[1] = 3
    %t4_0[2] = 5
    %t4_0[3] = 7
    %t4_0[4] = 11
    %t4_0[5] = 13
    %t4_0[6] = 17
    %t4_0[7] = 19
    expected_0 = %t4_0
    %t5_0 = rez_0
    %t6_0 = expected_0
    %t7_0 = 8
    %t8_0 = call eq params %t5_0, %t6_0, %t7_0
    ret %t8_0
    goto  L1
L1:
After removing phis from CSSA
L0:
    %t2_0 = 20
    %t3_0 = call sieve params %t2_0
    rez_0 = %t3_0
    %t4_0 = New([Int], len=8)
    %t4_0[0] = 2
    %t4_0[1] = 3
    %t4_0[2] = 5
    %t4_0[3] = 7
    %t4_0[4] = 11
    %t4_0[5] = 13
    %t4_0[6] = 17
    %t4_0[7] = 19
    expected_0 = %t4_0
    %t5_0 = rez_0
    %t6_0 = expected_0
    %t7_0 = 8
    %t8_0 = call eq params %t5_0, %t6_0, %t7_0
    ret %t8_0
    goto  L1
L1:
After exiting SSA
=================
L0:
    %t2_0 = 20
    %t3_0 = call sieve params %t2_0
    rez_0 = %t3_0
    %t4_0 = New([Int], len=8)
    %t4_0[0] = 2
    %t4_0[1] = 3
    %t4_0[2] = 5
    %t4_0[3] = 7
    %t4_0[4] = 11
    %t4_0[5] = 13
    %t4_0[6] = 17
    %t4_0[7] = 19
    expected_0 = %t4_0
    %t5_0 = rez_0
    %t6_0 = expected_0
    %t7_0 = 8
    %t8_0 = call eq params %t5_0, %t6_0, %t7_0
    ret %t8_0
    goto  L1
L1:
""", result);
    }

    @Test
    public void testSSA21()
    {
        String src = """
func eq(a: [Int], b: [Int], n: Int)->Int
{
    var result = 1
    var i = 0
    while (i < n)
    {
        if (a[i] != b[i])
        {
            result = 0
            break
        }
        i = i + 1
    } 
    return result
}
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
func eq
Before SSA
==========
L0:
    arg a
    arg b
    arg n
    result = 1
    i = 0
    goto  L2
L2:
    %t5 = i<n
    if %t5 goto L3 else goto L4
L3:
    %t6 = a[i]
    %t7 = b[i]
    %t8 = %t6!=%t7
    if %t8 goto L5 else goto L6
L5:
    result = 0
    goto  L4
L4:
    ret result
    goto  L1
L1:
L6:
    %t9 = i+1
    i = %t9
    goto  L2
After SSA
=========
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    goto  L2
L2:
    i_1 = phi(i_0, i_2)
    %t5_0 = i_1<n_0
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    goto  L4
L4:
    result_2 = phi(result_0, result_1)
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    goto  L2
After converting from SSA to CSSA
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    (%t27) = (i_0)
    goto  L2
L2:
    %t29 = phi(%t27, %t28)
    (i_1) = (%t29)
    %t5_0 = i_1<n_0
    (%t24) = (result_0)
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    (%t25) = (result_1)
    goto  L4
L4:
    %t26 = phi(%t24, %t25)
    (result_2) = (%t26)
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t28) = (i_2)
    goto  L2
After removing phis from CSSA
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    (%t27) = (i_0)
    %t29 = %t27
    goto  L2
L2:
    (i_1) = (%t29)
    %t5_0 = i_1<n_0
    (%t24) = (result_0)
    %t26 = %t24
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    (%t25) = (result_1)
    %t26 = %t25
    goto  L4
L4:
    (result_2) = (%t26)
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    (%t28) = (i_2)
    %t29 = %t28
    goto  L2
After exiting SSA
=================
L0:
    arg a_0
    arg b_0
    arg n_0
    result_0 = 1
    i_0 = 0
    %t27 = i_0
    %t29 = %t27
    goto  L2
L2:
    i_1 = %t29
    %t5_0 = i_1<n_0
    %t24 = result_0
    %t26 = %t24
    if %t5_0 goto L3 else goto L4
L3:
    %t6_0 = a_0[i_1]
    %t7_0 = b_0[i_1]
    %t8_0 = %t6_0!=%t7_0
    if %t8_0 goto L5 else goto L6
L5:
    result_1 = 0
    %t25 = result_1
    %t26 = %t25
    goto  L4
L4:
    result_2 = %t26
    ret result_2
    goto  L1
L1:
L6:
    %t9_0 = i_1+1
    i_2 = %t9_0
    %t28 = i_2
    %t29 = %t28
    goto  L2
""", result);
    }

}