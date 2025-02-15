package com.compilerprogramming.ezlang.semantic;

import com.compilerprogramming.ezlang.exceptions.CompilerException;
import com.compilerprogramming.ezlang.lexer.Lexer;
import com.compilerprogramming.ezlang.parser.Parser;
import com.compilerprogramming.ezlang.types.TypeDictionary;
import org.junit.Assert;
import org.junit.Test;

public class TestSemaDefineTypes {

    private void analyze(String src, String symName, String typeSig) {
        Parser parser = new Parser();
        var program = parser.parse(new Lexer(src));
        var typeDict = new TypeDictionary();
        var sema = new SemaDefineTypes(typeDict);
        sema.analyze(program);
        var symbol = typeDict.lookup(symName);
        Assert.assertNotNull(symbol);
        Assert.assertEquals(typeSig, symbol.type.describe());
    }

    @Test
    public void test1() {
        String src = """
struct Tree {
    var left: Tree?
    var right: Tree?
}                """;
        analyze(src, "Tree", "struct Tree{left: Tree?;right: Tree?;}");
    }

    @Test
    public void test2() {
        String src = """
struct Tree {
    var left: Tree
    var right: Tree
}                """;
        analyze(src, "Tree", "struct Tree{left: Tree;right: Tree;}");
    }

    @Test
    public void test3() {
        String src = """
struct TreeArray {
    var data: [Tree]
}                """;
        analyze(src, "TreeArray", "struct TreeArray{data: [Tree];}");
    }

    @Test
    public void test4() {
        String src = """
struct TreeArray {
    var data: [Tree?]
}                """;
        analyze(src, "TreeArray", "struct TreeArray{data: [Tree?];}");
    }

    @Test
    public void test5() {
        String src = """
struct TreeArray {
    var data: [Tree?]?
}                """;
        analyze(src, "TreeArray", "struct TreeArray{data: [Tree?]?;}");
    }

    @Test
    public void test6() {
        String src = """
func print(t: Tree) {
}                """;
        analyze(src, "print", "func print(t: Tree)");
    }

    @Test
    public void test7() {
        String src = """
func makeTree()->Tree {
}                """;
        analyze(src, "makeTree", "func makeTree()->Tree");
    }

    @Test(expected = CompilerException.class)
    public void test8() {
        Parser parser = new Parser();
        String src = """
struct TreeArray {
    var data: [Int?]
}                """;
        var program = parser.parse(new Lexer(src));
        var typeDict = new TypeDictionary();
        var sema = new SemaDefineTypes(typeDict);
        sema.analyze(program);
    }

    @Test
    public void test9() {
        String src = """
struct TreeArray {
    var data: [Int]
}                """;
        analyze(src, "TreeArray", "struct TreeArray{data: [Int];}");
    }

}
