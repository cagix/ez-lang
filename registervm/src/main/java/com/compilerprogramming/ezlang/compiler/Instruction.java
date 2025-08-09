package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.EZType;

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
    static final int I_FIELD_GET = 14;
    static final int I_FIELD_SET = 15;

    public final int opcode;
    protected Operand.RegisterOperand def;
    protected Operand[] uses;

    protected Instruction(int opcode, Operand... uses) {
        this.opcode = opcode;
        this.def = null;
        this.uses = new Operand[uses.length];
        System.arraycopy(uses, 0, this.uses, 0, uses.length);
    }
    protected Instruction(int opcode, Operand.RegisterOperand def, Operand... uses) {
        this.opcode = opcode;
        this.def = def;
        this.uses = new Operand[uses.length];
        System.arraycopy(uses, 0, this.uses, 0, uses.length);
    }

    public boolean isTerminal() { return false; }
    @Override
    public String toString() {
        return toStr(new StringBuilder()).toString();
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
        public final EZType.EZTypeArray type;
        public NewArray(EZType.EZTypeArray type, Operand.RegisterOperand destOperand) {
            super(I_NEW_ARRAY, destOperand);
            this.type = type;
        }
        public NewArray(EZType.EZTypeArray type, Operand.RegisterOperand destOperand, Operand len) {
            super(I_NEW_ARRAY, destOperand, len);
            this.type = type;
        }
        public NewArray(EZType.EZTypeArray type, Operand.RegisterOperand destOperand, Operand len, Operand initValue) {
            super(I_NEW_ARRAY, destOperand, len, initValue);
            this.type = type;
        }
        public Operand len() { return uses.length > 0 ? uses[0] : null; }
        public Operand initValue() { return uses.length > 1 ? uses[1] : null; }
        public Operand.RegisterOperand destOperand() { return def; }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append(def)
                    .append(" = ")
                    .append("New(")
                    .append(type);
            if (len() != null)
                sb.append(", len=").append(len());
            if (initValue() != null)
                sb.append(", initValue=").append(initValue());
            return sb.append(")");
        }
    }

    public static class NewStruct extends Instruction {
        public final EZType.EZTypeStruct type;
        public NewStruct(EZType.EZTypeStruct type, Operand.RegisterOperand destOperand) {
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
        public final EZType.EZTypeFunction callee;
        public final int newbase;
        public Call(int newbase, Operand.RegisterOperand returnOperand, EZType.EZTypeFunction callee, Operand.RegisterOperand... args) {
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


    public abstract StringBuilder toStr(StringBuilder sb);
}
