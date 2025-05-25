package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

/**
 * Represents a Virtual Register in the abstract machine.
 *
 * Virtual registers are used as operands in the instruction set of the abstract machine.
 * We try to meet several requirements.
 *
 * When executing instructions in the Interpreter, we require the notion of a frame slot - this
 * is the location of the register within the function's frame. Each executing function maps its slots to
 * an ExecutionStack, which is simply an array of Values. When the function executes, the location of a
 * register on the function's frame is given by the Register's slot plus the base offset of the function.
 * The base offset is the location where the function's first argument appears on the execution stack, and is
 * determined at runtime based on the call stack.
 *
 * In addition to the frame slot - for the purpose of SSA, each register must have a unique name. We use a
 * unique ID as a proxy for this name. During SSA transformation we assign new IDs as we rename variables, but
 * we also maintain the original ID of the register because the SSA algorithm needs to be able to track variables
 * by their original names.
 *
 * After SSA, during register allocation, we use graph coloring to assign slots to registers. We could rename
 * all registers in the instruction set again, but using a slot instead allows us to maintain the previously
 * generated name - this is mainly useful as we look for relations to the variables in the source program.
 *
 * @see RegisterPool
 * @see ChaitinGraphColoringRegisterAllocator
 * @see com.compilerprogramming.ezlang.compiler.Operand.RegisterOperand
 */
public class Register {
    /**
     * Unique virtual ID always unique
     */
    public final int id;
    /**
     * The base name - for local variables and function params this should be the name
     * in the source program. For temps this is a made up name. Name is suffixed with ssa version
     * during SSA transformation.
     *
     * Not unique.
     */
    protected final String name;
    /**
     * The type of the register
     */
    public final Type type;
    /**
     * The location of this register relative to the base
     * of the executing function. Multiple registers may share the same
     * frame slot because of different non-overlapping life times.
     */
    protected int frameSlot;

    public Register(int id, String name, Type type) {
        this(id,name,type,id);  // Initially frame slot is set to the unique ID
    }
    protected Register(int id, String name, Type type, int frameSlot) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.frameSlot = frameSlot;
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

    /**
     * The nonSSAID is valid as a frame slot prior to SSA conversion,
     * and following register assignment.
     * During SSA form this is not valid for registers that are instances of SSARegister.
     */
    public int nonSSAId() {
        //assert frameSlot >= 0; // assert inteferes with verbose display
        return frameSlot;
    }
    public void updateSlot(int slot) {
        this.frameSlot = slot;
    }
    public int frameSlot() { return frameSlot; }

    /**
     * An SSA Register retains a reference to the original
     * id of the register, and adds a version number.
     */
    public static class SSARegister extends Register {
        public final int ssaVersion;
        public final int originalRegNumber;
        public SSARegister(Register original, int id, int version) {
            super(id, original.name+"_"+version, original.type, -1);
            this.originalRegNumber = original.id;
            this.ssaVersion = version;
        }
        @Override
        public int nonSSAId() {
            return originalRegNumber;
        }

        @Override
        public String toString() {
            return "SSARegister{name=" + name + ", id=" + id + ", frameSlot=" + frameSlot + ", ssaVersion=" + ssaVersion + ", originalRegNumber=" + originalRegNumber + '}';
        }
    }

    @Override
    public String toString() {
        return "Register{name=" + name + ", id=" + id + ", frameSlot=" + frameSlot + "}";
    }
}
