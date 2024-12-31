package com.compilerprogramming.ezlang.compiler;

public class Optimizer {

    public void optimize(CompiledFunction function) {
        new EnterSSA(function);
        new SparseConditionalConstantPropagation().constantPropagation(function);
        new ExitSSA(function);
        new ChaitinGraphColoringRegisterAllocator().assignRegisters(function, 64);
    }
}
