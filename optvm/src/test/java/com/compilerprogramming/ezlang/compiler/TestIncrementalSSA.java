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
    l_5 = phi(l, l_9)
    j_4 = phi(j, j_2)
    k_2 = phi(k, k_5)
    i_1 = phi(i, i_7)
    if 1 goto L3 else goto L4
L3:
    if p goto L5 else goto L6
L5:
    j_1 = i_1
    if q goto L8 else goto L9
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
    if r goto L14 else goto L15
L14:
    %t35 = l_6+4
    l_7 = %t35
    goto  L15
L15:
    l_8 = phi(l_6, l_7)
    %t38 = !s
    if %t38 goto L16 else goto L17
L16:
    goto  L13
L13:
    l_9 = phi(l_6, l_8)
    %t49 = i_1+6
    i_7 = %t49
    %t54 = !t
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
func main()->Int
Reg #0 sum 0
Reg #1 i 1
Reg #2 j 2
Reg #3 %t3 3
Reg #4 i_1 1
Reg #5 j_1 2
Reg #6 %t6 6
Reg #7 j_2 2
Reg #8 %t8 8
Reg #9 %t9 9
Reg #10 %t10 10
Reg #11 sum_1 0
Reg #12 sum_2 0
Reg #13 %t13 13
Reg #14 j_3 2
Reg #15 j_4 2
Reg #16 sum_3 0
Reg #17 sum_4 0
Reg #18 %t18 18
Reg #19 i_2 1
Reg #20 i_3 1
Reg #21 i_4 1
L0:
    sum = 0
    i = 0
    j = 0
    goto  L2
L2:
    sum_3 = phi(sum, sum_1)
    i_1 = phi(i, i_4)
    %t3 = i_1<5
    if %t3 goto L3 else goto L4
L3:
    j_1 = 0
    goto  L5
L5:
    sum_1 = phi(sum_3, sum_4)
    j_2 = phi(j_1, j_4)
    %t6 = j_2<5
    if %t6 goto L6 else goto L7
L6:
    %t8 = j_2%2
    %t9 = %t8==0
    if %t9 goto L8 else goto L9
L8:
    %t10 = sum_1+j_2
    sum_2 = %t10
    goto  L9
L9:
    sum_4 = phi(sum_1, sum_2)
    %t13 = j_2+1
    j_4 = %t13
    goto  L5
L7:
    %t18 = i_1+1
    i_4 = %t18
    goto  L2
L4:
    ret sum_3
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
func main()->Int
Reg #0 a 0
Reg #1 i 1
Reg #2 j 2
Reg #3 %t3 3
Reg #4 i_1 1
Reg #5 j_1 2
Reg #6 %t6 6
Reg #7 j_2 2
Reg #8 %t8 8
Reg #9 i_2 1
Reg #10 %t10 10
Reg #11 a_1 0
Reg #12 %t12 12
Reg #13 a_2 0
Reg #14 %t14 14
Reg #15 %t15 15
Reg #16 a_3 0
Reg #17 %t17 17
Reg #18 j_3 2
Reg #19 j_4 2
Reg #20 j_5 2
Reg #21 a_4 0
Reg #22 a_5 0
Reg #23 a_6 0
Reg #24 i_3 1
Reg #25 i_4 1
Reg #26 %t26 26
Reg #27 i_5 1
L0:
    a = 0
    i = 0
    j = 0
    goto  L2
L2:
    a_4 = phi(a, a_1)
    i_1 = phi(i, i_5)
    %t3 = i_1<3
    if %t3 goto L3 else goto L4
L3:
    j_1 = 0
    goto  L5
L5:
    a_1 = phi(a_4, a_5)
    j_2 = phi(j_1, j_5)
    %t6 = j_2<3
    if %t6 goto L6 else goto L7
L6:
    %t8 = i_1==j_2
    if %t8 goto L8 else goto L9
L8:
    %t10 = a_1+i_1
    %t12 = %t10+j_2
    a_2 = %t12
    goto  L10
L10:
    a_5 = phi(a_2, a_6)
    %t17 = j_2+1
    j_5 = %t17
    goto  L5
L9:
    %t14 = i_1>j_2
    if %t14 goto L11 else goto L12
L11:
    %t15 = a_1-1
    a_3 = %t15
    goto  L12
L12:
    a_6 = phi(a_1, a_3)
    goto  L10
L7:
    %t26 = i_1+1
    i_5 = %t26
    goto  L2
L4:
    ret a_4
    goto  L1
L1:
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
func merge(begin: Int,middle: Int,end: Int)
Reg #0 begin 0
Reg #1 middle 1
Reg #2 end 2
Reg #3 cond 3
Reg #4 %t4 4
Reg #5 %t5 5
Reg #6 %t6 6
Reg #7 cond_1 3
Reg #8 cond_2 3
Reg #9 cond_3 3
Reg #10 cond_4 3
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
    cond_1 = 1
    goto  L7
L7:
    cond_3 = phi(cond, cond_1)
    goto  L5
L5:
    cond_2 = phi(cond, cond_3)
    if cond_2 goto L8 else goto L9
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
func foo(len: Int,val: Int,x: Int,y: Int)->[Int]
Reg #0 len 0
Reg #1 val 1
Reg #2 x 2
Reg #3 y 3
Reg #4 %t4 4
Reg #5 %t5 5
Reg #6 len_1 0
Reg #7 %t7 7
Reg #8 val_1 1
Reg #9 %t9 9
Reg #10 len_2 0
Reg #11 val_2 1
L0:
    arg len
    arg val
    arg x
    arg y
    %t4 = x>y
    if %t4 goto L2 else goto L3
L2:
    %t5 = len+x
    len_1 = %t5
    %t7 = val+x
    val_1 = %t7
    goto  L3
L3:
    val_2 = phi(val, val_1)
    len_2 = phi(len, len_1)
    %t9 = New([Int], len=len_2, initValue=val_2)
    ret %t9
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
func bug(N: Int)
Reg #0 N 0
Reg #1 p 1
Reg #2 %t2 2
Reg #3 p_1 1
Reg #4 N_1 0
Reg #5 %t5 5
Reg #6 p_2 1
Reg #7 N_2 0
Reg #8 p_3 1
Reg #9 %t9 9
Reg #10 p_4 1
Reg #11 N_3 0
Reg #12 %t12 12
Reg #13 p_5 1
L0:
    arg N
    p = 2
    goto  L2
L2:
    p_1 = phi(p, p_3)
    %t2 = p_1<N
    if %t2 goto L3 else goto L4
L3:
    if p_1 goto L5 else goto L6
L5:
    %t5 = p_1+1
    p_2 = %t5
    goto  L6
L6:
    p_3 = phi(p_1, p_2)
    goto  L2
L4:
    goto  L7
L7:
    p_4 = phi(p_1, p_5)
    %t9 = p_4<N
    if %t9 goto L8 else goto L9
L8:
    %t12 = p_4+1
    p_5 = %t12
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
func sieve(N: Int)->[Int]
Reg #0 N 0
Reg #1 ary 1
Reg #2 primes 2
Reg #3 nprimes 3
Reg #4 p 4
Reg #5 rez 5
Reg #6 j 6
Reg #7 i 7
Reg #8 %t8 8
Reg #9 %t9 9
Reg #10 %t10 10
Reg #11 %t11 11
Reg #12 p_1 4
Reg #13 %t13 13
Reg #14 N_1 0
Reg #15 %t15 15
Reg #16 ary_1 1
Reg #17 p_2 4
Reg #18 %t18 18
Reg #19 p_3 4
Reg #20 ary_2 1
Reg #21 primes_1 2
Reg #22 primes_2 2
Reg #23 nprimes_1 3
Reg #24 nprimes_2 3
Reg #25 %t25 25
Reg #26 nprimes_3 3
Reg #27 %t27 27
Reg #28 %t28 28
Reg #29 i_1 7
Reg #30 N_2 0
Reg #31 ary_3 1
Reg #32 %t32 32
Reg #33 p_4 4
Reg #34 i_2 7
Reg #35 N_3 0
Reg #36 %t36 36
Reg #37 p_5 4
Reg #38 primes_3 2
Reg #39 nprimes_4 3
Reg #40 %t40 40
Reg #41 p_6 4
Reg #42 N_4 0
Reg #43 %t43 43
Reg #44 ary_4 1
Reg #45 %t45 45
Reg #46 primes_4 2
Reg #47 nprimes_5 3
Reg #48 %t48 48
Reg #49 nprimes_6 3
Reg #50 %t50 50
Reg #51 p_7 4
Reg #52 p_8 4
Reg #53 N_5 0
Reg #54 ary_5 1
Reg #55 primes_5 2
Reg #56 nprimes_7 3
Reg #57 %t57 57
Reg #58 %t58 58
Reg #59 j_1 6
Reg #60 nprimes_8 3
Reg #61 %t61 61
Reg #62 primes_6 2
Reg #63 rez_1 5
Reg #64 %t64 64
Reg #65 j_2 6
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
    nprimes_2 = phi(nprimes, nprimes_3)
    p_1 = phi(p, p_5)
    %t11 = p_1*p_1
    %t13 = %t11<N
    if %t13 goto L3 else goto L4
L3:
    goto  L5
L5:
    p_2 = phi(p_1, p_3)
    %t15 = ary[p_2]
    if %t15 goto L6 else goto L7
L6:
    %t18 = p_2+1
    p_3 = %t18
    goto  L5
L7:
    primes[nprimes_2] = p_2
    %t25 = nprimes_2+1
    nprimes_3 = %t25
    %t27 = p_2+p_2
    i = %t27
    goto  L8
L8:
    i_1 = phi(i, i_2)
    %t28 = i_1<N
    if %t28 goto L9 else goto L10
L9:
    ary[i_1] = 1
    %t32 = i_1+p_2
    i_2 = %t32
    goto  L8
L10:
    %t36 = p_2+1
    p_5 = %t36
    goto  L2
L4:
    goto  L11
L11:
    nprimes_5 = phi(nprimes_2, nprimes_7)
    p_6 = phi(p_1, p_8)
    %t40 = p_6<N
    if %t40 goto L12 else goto L13
L12:
    %t43 = ary[p_6]
    %t45 = !%t43
    if %t45 goto L14 else goto L15
L14:
    primes[nprimes_5] = p_6
    %t48 = nprimes_5+1
    nprimes_6 = %t48
    goto  L15
L15:
    nprimes_7 = phi(nprimes_5, nprimes_6)
    %t50 = p_6+1
    p_8 = %t50
    goto  L11
L13:
    %t57 = New([Int], len=nprimes_5, initValue=0)
    rez = %t57
    j = 0
    goto  L16
L16:
    j_1 = phi(j, j_2)
    %t58 = j_1<nprimes_5
    if %t58 goto L17 else goto L18
L17:
    %t61 = primes[j_1]
    rez[j_1] = %t61
    %t64 = j_1+1
    j_2 = %t64
    goto  L16
L18:
    ret rez
    goto  L1
L1:
func eq(a: [Int],b: [Int],n: Int)->Int
Reg #0 a 0
Reg #1 b 1
Reg #2 n 2
Reg #3 result 3
Reg #4 i 4
Reg #5 %t5 5
Reg #6 i_1 4
Reg #7 n_1 2
Reg #8 %t8 8
Reg #9 a_1 0
Reg #10 %t10 10
Reg #11 b_1 1
Reg #12 %t12 12
Reg #13 result_1 3
Reg #14 %t14 14
Reg #15 i_2 4
Reg #16 result_2 3
Reg #17 result_3 3
L0:
    arg a
    arg b
    arg n
    result = 1
    i = 0
    goto  L2
L2:
    i_1 = phi(i, i_2)
    %t5 = i_1<n
    if %t5 goto L3 else goto L4
L3:
    %t8 = a[i_1]
    %t10 = b[i_1]
    %t12 = %t8!=%t10
    if %t12 goto L5 else goto L6
L5:
    result_1 = 0
    goto  L4
L4:
    result_2 = phi(result, result_1)
    ret result_2
    goto  L1
L1:
L6:
    %t14 = i_1+1
    i_2 = %t14
    goto  L2
func main()->Int
Reg #0 rez 0
Reg #1 expected 1
Reg #2 %t2 2
Reg #3 %t3 3
Reg #4 %t4 4
Reg #5 %t5 5
Reg #6 %t6 6
Reg #7 %t7 7
Reg #8 %t8 8
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
""", result);
    }
}