package com.compilerprogramming.ezlang.compiler;

import java.util.EnumSet;

public class Optimizer {

    public void optimize(CompiledFunction function, EnumSet<Options> options) {
        if (options.contains(Options.OPTIMIZE)) {
            new EnterSSA(function, options);
            if (options.contains(Options.SCCP))
                new SparseConditionalConstantPropagation().constantPropagation(function).apply(options);
            new ExitSSA(function, options);
        }
        if (options.contains(Options.REGALLOC))
            new ChaitinGraphColoringRegisterAllocator().assignRegisters(function, 64, options);
    }
}
