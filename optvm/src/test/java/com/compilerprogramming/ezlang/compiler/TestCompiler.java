package com.compilerprogramming.ezlang.compiler;

import org.junit.Assert;
import org.junit.Test;

public class TestCompiler {

    String compileSrc(String src) {
        var compiler = new Compiler();
        var typeDict = compiler.compileSrc(src);
        return compiler.dumpIR(typeDict);
    }

    @Test
    public void testFunction1() {
        String src = """
                func foo(n: Int)->Int {
                    return 1;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    ret 1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction2() {
        String src = """
                func foo(n: Int)->Int {
                    return -1;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    ret -1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction3() {
        String src = """
                func foo(n: Int)->Int {
                    return n;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    ret n
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction4() {
        String src = """
                func foo(n: Int)->Int {
                    return -n;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    %t1 = -n
    ret %t1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction5() {
        String src = """
                func foo(n: Int)->Int {
                    return n+1;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    %t1 = n+1
    ret %t1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction6() {
        String src = """
                func foo(n: Int)->Int {
                    return 1+1;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    ret 2
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction7() {
        String src = """
                func foo(n: Int)->Int {
                    return 1+1-1;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    ret 1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction8() {
        String src = """
                func foo(n: Int)->Int {
                    return 2==2;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    ret 1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction9() {
        String src = """
                func foo(n: Int)->Int {
                    return 1!=1;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    ret 0
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction10() {
        String src = """
                func foo(n: [Int])->Int {
                    return n[0];
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    %t1 = n[0]
    ret %t1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction11() {
        String src = """
                func foo(n: [Int])->Int {
                    return n[0]+n[1];
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    %t1 = n[0]
    %t2 = n[1]
    %t3 = %t1+%t2
    ret %t3
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction12() {
        String src = """
                func foo()->[Int] {
                    return new [Int] { 1, 2, 3 };
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    %t0 = New([Int])
    %t0.append(1)
    %t0.append(2)
    %t0.append(3)
    ret %t0
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction13() {
        String src = """
                func foo(n: Int) -> [Int] {
                    return new [Int] { n };
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    %t1 = New([Int])
    %t1.append(n)
    ret %t1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction14() {
        String src = """
                func add(x: Int, y: Int) -> Int {
                    return x+y;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg x
    arg y
    %t2 = x+y
    ret %t2
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction15() {
        String src = """
                struct Person
                {
                    var age: Int
                    var children: Int
                }
                func foo(p: Person) -> Person {
                    p.age = 10;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg p
    p.age = 10
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction16() {
        String src = """
                struct Person
                {
                    var age: Int
                    var children: Int
                }
                func foo() -> Person {
                    return new Person { age=10, children=0 };
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    %t0 = New(Person)
    %t0.age = 10
    %t0.children = 0
    ret %t0
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction17() {
        String src = """
                func foo(array: [Int]) {
                    array[0] = 1
                    array[1] = 2
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg array
    array[0] = 1
    array[1] = 2
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction18() {
        String src = """
                func min(x: Int, y: Int) -> Int {
                    if (x < y)
                        return x;
                    return y;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg x
    arg y
    %t2 = x<y
    if %t2 goto L2 else goto L3
L2:
    ret x
    goto  L1
L1:
L3:
    ret y
    goto  L1
""", result);
    }

    @Test
    public void testFunction19() {
        String src = """
                func loop() {
                    while (1)
                        return;
                    return;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
                L0:
                    goto  L2
                L2:
                    if 1 goto L3 else goto L4
                L3:
                    goto  L1
                L1:
                L4:
                    goto  L1
                """, result);
    }

    @Test
    public void testFunction20() {
        String src = """
                func loop() {
                    while (1)
                        break;
                    return;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
                L0:
                    goto  L2
                L2:
                    if 1 goto L3 else goto L4
                L3:
                    goto  L4
                L4:
                    goto  L1
                L1:
                """, result);
    }

    @Test
    public void testFunction21() {
        String src = """
                func loop(n: Int) {
                    while (n > 0) {
                        n = n - 1;
                    }
                    return;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg n
    goto  L2
L2:
    %t1 = n>0
    if %t1 goto L3 else goto L4
L3:
    %t2 = n-1
    n = %t2
    goto  L2
L4:
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction22() {
        String src = """
                func foo() {}
                func bar() { foo(); }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    goto  L1
L1:
L0:
    call foo
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction23() {
        String src = """
                func foo(x: Int, y: Int) {}
                func bar() { foo(1,2); }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg x
    arg y
    goto  L1
L1:
L0:
    %t0 = 1
    %t1 = 2
    call foo params %t0, %t1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction24() {
        String src = """
                func foo(x: Int, y: Int)->Int { return x+y; }
                func bar()->Int { var t = foo(1,2); return t+1; }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg x
    arg y
    %t2 = x+y
    ret %t2
    goto  L1
L1:
L0:
    %t1 = 1
    %t2 = 2
    %t3 = call foo params %t1, %t2
    t = %t3
    %t4 = t+1
    ret %t4
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction25() {
        String src = """
                struct Person
                {
                    var age: Int
                    var children: Int
                }
                func foo(p: Person) -> Int {
                    return p.age;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg p
    %t1 = p.age
    ret %t1
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction26() {
        String src = """
                struct Person
                {
                    var age: Int
                    var parent: Person
                }
                func foo(p: Person) -> Int {
                    return p.parent.age;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg p
    %t1 = p.parent
    %t2 = %t1.age
    ret %t2
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction27() {
        String src = """
                struct Person
                {
                    var age: Int
                    var parent: Person
                }
                func foo(p: [Person], i: Int) -> Int {
                    return p[i].parent.age;
                }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg p
    arg i
    %t2 = p[i]
    %t3 = %t2.parent
    %t4 = %t3.age
    ret %t4
    goto  L1
L1:
""", result);
    }

    @Test
    public void testFunction28() {
        String src = """
                func foo(x: Int, y: Int)->Int { return x+y; }
                func bar(a: Int)->Int { var t = foo(a,2); return t+1; }
                """;
        String result = compileSrc(src);
        Assert.assertEquals("""
L0:
    arg x
    arg y
    %t2 = x+y
    ret %t2
    goto  L1
L1:
L0:
    arg a
    %t2 = a
    %t3 = 2
    %t4 = call foo params %t2, %t3
    t = %t4
    %t5 = t+1
    ret %t5
    goto  L1
L1:
""", result);
    }
}
