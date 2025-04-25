package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;
import org.junit.Before;
import org.junit.Test;

import static com.compilerprogramming.ezlang.compiler.Main.PORTS;
import static org.junit.Assert.assertEquals;

public class TestSONTypes {

    String compileSrc(String src) {
        var compiler = new CodeGen(src);
        compiler.parse();
        return null;
    }

    @Test
    public void test1() {
        String src = """
struct T { var i: Int }
""";
        compileSrc(src);
    }

    @Test
    public void test2() {
        String src = """
struct T1 { var i: S }
struct S { var i: Int }
""";
        compileSrc(src);
    }

    @Test
    public void test3() {
        String src = """
struct T2 { var next: T2? }
""";
        compileSrc(src);
    }

    @Test
    public void test4() {
        String src = """
struct T3 { var arr: [Int] }
""";
        compileSrc(src);
    }

    @Test
    public void test5() {
        String src = """
func foo() {}
""";
        compileSrc(src);
    }

    @Test
    public void test6() {
        String src = """
func foo(a: Int) {}
""";
        compileSrc(src);
    }


    @Test
    public void test7() {
        String src = """
func foo(a: Int, b: Int)->Int {}
""";
        compileSrc(src);
    }

    @Test
    public void test8() {
        String src = """
func foo(a: Int, b: Int)->[Int] {}
""";
        compileSrc(src);
    }

    @Test
    public void test9() {
        String src = """
func foo(a: Int, b: Int)->[Int]? {}
""";
        compileSrc(src);
    }

    @Test
    public void test10() {
        String src = """
func foo(a: Int, b: Int)->Int { 
  return a+b;
}
""";
        compileSrc(src);
    }

    @Test
    public void test11() {
        String src = """
func foo()->[Int] { 
  return new [Int]{};
}
""";
        compileSrc(src);
    }

    @Test
    public void test12() {
        String src = """
func foo()->[Int] { 
  return new [Int]{1};
}
""";
        compileSrc(src);
    }

    @Test
    public void test13() {
        String src = """
func foo()->[Int] { 
  return new [Int]{42,84};
}
""";
        compileSrc(src);
    }

    @Test
    public void test14() {
        String src = """
struct T4 { var i: Int }
func foo()->T4 { 
  return new T4{i=1};
}
""";
        compileSrc(src);
    }

    @Test
    public void test15() {
        String src = """
struct T5 { var i: Int; var j: Int }
func foo()->T5 { 
  return new T5{i=23, j=32};
}
""";
        compileSrc(src);
    }

    @Test
    public void test16() {
        String src = """
func foo()->Int { 
  return new [Int]{42,84}[1];
}
""";
        compileSrc(src);
    }

    @Test
    public void test17() {
        String src = """
struct T5 { var i: Int; var j: Int }
func foo()->Int { 
  return new T5{i=23, j=32}.j;
}
""";
        compileSrc(src);
    }

    @Test
    public void test18() {
        String src = """
func bar()->Int { return 1 }
func foo()->Int { 
  return bar();
}
""";
        compileSrc(src);
    }

    @Test
    public void test19() {
        String src = """
func foo()->Int { 
  var x = 1
  return x
}
""";
        compileSrc(src);
    }

        static void testCPU( String src, String cpu, String os, int spills, String stop ) {
        CodeGen code = new CodeGen(src);
        code.parse().opto().typeCheck().loopTree().instSelect(cpu,os).GCM().localSched().regAlloc().encode();
        int delta = spills>>3;
        if( delta==0 ) delta = 1;
        assertEquals("Expect spills:",spills,code._regAlloc._spillScaled,delta);
        if( stop != null )
            assertEquals(stop, code._stop.toString());
        System.out.println(code.asm());
    }

    private static void testAllCPUs( String src, int spills, String stop ) {
        testCPU(src,"x86_64_v2", "SystemV",spills,stop);
        testCPU(src,"riscv"    , "SystemV",spills,stop);
        testCPU(src,"arm"      , "SystemV",spills,stop);
    }

    @Test
    public void test101() {
        testAllCPUs("""
                func main()->Int {
                    return 42
                }
                """, 0, null);
    }
}
