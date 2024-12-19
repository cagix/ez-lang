package com.compilerprogramming.ezlang.compiler;

import java.util.BitSet;
import java.util.List;

public class LiveSet extends BitSet {
    public LiveSet(int numRegs) {
        super(numRegs);
    }
    public LiveSet dup() {
        return (LiveSet) clone();
    }
    public void live(Register r) {
        set(r.id, true);
    }
    public void dead(Register r) {
        set(r.id, false);
    }
    public void live(List<Register> regs) {
        for (Register r : regs) {
            live(r);
        }
    }
    public void add(Register r) {
        set(r.id, true);
    }
    public void remove(Register r) {
        set(r.id, false);
    }
    public void remove(List<Register> regs) {
        for (Register r : regs) {
            remove(r);
        }
    }
    public boolean isMember(Register r) {
        return get(r.id);
    }
    public LiveSet intersect(LiveSet other) {
        and(other);
        return this;
    }
    public LiveSet union(LiveSet other) {
        or(other);
        return this;
    }
    public LiveSet intersectNot(LiveSet other) {
        andNot(other);
        return this;
    }
}
