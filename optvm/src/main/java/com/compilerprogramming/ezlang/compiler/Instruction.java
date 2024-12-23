package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Instruction {

    static final int I_NOOP = 0;
    static final int I_MOVE = 1;
    static final int I_RET  = 2;
    static final int I_UNARY = 3;
    static final int I_BINARY = 4;
    static final int I_BR = 5;
    static final int I_CBR = 6;
    static final int I_ARG = 7;
    static final int I_CALL = 8;
    static final int I_PHI = 9;
    static final int I_NEW_ARRAY = 10;
    static final int I_NEW_STRUCT = 11;
    static final int I_ARRAY_STORE = 12;
    static final int I_ARRAY_LOAD = 13;
    static final int I_ARRAY_APPEND = 14;
    static final int I_FIELD_GET = 15;
    static final int I_FIELD_SET = 16;

    public final int opcode;
    public Operand.RegisterOperand def;
    public Operand[] uses;

    public Instruction(int opcode, Operand... uses) {
        this.opcode = opcode;
        this.def = null;
        this.uses = uses;
    }
    public Instruction(int opcode, Operand.RegisterOperand def, Operand... uses) {
        this.opcode = opcode;
        this.def = def;
        this.uses = uses;
    }

    public boolean isTerminal() { return false; }
    @Override
    public String toString() {
        return toStr(new StringBuilder()).toString();
    }

    public boolean definesVar() { return def != null; }
    public Register def() { return def != null ? def.reg: null; }

    public List<Register> uses() {
        List<Register> useList = null;
        for (int i = 0; i < uses.length; i++) {
            Operand operand = uses[i];
            if (operand != null && operand instanceof Operand.RegisterOperand registerOperand) {
                if (useList == null) useList = new ArrayList<>();
                useList.add(registerOperand.reg);
            }
        }
        if (useList == null) useList = Collections.emptyList();
        return useList;
    }
    public void replaceDef(Register newReg) {
        if (def == null) throw new IllegalStateException();
        def.replaceRegister(newReg);
    }
    public void replaceUses(Register[] newUses) {
        int j = 0;
        for (int i = 0; i < uses.length; i++) {
            Operand use = uses[i];
            if (use != null && use instanceof Operand.RegisterOperand registerOperand) {
                registerOperand.replaceRegister(newUses[j++]);
            }
        }
    }
    public boolean replaceUse(Register source, Register target) {
        boolean replaced = false;
        for (int i = 0; i < uses.length; i++) {
            Operand operand = uses[i];
            if (operand != null && operand instanceof Operand.RegisterOperand registerOperand && registerOperand.reg.id == source.id) {
                registerOperand.replaceRegister(target);
                replaced = true;
            }
        }
        return replaced;
    }

    public static class NoOp extends Instruction {
        public NoOp() {
            super(I_NOOP);
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("noop");
        }
    }

    public static class Move extends Instruction {
        public Move(Operand from, Operand to) {
            super(I_MOVE, (Operand.RegisterOperand) to, from);
        }
        public Operand from() { return uses[0]; }
        public Operand.RegisterOperand to() { return def; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(to()).append(" = ").append(from());
        }
    }

    public static class NewArray extends Instruction {
        public final Type.TypeArray type;
        public NewArray(Type.TypeArray type, Operand.RegisterOperand destOperand) {
            super(I_NEW_ARRAY, destOperand);
            this.type = type;
        }
        public Operand.RegisterOperand destOperand() { return def; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(def)
                    .append(" = ")
                    .append("New(")
                    .append(type)
                    .append(")");
        }
    }

    public static class NewStruct extends Instruction {
        public final Type.TypeStruct type;
        public NewStruct(Type.TypeStruct type, Operand.RegisterOperand destOperand) {
            super(I_NEW_STRUCT, destOperand);
            this.type = type;
        }
        public Operand.RegisterOperand destOperand() { return def; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(def)
                    .append(" = ")
                    .append("New(")
                    .append(type)
                    .append(")");
        }
    }

    public static class ArrayLoad extends Instruction {
        public ArrayLoad(Operand.LoadIndexedOperand from, Operand.RegisterOperand to) {
            super(I_ARRAY_LOAD, to, from.arrayOperand, from.indexOperand);
        }
        public Operand arrayOperand() { return uses[0]; }
        public Operand indexOperand() { return uses[1]; }
        public Operand.RegisterOperand destOperand() { return def; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(destOperand())
                    .append(" = ")
                    .append(arrayOperand())
                    .append("[")
                    .append(indexOperand())
                    .append("]");
        }
    }

    public static class ArrayStore extends Instruction {
        public ArrayStore(Operand from, Operand.LoadIndexedOperand to) {
            super(I_ARRAY_STORE, (Operand.RegisterOperand) null, to.arrayOperand, to.indexOperand, from);
        }
        public Operand arrayOperand() { return uses[0]; }
        public Operand indexOperand() { return uses[1]; }
        public Operand sourceOperand() { return uses[2]; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb
                    .append(arrayOperand())
                    .append("[")
                    .append(indexOperand())
                    .append("] = ")
                    .append(sourceOperand());
        }
    }

    public static class GetField extends Instruction {
        public final String fieldName;
        public final int fieldIndex;
        public GetField(Operand.LoadFieldOperand from, Operand.RegisterOperand to) {
            super(I_FIELD_GET, to, from.structOperand);
            this.fieldName = from.fieldName;
            this.fieldIndex = from.fieldIndex;
        }
        public Operand structOperand() { return uses[0]; }
        public Operand.RegisterOperand destOperand() { return def; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(def)
                    .append(" = ")
                    .append(uses[0])
                    .append(".")
                    .append(fieldName);
        }
    }

    public static class SetField extends Instruction {
        public final String fieldName;
        public final int fieldIndex;
        public SetField(Operand from,Operand.LoadFieldOperand to) {
            super(I_FIELD_SET, (Operand.RegisterOperand) null, to.structOperand, from);
            this.fieldName = to.fieldName;
            this.fieldIndex = to.fieldIndex;
        }
        public Operand structOperand() { return uses[0]; }
        public Operand sourceOperand() { return uses[1]; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb
                    .append(structOperand())
                    .append(".")
                    .append(fieldName)
                    .append(" = ")
                    .append(sourceOperand());
        }
    }
    public static class Ret extends Instruction {
        public Ret(Operand value) {
            super(I_RET, (Operand.RegisterOperand) null, value);
        }
        public Operand value() { return uses[0]; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append("ret");
            if (uses[0] != null)
                sb.append(" ").append(value());
            return sb;
        }
    }
    public static class Unary extends Instruction {
        public final String unop;
        public Unary(String unop, Operand.RegisterOperand result, Operand operand) {
            super(I_UNARY, result, operand);
            this.unop = unop;
        }
        public Operand.RegisterOperand result() { return def; }
        public Operand operand() { return uses[0]; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(result()).append(" = ").append(unop).append(operand());
        }
    }

    public static class Binary extends Instruction {
        public final String binOp;
        public Binary(String binop, Operand.RegisterOperand result, Operand left, Operand right) {
            super(I_BINARY, result, left, right);
            this.binOp = binop;
        }
        public Operand.RegisterOperand result() { return def; }
        public Operand left() { return uses[0]; }
        public Operand right() { return uses[1]; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(def).append(" = ").append(uses[0]).append(binOp).append(uses[1]);
        }
    }

    public static class AStoreAppend extends Instruction {
        public AStoreAppend(Operand.RegisterOperand array, Operand value) {
            super(I_ARRAY_APPEND, (Operand.RegisterOperand) null, array, value);
        }
        public Operand.RegisterOperand array() { return (Operand.RegisterOperand) uses[0]; }
        public Operand value() { return uses[1]; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(uses[0]).append(".append(").append(uses[1]).append(")");
        }
    }

    public static class ConditionalBranch extends Instruction {
        public final BasicBlock trueBlock;
        public final BasicBlock falseBlock;
        public ConditionalBranch(BasicBlock currentBlock, Operand condition, BasicBlock trueBlock, BasicBlock falseBlock) {
            super(I_CBR, (Operand.RegisterOperand) null, condition);
            this.trueBlock = trueBlock;
            this.falseBlock = falseBlock;
            currentBlock.addSuccessor(trueBlock);
            currentBlock.addSuccessor(falseBlock);
        }
        public Operand condition() { return uses[0]; }
        @Override
        public boolean isTerminal() {
            return true;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("if ").append(condition()).append(" goto L").append(trueBlock.bid).append(" else goto L").append(falseBlock.bid);
        }
    }

    public static class Call extends Instruction {
        public final Type.TypeFunction callee;
        public final int newbase;
        public Call(int newbase, Operand.RegisterOperand returnOperand, Type.TypeFunction callee, Operand.RegisterOperand... args) {
            super(I_CALL, returnOperand, args);
            this.callee = callee;
            this.newbase = newbase;
        }
        public Operand.RegisterOperand returnOperand() { return def; }
        public Operand[] args() { return uses; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            if (def != null) {
                sb.append(def).append(" = ");
            }
            sb.append("call ").append(callee);
            if (uses.length > 0)
                sb.append(" params ");
            for (int i = 0; i < uses.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(uses[i]);
            }
            return sb;
        }
    }

    public static class Jump extends Instruction {
        public final BasicBlock jumpTo;
        public Jump(BasicBlock jumpTo) {
            super(I_BR);
            this.jumpTo = jumpTo;
        }
        @Override
        public boolean isTerminal() {
            return true;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("goto ").append(" L").append(jumpTo.bid);
        }
    }

    /**
     * Phi does not generate uses or defs directly, instead
     * they are treated as a special case.
     * To avoid bugs we do not use the def or uses.
     */
    public static class Phi extends Instruction {
        public Register value;
        public final Register[] inputs;
        public Phi(Register value, List<Register> inputs) {
            super(I_PHI);
            this.value = value;
            this.inputs = inputs.toArray(new Register[inputs.size()]);
        }
        public void replaceInput(int i, Register newReg) {
            inputs[i] = newReg;
        }
        public Register input(int i) {
            return inputs[i];
        }
        @Override
        public Register def() {
            throw new UnsupportedOperationException();
        }
        @Override
        public void replaceDef(Register newReg) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean definesVar() {
            return false;
        }
        public Register value() {
            return value;
        }
        public void replaceValue(Register newReg) {
            this.value = newReg;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append(value().name()).append(" = phi(");
            for (int i = 0; i < inputs.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(inputs[i].name());
            }
            sb.append(")");
            return sb;
        }
    }

    public static class ArgInstruction extends Instruction {
        public ArgInstruction(Operand.RegisterOperand arg) {
            super(I_ARG, arg);
        }
        public Operand.RegisterOperand arg() { return def; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("arg ").append(arg());
        }
    }

    public abstract StringBuilder toStr(StringBuilder sb);
}
