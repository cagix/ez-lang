package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

public abstract class Instruction {

    public boolean isTerminal() {
        return false;
    }
    @Override
    public String toString() {
        return toStr(new StringBuilder()).toString();
    }

    public static class Move extends Instruction {
        public final Operand from;
        public final Operand to;
        public Move(Operand from, Operand to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(to).append(" = ").append(from);
        }
    }

    public static class NewArray extends Instruction {
        public final Type.TypeArray type;
        public final Operand.RegisterOperand destOperand;
        public NewArray(Type.TypeArray type, Operand.RegisterOperand destOperand) {
            this.type = type;
            this.destOperand = destOperand;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(destOperand)
                    .append(" = ")
                    .append("New(")
                    .append(type)
                    .append(")");
        }
    }

    public static class NewStruct extends Instruction {
        public final Type.TypeStruct type;
        public final Operand.RegisterOperand destOperand;
        public NewStruct(Type.TypeStruct type, Operand.RegisterOperand destOperand) {
            this.type = type;
            this.destOperand = destOperand;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(destOperand)
                    .append(" = ")
                    .append("New(")
                    .append(type)
                    .append(")");
        }
    }

    public static class ArrayLoad extends Instruction {
        public final Operand arrayOperand;
        public final Operand indexOperand;
        public final Operand.RegisterOperand destOperand;
        public ArrayLoad(Operand.LoadIndexedOperand from, Operand.RegisterOperand to) {
            arrayOperand = from.arrayOperand;
            indexOperand = from.indexOperand;
            destOperand = to;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(destOperand)
                    .append(" = ")
                    .append(arrayOperand)
                    .append("[")
                    .append(indexOperand)
                    .append("]");
        }
    }

    public static class ArrayStore extends Instruction {
        public final Operand arrayOperand;
        public final Operand indexOperand;
        public final Operand sourceOperand;
        public ArrayStore(Operand from, Operand.LoadIndexedOperand to) {
            arrayOperand = to.arrayOperand;
            indexOperand = to.indexOperand;
            sourceOperand = from;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb
                    .append(arrayOperand)
                    .append("[")
                    .append(indexOperand)
                    .append("] = ")
                    .append(sourceOperand);
        }
    }

    public static class GetField extends Instruction {
        public final Operand structOperand;
        public final String fieldName;
        public final int fieldIndex;
        public final Operand.RegisterOperand destOperand;
        public GetField(Operand.LoadFieldOperand from, Operand.RegisterOperand to)
        {
            this.structOperand = from.structOperand;
            this.fieldName = from.fieldName;
            this.fieldIndex = from.fieldIndex;
            this.destOperand = to;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(destOperand)
                    .append(" = ")
                    .append(structOperand)
                    .append(".")
                    .append(fieldName);
        }
    }

    public static class SetField extends Instruction {
        public final Operand structOperand;
        public final String fieldName;
        public final int fieldIndex;
        public final Operand sourceOperand;
        public SetField(Operand from,Operand.LoadFieldOperand to)
        {
            this.structOperand = to.structOperand;
            this.fieldName = to.fieldName;
            this.fieldIndex = to.fieldIndex;
            this.sourceOperand = from;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb
                    .append(structOperand)
                    .append(".")
                    .append(fieldName)
                    .append(" = ")
                    .append(sourceOperand);
        }
    }
    public static class Return extends Move {
        public Return(Operand from) {
            super(from, new Operand.ReturnRegisterOperand());
        }
    }
    public static class Unary extends Instruction {
        public final String unop;
        public final Operand.RegisterOperand result;
        public final Operand operand;
        public Unary(String unop, Operand.RegisterOperand result, Operand operand) {
            this.unop = unop;
            this.result = result;
            this.operand = operand;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(result).append(" = ").append(unop).append(operand);
        }
    }

    public static class Binary extends Instruction {
        public final String binOp;
        public final Operand.RegisterOperand result;
        public final Operand left;
        public final Operand right;
        public Binary(String binop, Operand.RegisterOperand result, Operand left, Operand right) {
            this.binOp = binop;
            this.result = result;
            this.left = left;
            this.right = right;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(result).append(" = ").append(left).append(binOp).append(right);
        }
    }

    public static class AStoreAppend extends Instruction {
        public final Operand.RegisterOperand array;
        public final Operand value;
        public AStoreAppend(Operand.RegisterOperand array, Operand value) {
            this.array = array;
            this.value = value;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append(array).append(".append(").append(value).append(")");
        }
    }

    public static class ConditionalBranch extends Instruction {
        public final Operand condition;
        public final BasicBlock trueBlock;
        public final BasicBlock falseBlock;
        public ConditionalBranch(BasicBlock currentBlock, Operand condition, BasicBlock trueBlock, BasicBlock falseBlock) {
            this.condition = condition;
            this.trueBlock = trueBlock;
            this.falseBlock = falseBlock;
            currentBlock.addSuccessor(trueBlock);
            currentBlock.addSuccessor(falseBlock);
        }
        @Override
        public boolean isTerminal() {
            return true;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("if ").append(condition).append(" goto L").append(trueBlock.bid).append(" else goto L").append(falseBlock.bid);
        }
    }

    public static class Call extends Instruction {
        public final Type.TypeFunction callee;
        public final Operand.RegisterOperand[] args;
        public final Operand.RegisterOperand returnOperand;
        public final int newbase;
        public Call(int newbase, Operand.RegisterOperand returnOperand, Type.TypeFunction callee, Operand.RegisterOperand... args) {
            this.returnOperand = returnOperand;
            this.callee = callee;
            this.args = args;
            this.newbase = newbase;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            if (returnOperand != null) {
                sb.append(returnOperand).append(" = ");
            }
            sb.append("call ").append(callee);
            if (args.length > 0)
                sb.append(" params ");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i]);
            }
            return sb;
        }
    }

    public static class Jump extends Instruction {
        public final BasicBlock jumpTo;
        public Jump(BasicBlock jumpTo) {
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
