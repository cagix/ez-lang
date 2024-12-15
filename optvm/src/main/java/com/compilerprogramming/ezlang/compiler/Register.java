package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

import java.util.List;

/**
 * Virtual register represents values that are operands of the
 * IR.
 */
public class Register {
    /**
     * Unique virtual ID
     */
    private final int id;
    /**
     * The base name - for local variables and function params this should be the name
     * in the source program. For temps this is a made up name.
     * Does not include ssa version
     */
    private final String name;
    /**
     * The type of a register
     */
    public final Type type;

    public List<Instruction> uses;

    public Register(int id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Register register = (Register) o;
        return id == register.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String name() {
        return name;
    }
    public int nonSSAId() {
        return id;
    }

    /**
     * An SSA Register retains a reference to the original
     * id of the register, and adds a version number.
     */
    public static class SSARegister extends Register {
        public final int ssaVersion;
        public final int originalRegNumber;
        public SSARegister(Register original, int id, int version) {
            super(id, original.name+"_"+version, original.type);
            this.originalRegNumber = original.id;
            this.ssaVersion = version;
        }
        @Override
        public int nonSSAId() {
            return originalRegNumber;
        }
    }
}
