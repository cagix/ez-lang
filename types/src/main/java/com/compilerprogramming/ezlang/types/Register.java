package com.compilerprogramming.ezlang.types;

import java.util.Objects;

public class Register {
    /**
     * Slot in the function's frame
     */
    public final int slot;
    /**
     * Unique virtual ID
     */
    public final int id;
    public final int ssaVersion;
    public final String name;
    public final Type type;

    public Register(int slot, int id, String name, Type type) {
        this(slot, id, name, type, 0);
    }
    public Register(int slot, int id, String name, Type type, int ssaVersion) {
        this.slot = slot;
        this.id = id;
        this.name = name;
        this.type = type;
        this.ssaVersion = ssaVersion;
    }
    public Register cloneWithVersion(int ssaVersion) {
        return new Register(this.slot, this.id, this.name+"_"+ssaVersion, this.type, ssaVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Register register = (Register) o;
        return id == register.id && ssaVersion == register.ssaVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ssaVersion);
    }
}
