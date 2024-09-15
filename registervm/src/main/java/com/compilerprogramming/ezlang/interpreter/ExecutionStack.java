package com.compilerprogramming.ezlang.interpreter;

public class ExecutionStack {

    public Value[] stack;
    public int sp;

    public ExecutionStack(int maxStackSize) {
        this.stack = new Value[maxStackSize];
        this.sp = -1;
    }
}
