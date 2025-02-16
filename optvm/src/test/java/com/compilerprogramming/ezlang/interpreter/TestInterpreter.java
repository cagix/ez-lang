package com.compilerprogramming.ezlang.interpreter;

import com.compilerprogramming.ezlang.compiler.Compiler;
import com.compilerprogramming.ezlang.compiler.Options;
import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;

public class TestInterpreter {

    Value compileAndRun(String src, String mainFunction) {
        return compileAndRun(src, mainFunction, Options.NONE);
    }
    Value compileAndRun(String src, String mainFunction, EnumSet<Options> options) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src, options);
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
        var value = compileAndRun(src, "foo", Options.OPT);
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

                func foo()->Int {
                    return fib(10);
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 89);
    }

    @Test
    public void testFunction10() {
        String src = """
                func foo()->Int {
                    if (1)
                        return 2;
                    return 3;
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 2);
    }

    // 4.1.2 optimizer evaluation
    // propagating through expressions
    @Test
    public void testFunction11() {
        String src = """
                func bar(data: [Int]) {
                    var j = 1
                    var k = 2
                    var m = 4
                    var n = k * m
                    j = n + j
                    data[0] = j
                }
                func foo()->Int {
                    var data = new [Int] {0}
                    bar(data)
                    return data[0]
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 9);
    }

    // 4.2.1 optimizer evaluation
    // extended basic blocks
    @Test
    public void testFunction12() {
        String src = """
                func bar(data: [Int]) {
                    var j = 12345
                    if (data[0])
                        data[1] = 1 + j - 1234
                    else
                        data[2] = 123 + j + 10
                }
                func foo()->Int {
                    var data = new [Int] {0,0,0}
                    bar(data)
                    return data[0]+data[1]+data[2];
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 12478);
    }

    // 4.2.1 optimizer evaluation
    // extended basic blocks
    @Test
    public void testFunction13() {
        String src = """
                func bar(data: [Int]) {
                    var j = 12345
                    if (data[0])
                        data[1] = 1 + j - 1234
                    else
                        data[2] = 123 - j + 10
                }
                func foo()->Int {
                    var data = new [Int] {0,0,0}
                    bar(data)
                    return data[0]+data[1]+data[2];
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == -12212);
    }

    // 4.2.2 dominators
    @Test
    public void testFunction14() {
        String src = """
                func bar(data: [Int]) {
                    var j = 5
                    if (data[0])
                        data[1] = 10
                    else
                        data[2] = 15
                    data[3] = j + 21    
                }
                func foo()->Int {
                    var data = new [Int] {0,0,0,0}
                    bar(data)
                    return data[0]+data[1]+data[2]+data[3];
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 5+21+15);
    }

    // 4.2.2 dominators
    @Test
    public void testFunction15() {
        String src = """
                func bar(data: [Int]) {
                    var j = 5
                    if (data[0])
                        data[1] = j * 10
                    else
                        data[2] = j * 15
                    data[3] = j * 21    
                }
                func foo()->Int {
                    var data = new [Int] {0,0,0,0}
                    bar(data)
                    return data[0]+data[1]+data[2]+data[3];
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 5*15+5*21);
    }

    // 4.2.3 DAGs
    @Test
    public void testFunction16() {
        String src = """
                func bar(data: [Int]) {
                    var j: Int
                    if (data[0]) {
                        j = 5
                        data[1] = 10
                    }
                    else {
                        data[2] = 15
                        j = 5
                    }
                    data[3] = j + 21
                }
                func foo()->Int {
                    var data = new [Int] {1,0,0,0}
                    bar(data)
                    return data[0]+data[1]+data[2]+data[3]
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 1+10+5+21);
    }

    // 4.2.3 DAGs
    // Doesn't give the outcome
    @Test
    public void testFunction17() {
        String src = """
                func bar(data: [Int]) {
                    var j: Int
                    var k: Int
                    if (data[0]) {
                        j = 4
                        k = 6
                        data[1] = j
                    }
                    else {
                        j = 7
                        k = 3
                        data[2] = k
                    }
                    data[3] = (j+k) * 21
                }
                func foo()->Int {
                    var data = new [Int] {1,0,0,0}
                    bar(data)
                    return data[0]+data[1]+data[2]+data[3]
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 1+4+210);
    }

    // 4.2.4 Loops
    @Test
    public void testFunction18() {
        String src = """
                func bar(data: [Int]) {
                    var i: Int
                    var stop = data[0]
                    var j = 21
                    i = 1
                    while ( i < stop ) {
                        j = (j - 20) * 21;
                        i = i + 1
                    }
                    data[1] = j
                    data[2] = i
                }
                func foo()->Int {
                    var data = new [Int] {2,0,0}
                    bar(data)
                    return data[0]+data[1]+data[2];
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 2+21+2);
    }

    // 4.3 Conditional constants
    @Test
    public void testFunction19() {
        String src = """
                func bar(data: [Int]) {
                    var j = 1
                    if (j) j = 10
                    else j = data[0]
                    data[0] = j * 21 + data[1]
                }
                func foo()->Int {
                    var data = new [Int] {2,3}
                    bar(data)
                    return data[0]+data[1];
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 213+3);
    }

    // 4.4 Conditional based assertions
    @Test
    public void testFunction20() {
        String src = """
                func bar(data: [Int]) {
                    var j = data[0]
                    if (j == 5)
                        j = j * 21 + 25 / j
                    data[1] = j
                }
                func foo()->Int {
                    var data = new [Int] {5,3}
                    bar(data)
                    return data[0]+data[1];
                }
                """;
        var value = compileAndRun(src, "foo", Options.OPT);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Value.IntegerValue integerValue
                && integerValue.value == 5+(5*21+25/5));
    }

}
