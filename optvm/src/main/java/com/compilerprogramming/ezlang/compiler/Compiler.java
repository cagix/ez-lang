package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.lexer.Lexer;
import com.compilerprogramming.ezlang.parser.Parser;
import com.compilerprogramming.ezlang.semantic.SemaAssignTypes;
import com.compilerprogramming.ezlang.semantic.SemaDefineTypes;
import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.Type;
import com.compilerprogramming.ezlang.types.TypeDictionary;

public class Compiler {

    private void compile(TypeDictionary typeDictionary, boolean opt) {
        for (Symbol symbol: typeDictionary.getLocalSymbols()) {
            if (symbol instanceof Symbol.FunctionTypeSymbol functionSymbol) {
                Type.TypeFunction functionType = (Type.TypeFunction) functionSymbol.type;
                var function = new CompiledFunction(functionSymbol);
                functionType.code = function;
                if (opt) {
                    new Optimizer().optimize(function);
                }
            }
        }
    }
    public TypeDictionary compileSrc(String src) {
        return compileSrc(src, false);
    }
    public TypeDictionary compileSrc(String src, boolean opt) {
        Parser parser = new Parser();
        var program = parser.parse(new Lexer(src));
        var typeDict = new TypeDictionary();
        var sema = new SemaDefineTypes(typeDict);
        sema.analyze(program);
        var sema2 = new SemaAssignTypes(typeDict);
        sema2.analyze(program);
        compile(typeDict, opt);
        return typeDict;
    }
    public static String dumpIR(TypeDictionary typeDictionary) {
        return dumpIR(typeDictionary, false);
    }
    public static String dumpIR(TypeDictionary typeDictionary, boolean verbose) {
        StringBuilder sb = new StringBuilder();
        for (Symbol s: typeDictionary.bindings.values()) {
            if (s instanceof Symbol.FunctionTypeSymbol f) {
                var function = (CompiledFunction) f.code();
                function.toStr(sb, verbose);
            }
        }
        return sb.toString();
    }
}
