package com.compilerprogramming.ezlang.compiler;

import java.util.EnumSet;

public enum Options {
    OPTIMIZE,
    SCCP,
    CCP, // constant comparison propagation
    REGALLOC,
    DUMP_INITIAL_IR,
    DUMP_PRE_SSA_DOMTREE,
    DUMP_SSA_IR,
    DUMP_SCCP_PREAPPLY,
    DUMP_SCCP_POSTAPPLY,
    DUMP_CCP_POSTAPPLY,
    DUMP_SSA_LIVENESS,
    DUMP_SSA_DOMTREE,
    DUMP_POST_SSA_IR,
    DUMP_INTERFERENCE_GRAPH,
    DUMP_CHAITIN_COALESCE,
    DUMP_POST_CHAITIN_IR;

    public static final EnumSet<Options> NONE = EnumSet.noneOf(Options.class);
    public static final EnumSet<Options> OPT = EnumSet.of(Options.OPTIMIZE,Options.SCCP,Options.CCP,Options.REGALLOC);
    public static final EnumSet<Options> OPT_VERBOSE = EnumSet.allOf(Options.class);
}
