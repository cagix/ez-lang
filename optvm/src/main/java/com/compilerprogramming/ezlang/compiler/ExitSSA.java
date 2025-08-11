package com.compilerprogramming.ezlang.compiler;

import java.util.*;

/**
 * Converts from SSA form to non-SSA form.
 */
public class ExitSSA {
    public ExitSSA(CompiledFunction function, EnumSet<Options> options) {
        if (options.contains(Options.SSA_DESTRUCTION_BOISSINOT_NOCOALESCE))
            new ExitSSABoissinotNoCoalesce(function,options);
        else
            new ExitSSABriggs(function,options);
    }
}
