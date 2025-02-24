package com.compilerprogramming.ezlang.compiler;

import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;

public class TestIncrementalSSA {
    String compileSrc(String src) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src, EnumSet.of(Options.ISSA));
        return compiler.dumpIR(typeDict, true);
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
                func foo(d: Int)
                Reg #0 d 0
                Reg #1 a 1
                Reg #2 b 2
                Reg #3 c 3
                Reg #4 %t4 4
                Reg #5 %t5 5
                Reg #6 a_1 1
                Reg #7 %t7 7
                Reg #8 c_1 3
                L0:
                    arg d
                    a = 42
                    b = a
                    %t4 = a+b
                    c = %t4
                    %t5 = c+23
                    a_1 = %t5
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
                func foo(d: Int)->Int
                Reg #0 d 0
                Reg #1 a 1
                Reg #2 %t2 2
                Reg #3 a_1 1
                Reg #4 %t4 4
                Reg #5 a_2 1
                Reg #6 a_3 1
                L0:
                    arg d
                    a = 42
                    if d goto L2 else goto L3
                L2:
                    %t2 = a+1
                    a_1 = %t2
                    goto  L4
                L4:
                    a_3 = phi(a_1, a_2)
                    ret a_3
                    goto  L1
                L1:
                L3:
                    %t4 = a-1
                    a_2 = %t4
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
                func factorial(num: Int)->Int
                Reg #0 num 0
                Reg #1 result 1
                Reg #2 %t2 2
                Reg #3 num_1 0
                Reg #4 %t4 4
                Reg #5 result_1 1
                Reg #6 result_2 1
                Reg #7 %t7 7
                Reg #8 num_2 0
                L0:
                    arg num
                    result = 1
                    goto  L2
                L2:
                    result_1 = phi(result, result_2)
                    num_1 = phi(num, num_2)
                    %t2 = num_1>1
                    if %t2 goto L3 else goto L4
                L3:
                    %t4 = result_1*num_1
                    result_2 = %t4
                    %t7 = num_1-1
                    num_2 = %t7
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
                func print(a: Int,b: Int,c: Int,d: Int)
                Reg #0 a 0
                Reg #1 b 1
                Reg #2 c 2
                Reg #3 d 3
                L0:
                    arg a
                    arg b
                    arg c
                    arg d
                    goto  L1
                L1:
                func example14_66(p: Int,q: Int,r: Int,s: Int,t: Int)
                Reg #0 p 0
                Reg #1 q 1
                Reg #2 r 2
                Reg #3 s 3
                Reg #4 t 4
                Reg #5 i 5
                Reg #6 j 6
                Reg #7 k 7
                Reg #8 l 8
                Reg #9 p_1 0
                Reg #10 i_1 5
                Reg #11 j_1 6
                Reg #12 q_1 1
                Reg #13 l_1 8
                Reg #14 l_2 8
                Reg #15 %t15 15
                Reg #16 k_1 7
                Reg #17 k_2 7
                Reg #18 k_3 7
                Reg #19 %t19 19
                Reg #20 k_4 7
                Reg #21 %t21 21
                Reg #22 i_2 5
                Reg #23 i_3 5
                Reg #24 %t24 24
                Reg #25 j_2 6
                Reg #26 j_3 6
                Reg #27 j_4 6
                Reg #28 %t28 28
                Reg #29 k_5 7
                Reg #30 %t30 30
                Reg #31 l_3 8
                Reg #32 l_4 8
                Reg #33 l_5 8
                Reg #34 r_1 2
                Reg #35 %t35 35
                Reg #36 l_6 8
                Reg #37 l_7 8
                Reg #38 %t38 38
                Reg #39 s_1 3
                Reg #40 s_2 3
                Reg #41 r_2 2
                Reg #42 r_3 2
                Reg #43 r_4 2
                Reg #44 r_5 2
                Reg #45 s_3 3
                Reg #46 s_4 3
                Reg #47 s_5 3
                Reg #48 l_8 8
                Reg #49 %t49 49
                Reg #50 i_4 5
                Reg #51 i_5 5
                Reg #52 i_6 5
                Reg #53 i_7 5
                Reg #54 %t54 54
                Reg #55 t_1 4
                Reg #56 t_2 4
                Reg #57 t_3 4
                Reg #58 t_4 4
                Reg #59 t_5 4
                Reg #60 t_6 4
                Reg #61 p_2 0
                Reg #62 p_3 0
                Reg #63 p_4 0
                Reg #64 p_5 0
                Reg #65 p_6 0
                Reg #66 q_2 1
                Reg #67 q_3 1
                Reg #68 q_4 1
                Reg #69 q_5 1
                Reg #70 q_6 1
                Reg #71 r_6 2
                Reg #72 s_6 3
                Reg #73 j_5 6
                Reg #74 j_6 6
                Reg #75 j_7 6
                Reg #76 k_6 7
                Reg #77 k_7 7
                Reg #78 k_8 7
                Reg #79 l_9 8
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
                    t_5 = phi(t, t_1)
                    s_5 = phi(s, s_2)
                    r_4 = phi(r, r_1)
                    l_5 = phi(l, l_9)
                    j_4 = phi(j, j_5)
                    k_2 = phi(k, k_6)
                    q_1 = phi(q, q_2)
                    i_1 = phi(i, i_7)
                    p_1 = phi(p, p_2)
                    if 1 goto L3 else goto L4
                L3:
                    if p_1 goto L5 else goto L6
                L5:
                    j_1 = i_1
                    if q_1 goto L8 else goto L9
                L8:
                    l_1 = 2
                    goto  L10
                L10:
                    l_4 = phi(l_1, l_2)
                    %t15 = k_2+1
                    k_3 = %t15
                    goto  L7
                L7:
                    l_3 = phi(l_4, l_5)
                    k_5 = phi(k_3, k_4)
                    j_2 = phi(j_1, j_4)
                    %t21 = i_1
                    %t24 = j_2
                    %t28 = k_5
                    %t30 = l_3
                    call print params %t21, %t24, %t28, %t30
                    goto  L11
                L11:
                    l_6 = phi(l_3, l_8)
                    if 1 goto L12 else goto L13
                L12:
                    if r_4 goto L14 else goto L15
                L14:
                    %t35 = l_6+4
                    l_7 = %t35
                    goto  L15
                L15:
                    l_8 = phi(l_6, l_7)
                    %t38 = !s_5
                    if %t38 goto L16 else goto L17
                L16:
                    goto  L13
                L13:
                    l_9 = phi(l_6, l_8)
                    k_6 = phi(k_5, k_7)
                    j_5 = phi(j_2, j_6)
                    q_2 = phi(q_1, q_3)
                    p_2 = phi(p_1, p_3)
                    t_1 = phi(t_5, t_2)
                    i_4 = phi(i_1, i_5)
                    %t49 = i_4+6
                    i_7 = %t49
                    %t54 = !t_1
                    if %t54 goto L18 else goto L19
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
                    %t19 = k_2+2
                    k_4 = %t19
                    goto  L7
                """, result);
    }

    @Test
    public void test5() {
        String src = """
                func fib(n: Int)->Int {
                    var i: Int;
                    var temp: Int;
                    var f1=1;
                    var f2=1;
                    i=n;
                    while( i>1 ){
                        temp = f1+f2;
                        f1=f2;
                        f2=temp;
                        i=i-1;
                    }
                    return f2;
                }
                func foo()->Int {
                    return fib(10);
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
                func fib(n: Int)->Int
                Reg #0 n 0
                Reg #1 i 1
                Reg #2 temp 2
                Reg #3 f1 3
                Reg #4 f2 4
                Reg #5 %t5 5
                Reg #6 i_1 1
                Reg #7 %t7 7
                Reg #8 f1_1 3
                Reg #9 f2_1 4
                Reg #10 f1_2 3
                Reg #11 f2_2 4
                Reg #12 %t12 12
                Reg #13 i_2 1
                L0:
                    arg n
                    f1 = 1
                    f2 = 1
                    i = n
                    goto  L2
                L2:
                    f2_1 = phi(f2, f2_2)
                    f1_1 = phi(f1, f1_2)
                    i_1 = phi(i, i_2)
                    %t5 = i_1>1
                    if %t5 goto L3 else goto L4
                L3:
                    %t7 = f1_1+f2_1
                    temp = %t7
                    f1_2 = f2_1
                    f2_2 = temp
                    %t12 = i_1-1
                    i_2 = %t12
                    goto  L2
                L4:
                    ret f2_1
                    goto  L1
                L1:
                func foo()->Int
                Reg #0 %t0 0
                Reg #1 %t1 1
                L0:
                    %t0 = 10
                    %t1 = call fib params %t0
                    ret %t1
                    goto  L1
                L1:
                """, result);
        System.out.println(result);
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
func foo(x: Int,y: Int)->Int
Reg #0 x 0
Reg #1 y 1
Reg #2 sum 2
Reg #3 %t3 3
Reg #4 %t4 4
Reg #5 x_1 0
Reg #6 y_1 1
Reg #7 %t7 7
Reg #8 %t8 8
Reg #9 %t9 9
Reg #10 %t10 10
Reg #11 sum_1 2
Reg #12 sum_2 2
Reg #13 %t13 13
Reg #14 x_2 0
Reg #15 x_3 0
Reg #16 y_2 1
Reg #17 sum_3 2
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
    sum_1 = phi(sum, sum_3)
    x_1 = phi(x, x_3)
    %t4 = x_1<y
    if %t4 goto L5 else goto L6
L5:
    %t7 = x_1/2
    %t8 = %t7*2
    %t9 = %t8==x_1
    if %t9 goto L7 else goto L8
L7:
    %t10 = sum_1+1
    sum_2 = %t10
    goto  L8
L8:
    sum_3 = phi(sum_1, sum_2)
    %t13 = x_1+1
    x_3 = %t13
    goto  L4
L6:
    ret sum_1
    goto  L1
""", result);
    }


}