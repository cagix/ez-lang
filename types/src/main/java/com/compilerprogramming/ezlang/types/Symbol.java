package com.compilerprogramming.ezlang.types;

/**
 * A symbol is something that has a name and a type.
 */
public abstract class Symbol {

    public final String name;
    public EZType type;

    protected Symbol(String name, EZType type) {
        this.name = name;
        this.type = type;
    }

    public static class TypeSymbol extends Symbol {
        public TypeSymbol(String name, EZType type) {
            super(name, type);
        }
    }

    public static class FunctionTypeSymbol extends Symbol {
        public final Object functionDecl;
        public FunctionTypeSymbol(String name, EZType.EZTypeFunction type, Object functionDecl) {
            super(name, type);
            this.functionDecl = functionDecl;
        }
        public Object code() {
            EZType.EZTypeFunction function = (EZType.EZTypeFunction) type;
            return function.code;
        }
    }

    public static class VarSymbol extends Symbol {
        // Values assigned by bytecode compiler
        public int regNumber;
        public VarSymbol(String name, EZType type) {
            super(name, type);
        }
    }

    public static class ParameterSymbol extends VarSymbol {
        public ParameterSymbol(String name, EZType type) {
            super(name, type);
        }
    }
}
