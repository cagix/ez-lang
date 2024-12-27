package com.compilerprogramming.ezlang.interpreter;

import com.compilerprogramming.ezlang.compiler.Compiler;
import org.junit.Assert;
import org.junit.Test;

public class TestInterpreter {

    Value compileAndRun(String src, String mainFunction) {
        return compileAndRun(src, mainFunction, false);
    }
    Value compileAndRun(String src, String mainFunction, boolean opt) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src, opt);
        var compiled = compiler.dumpIR(typeDict);
        System.out.println(compiled);
        var interpreter = new Interpreter(typeDict);
        return interpreter.run(mainFunction);
    }

    @Test
    public void testFunction1() {
        String src = """
                func foo()->Int {
                    return 42;
                }
                """;
        var value = compileAndRun(src, "foo");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
            && integerValue.value == 42);
    }

    @Test
    public void testFunction2() {
        String src = """
                func bar()->Int {
                    return 42;
                }
                func foo()->Int {
                    return bar();
                }
                """;
        var value = compileAndRun(src, "foo");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 42);
    }

    @Test
    public void testFunction3() {
        String src = """
                func negate(n: Int)->Int {
                    return -n;
                }
                func foo()->Int {
                    return negate(42);
                }
                """;
        var value = compileAndRun(src, "foo");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == -42);
    }

    @Test
    public void testFunction4() {
        String src = """
                func foo(x: Int, y: Int)->Int { return x+y; }
                func bar()->Int { var t = foo(1,2); return t+1; }
                """;
        var value = compileAndRun(src, "bar");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 4);
    }

    @Test
    public void testFunction5() {
        String src = """
                func bar()->Int { var t = new [Int] {1,21,3}; return t[1]; }
                """;
        var value = compileAndRun(src, "bar");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 21);
    }

    @Test
    public void testFunction6() {
        String src = """
                struct Test
                {
                    var field: Int
                }
                func foo()->Test 
                {
                    var test = new Test{ field = 42 }
                    return test
                }
                func bar()->Int { return foo().field }
                """;
        var value = compileAndRun(src, "bar");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 42);
    }

    @Test
    public void testFunction7() {
        String src = """
                func bar(arg: Int)->Int {
                    if (arg)
                        return 42;
                    return 0;    
                }
                func foo()->Int {
                    return bar(1);
                }
                """;
        var value = compileAndRun(src, "foo");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 42);
    }

    @Test
    public void testFunction8() {
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
                func foo()->Int {
                    return factorial(5);
                }
                """;
        var value = compileAndRun(src, "foo", true);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 120);
    }

    @Test
    public void testFunction9() {
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

                func foo() {
                    return fib(10);
                }
                """;
        var value = compileAndRun(src, "foo", true);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 89);
    }
}
