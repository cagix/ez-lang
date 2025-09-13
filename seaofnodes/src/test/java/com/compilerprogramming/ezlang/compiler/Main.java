package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.compiler.codegen.CodeGen;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static final String PORTS = "com.compilerprogramming.ezlang.compiler.node.cpus";
    // Compile and run a simple program
    public static void main( String[] args ) throws Exception {
        // First arg is file, 2nd+ args are program args
        String src = Files.readString(Path.of(args[0]));
        CodeGen code = new CodeGen(src);
        code.parse().opto().typeCheck().GCM().localSched();
        System.out.println(code._stop);
        //System.out.println(Eval2.eval(code,arg,100000));
    }
}
