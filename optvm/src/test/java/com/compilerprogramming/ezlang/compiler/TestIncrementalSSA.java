package com.compilerprogramming.ezlang.compiler;

import org.junit.Test;

import java.util.EnumSet;

public class TestIncrementalSSA {
    String compileSrc(String src) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src, EnumSet.of(Options.ISSA,Options.DUMP_SSA_IR));
        return compiler.dumpIR(typeDict,true);
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
        System.out.println(result);
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
        System.out.println(result);
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
        System.out.println(result);
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
        System.out.println(result);
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
        System.out.println(result);
    }
}