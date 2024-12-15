package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Instruction {
    public BasicBlock bb;

    public boolean isTerminal() {
        return false;
    }
    @Override
    public String toString() {
        return toStr(new StringBuilder()).toString();
    }

    public boolean definesVar() { return false; }
    public Register def() { return null; }

    public boolean usesVars() { return false; }
    public List<Register> uses() { return Collections.emptyList(); }
    public void replaceDef(Register newReg) {}
    public void replaceUses(Register[] newUses) {}

    public static class Move extends Instruction {
        public final Operand from;
        public final Operand to;
        public Move(Operand from, Operand to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean definesVar() {
            return true;
        }

        @Override
        public Register def() {
            if (to instanceof Operand.RegisterOperand registerOperand)
                return registerOperand.reg;
            throw new IllegalStateException();
        }

        @Override
        public void replaceDef(Register newReg) {
            this.to.replaceRegister(newReg);
        }

        @Override
        public boolean usesVars() {
            return (from instanceof Operand.RegisterOperand);
        }

        @Override
        public List<Register> uses() {
            if (from instanceof Operand.RegisterOperand registerOperand)
                return List.of(registerOperand.reg);
            return super.uses();
        }

        @Override
        public void replaceUses(Register[] newUses) {
            from.replaceRegister(newUses[0]);
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
        public boolean definesVar() {
            return true;
        }

        @Override
        public Register def() {
            return destOperand.reg;
        }

        @Override
        public void replaceDef(Register newReg) {
            destOperand.replaceRegister(newReg);
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
        public boolean definesVar() {
            return true;
        }

        @Override
        public Register def() {
            return destOperand.reg;
        }

        @Override
        public void replaceDef(Register newReg) {
            destOperand.replaceRegister(newReg);
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
        public boolean definesVar() {
            return true;
        }
        @Override
        public Register def() {
            return destOperand.reg;
        }
        @Override
        public void replaceDef(Register newReg) {
            destOperand.replaceRegister(newReg);
        }

        @Override
        public boolean usesVars() {
            return true;
        }

        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            if (arrayOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            if (indexOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            return usesList;
        }

        @Override
        public void replaceUses(Register[] newUses) {
            int i = 0;
            if (arrayOperand instanceof Operand.RegisterOperand) {
                arrayOperand.replaceRegister(newUses[i++]);
            }
            if (indexOperand instanceof Operand.RegisterOperand) {
                indexOperand.replaceRegister(newUses[i]);
            }
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
        public boolean usesVars() {
            return true;
        }

        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            if (arrayOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            if (indexOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            if (sourceOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            return usesList;
        }

        @Override
        public void replaceUses(Register[] newUses) {
            int i = 0;
            if (arrayOperand instanceof Operand.RegisterOperand) {
                arrayOperand.replaceRegister(newUses[i++]);
            }
            if (indexOperand instanceof Operand.RegisterOperand) {
                indexOperand.replaceRegister(newUses[i++]);
            }
            if (sourceOperand instanceof Operand.RegisterOperand) {
                sourceOperand.replaceRegister(newUses[i]);
            }
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
        public boolean definesVar() {
            return true;
        }
        @Override
        public Register def() {
            return destOperand.reg;
        }

        @Override
        public void replaceDef(Register newReg) {
            destOperand.replaceRegister(newReg);
        }

        @Override
        public boolean usesVars() {
            return true;
        }
        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            if (structOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            return usesList;
        }

        @Override
        public void replaceUses(Register[] newUses) {
            if (newUses.length > 0)
                structOperand.replaceRegister(newUses[0]);
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
        public boolean usesVars() {
            return true;
        }
        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            if (structOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            if (sourceOperand instanceof Operand.RegisterOperand registerOperand)
                usesList.add(registerOperand.reg);
            return usesList;
        }
        @Override
        public void replaceUses(Register[] newUses) {
            int i = 0;
            if (structOperand instanceof Operand.RegisterOperand) {
                structOperand.replaceRegister(newUses[i++]);
            }
            if (sourceOperand instanceof Operand.RegisterOperand) {
                sourceOperand.replaceRegister(newUses[i]);
            }
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
        public Return(Operand from, Register reg) {
            super(from, new Operand.ReturnRegisterOperand(reg));
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
        public boolean definesVar() {
            return true;
        }

        @Override
        public Register def() {
            return result.reg;
        }

        @Override
        public void replaceDef(Register newReg) {
            result.replaceRegister(newReg);
        }

        @Override
        public boolean usesVars() {
            return operand instanceof Operand.RegisterOperand registerOperand;
        }

        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            if (operand instanceof Operand.RegisterOperand registerOperand) {
                usesList.add(registerOperand.reg);
            }
            return usesList;
        }

        @Override
        public void replaceUses(Register[] newUses) {
            if (newUses.length > 0)
                operand.replaceRegister(newUses[0]);
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
        public boolean definesVar() {
            return true;
        }

        @Override
        public Register def() {
            return result.reg;
        }

        @Override
        public void replaceDef(Register newReg) {
            result.replaceRegister(newReg);
        }

        @Override
        public boolean usesVars() {
            return left instanceof Operand.RegisterOperand ||
                    right instanceof Operand.RegisterOperand;
        }
        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            if (left instanceof Operand.RegisterOperand registerOperand) {
                usesList.add(registerOperand.reg);
            }
            if (right instanceof Operand.RegisterOperand registerOperand) {
                usesList.add(registerOperand.reg);
            }
            return usesList;
        }

        @Override
        public void replaceUses(Register[] newUses) {
            int i = 0;
            if (left instanceof Operand.RegisterOperand) {
                left.replaceRegister(newUses[i++]);
            }
            if (right instanceof Operand.RegisterOperand) {
                right.replaceRegister(newUses[i]);
            }
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
        public boolean usesVars() {
            return true;
        }
        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            usesList.add(array.reg);
            if (value instanceof Operand.RegisterOperand registerOperand) {
                usesList.add(registerOperand.reg);
            }
            return usesList;
        }
        @Override
        public void replaceUses(Register[] newUses) {
            array.replaceRegister(newUses[0]);
            if (value instanceof Operand.RegisterOperand)
                value.replaceRegister(newUses[1]);
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
        public boolean usesVars() {
            return condition instanceof Operand.RegisterOperand;
        }
        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            if (condition instanceof Operand.RegisterOperand registerOperand) {
                usesList.add(registerOperand.reg);
            }
            return usesList;
        }

        @Override
        public void replaceUses(Register[] newUses) {
            if (condition instanceof Operand.RegisterOperand) {
                condition.replaceRegister(newUses[0]);
            }
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
        public boolean definesVar() {
            return returnOperand != null;
        }

        @Override
        public Register def() {
            return returnOperand != null ? returnOperand.reg : null;
        }

        @Override
        public void replaceDef(Register newReg) {
            if (returnOperand != null)
                returnOperand.replaceRegister(newReg);
        }

        @Override
        public boolean usesVars() {
            return args != null && args.length > 0;
        }
        @Override
        public List<Register> uses() {
            List<Register> usesList = new ArrayList<>();
            for (Operand.RegisterOperand argOperand : args) {
                usesList.add(argOperand.reg);
            }
            return usesList;
        }

        @Override
        public void replaceUses(Register[] newUses) {
            if (args == null)
                return;
            for (int i = 0; i < args.length; i++) {
                args[i].replaceRegister(newUses[i]);
            }
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

    public static class Phi extends Instruction {
        public Operand.RegisterOperand dest;
        public final List<Operand.RegisterOperand> inputs = new ArrayList<>();
        public Phi(Register dest, List<Register> inputs) {
            this.dest = new Operand.RegisterOperand(dest);
            for (Register input : inputs) {
                this.inputs.add(new Operand.RegisterOperand(input));
            }
        }
        @Override
        public boolean definesVar() {
            return true;
        }
        @Override
        public Register def() {
            return dest.reg;
        }
        @Override
        public void replaceDef(Register newReg) {
            dest = new Operand.RegisterOperand(newReg);
        }
        public void replaceInput(int i, Register newReg) {
            inputs.set(i, new Operand.RegisterOperand(newReg));
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            sb.append(dest).append(" = phi(");
            for (int i = 0; i < inputs.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(inputs.get(i));
            }
            sb.append(")");
            return sb;
        }
    }

    public static class ArgInstruction extends Instruction {
        Operand.RegisterOperand arg;

        @Override
        public boolean definesVar() {
            return true;
        }
        @Override
        public Register def() {
            return arg.reg;
        }

        @Override
        public void replaceDef(Register newReg) {
            arg.replaceRegister(newReg);
        }

        public ArgInstruction(Operand.RegisterOperand arg) {
            this.arg = arg;
        }
        @Override
        public StringBuilder toStr(StringBuilder sb) {
            return sb.append("arg ").append(arg);
        }
    }

    public abstract StringBuilder toStr(StringBuilder sb);
}
