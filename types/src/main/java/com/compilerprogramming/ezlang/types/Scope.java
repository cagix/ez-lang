package com.compilerprogramming.ezlang.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Scope {

    public final Map<String, Symbol> bindings = new LinkedHashMap<>();
    public final Scope parent;
    public final List<Scope> children = new ArrayList<>();

    // values assigned by compiler
    public int maxReg;
    public final boolean isFunctionParameterScope;

    public Scope(Scope parent, boolean isFunctionParameterScope) {
        this.parent = parent;
        this.isFunctionParameterScope = isFunctionParameterScope;
        if (parent != null)
            parent.children.add(this);
    }

    public Scope(Scope parent) {
        this(parent, false);
    }

    public Symbol lookup(String name) {
        Symbol symbol = bindings.get(name);
        if (symbol == null && parent != null)
            symbol = parent.lookup(name);
        return symbol;
    }

    public Symbol localLookup(String name) {
        return bindings.get(name);
    }

    public Symbol install(String name, Symbol symbol) {
        bindings.put(name, symbol);
        return symbol;
    }

    public List<Symbol> getLocalSymbols() {
        return new ArrayList<>(bindings.values());
    }
}
