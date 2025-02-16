package com.compilerprogramming.ezlang.semantic;

import com.compilerprogramming.ezlang.exceptions.CompilerException;
import com.compilerprogramming.ezlang.lexer.Lexer;
import com.compilerprogramming.ezlang.parser.Parser;
import com.compilerprogramming.ezlang.types.TypeDictionary;
import org.junit.Assert;
import org.junit.Test;

public class TestSemaAssignTypes {

    static void analyze(String src, String symbolName, String typeSig) {
        Parser parser = new Parser();
        var program = parser.parse(new Lexer(src));
        var typeDict = new TypeDictionary();
        var sema = new SemaDefineTypes(typeDict);
        sema.analyze(program);
        var symbol = typeDict.lookup(symbolName);
        Assert.assertNotNull(symbol);
        Assert.assertEquals(typeSig, symbol.type.describe());
        var sema2 = new SemaAssignTypes(typeDict);
        sema2.analyze(program);
    }

    @Test
    public void test1() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a+b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test51() {
        String src = """
    func foo()->Int 
    {
       return 1+1;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test2() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a-b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test52() {
        String src = """
    func foo()->Int 
    {
       return 1-1;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }


    @Test
    public void test3() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a*b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test53() {
        String src = """
    func foo()->Int 
    {
       return 4*2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test4() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a/b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test54() {
        String src = """
    func foo()->Int 
    {
       return 4/2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }


    @Test
    public void test5() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a==b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }


    @Test
    public void test55() {
        String src = """
    func foo()->Int 
    {
       return 4==2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test6() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a!=b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test56() {
        String src = """
    func foo()->Int 
    {
       return 4!=2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }


    @Test
    public void test7() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a<b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }


    @Test
    public void test57() {
        String src = """
    func foo()->Int 
    {
       return 4<2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test8() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a<=b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test58() {
        String src = """
    func foo()->Int 
    {
       return 4<=2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test9() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a>b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test59() {
        String src = """
    func foo()->Int 
    {
       return 4>2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test10() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a>=b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test60() {
        String src = """
    func foo()->Int 
    {
       return 4>=2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test11() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a&&b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test61() {
        String src = """
    func foo()->Int 
    {
       return 4&&2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test12() {
        String src = """
    func foo(a: Int, b: Int)->Int 
    {
       return a||b;
    }
""";
        analyze(src, "foo", "func foo(a: Int,b: Int)->Int");
    }

    @Test
    public void test62() {
        String src = """
    func foo()->Int 
    {
       return 4||2;
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test
    public void test13() {
        String src = """
    struct Foo
    {
        var bar: [Int]
    }
    func foo()->Int 
    {
       var f: Foo
       f = new Foo{}
       return f.bar[0]
    }
""";
        analyze(src, "foo", "func foo()->Int");
    }

    @Test(expected = CompilerException.class)
    public void test14() {
        String src = """
    struct Foo
    {
        var bar: [Int]
    }
    func foo()
    {
       var f: Foo
       f = null
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test
    public void test15() {
        String src = """
    struct Foo
    {
        var bar: [Int]
    }
    func foo()
    {
       var f: Foo?
       f = null
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test(expected = CompilerException.class)
    public void test16() {
        String src = """
    func foo()
    {
       var f = null
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test(expected = CompilerException.class)
    public void test17() {
        String src = """
    func foo()
    {
       var f = new [Int] {null}
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test(expected = CompilerException.class)
    public void test18() {
        String src = """
    func foo()
    {
       var f = new [Int?] {null}
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test
    public void test19() {
        String src = """
    struct Foo { var bar: Int }
    func foo()
    {
       var f = new [Foo?] {null}
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test
    public void test20() {
        String src = """
    struct Foo { var bar: Int }
    func foo()
    {
       var f = new [Foo?] {new Foo{ bar = 1}}
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test(expected = CompilerException.class)
    public void test21() {
        String src = """
    struct Foo { var bar: Int }
    func foo()
    {
       var f = new [Foo?] {new Foo{ bar = null}}
    }
""";
        analyze(src, "foo", "func foo()");
    }

    @Test(expected = CompilerException.class)
    public void test22() {
        String src = """
    struct Foo { var bar: Int }
    func foo()
    {
       var f = new [Foo?]? {}
    }
""";
        analyze(src, "foo", "func foo()");
    }
}
