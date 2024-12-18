package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

import java.util.ArrayList;

/**
 * The RegisterPool is used when compiling functions
 * to assign IDs to registers. Initially the registers get
 * sequential IDs. For SSA registers we assign new IDs but also
 * retain the old ID and attach a version number - the old ID is
 * required because our SSA algo must be able to track each original
 * variable / register.
 */
public class RegisterPool {
    private final ArrayList<Register> registers = new ArrayList<>();
    public final Register returnRegister;

    public RegisterPool(String returnVarName, Type type) {
        if (type != null)
            returnRegister = newReg(returnVarName, type);
        else
            returnRegister = null;
    }
    public Register getReg(int regNumber) {
        return registers.get(regNumber);
    }
    public Register newReg(String baseName, Type type) {
        var id = registers.size();
        var reg = new Register(id, baseName, type);
        registers.add(reg);
        return reg;
    }
    public Register newTempReg(Type type) {
        var id = registers.size();
        var name = "%t"+id;
        var reg = new Register(id, name, type);
        registers.add(reg);
        return reg;
    }
    public Register newTempReg(String baseName, Type type) {
        var id = registers.size();
        var name = baseName+"_"+id;
        var reg = new Register(id, name, type);
        registers.add(reg);
        return reg;
    }

    public Register.SSARegister ssaReg(Register original, int version) {
        var id = registers.size();
        var reg = new Register.SSARegister(original, id, version);
        registers.add(reg);
        return reg;
    }
    public int numRegisters() {
        return registers.size();
    }

    public void toStr(StringBuilder sb) {
        for (Register reg : registers) {
            sb.append("Reg #").append(reg.id).append(" ").append(reg.name()).append("\n");
        }
    }
}
