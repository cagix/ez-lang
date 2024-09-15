package com.compilerprogramming.ezlang.bytecode;

import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.Type;
import com.compilerprogramming.ezlang.types.TypeDictionary;

public class BytecodeCompiler {

    public void compile(TypeDictionary typeDictionary) {
        for (Symbol symbol: typeDictionary.getLocalSymbols()) {
            if (symbol instanceof Symbol.FunctionTypeSymbol functionSymbol) {
                Type.TypeFunction functionType = (Type.TypeFunction) functionSymbol.type;
                functionType.code = new BytecodeFunction(functionSymbol);
            }
        }
    }
}
