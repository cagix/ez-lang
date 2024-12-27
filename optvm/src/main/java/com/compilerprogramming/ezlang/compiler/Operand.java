package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

public class Operand {

    Type type;

    public void replaceRegister(Register register) {}

    public static class ConstantOperand extends Operand {
        public final long value;
        public ConstantOperand(long value, Type type) {
            this.value = value;
            this.type = type;
        }
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class RegisterOperand extends Operand {
        Register reg;
        public RegisterOperand(Register reg) {
            this.reg = reg;
            if (reg == null)
                throw new NullPointerException();
        }
        public int slot() { return reg.nonSSAId(); }

        @Override
        public void replaceRegister(Register register) {
            this.reg = register;
        }

        @Override
        public String toString() {
            return reg.name();
        }
    }

    public static class LocalRegisterOperand extends RegisterOperand {
        public LocalRegisterOperand(Register reg) {
            super(reg);
        }
    }

    public static class LocalFunctionOperand extends Operand {
        public final Type.TypeFunction functionType;
        public LocalFunctionOperand(Type.TypeFunction functionType) {
            this.functionType = functionType;
        }
        @Override
        public String toString() {
            return functionType.toString();
        }
    }

    /**
     * Represents the return register, which is the location where
     * the caller will expect to see any return value. The VM must map
     * this to appropriate location.
     */
    public static class ReturnRegisterOperand extends RegisterOperand {
        public ReturnRegisterOperand(Register reg) { super(reg); }
    }

    /**
     * Represents a temp register, maps to a location on the
     * virtual stack. Temps start at offset 0, but this is a relative
     * register number from start of temp area.
     */
    public static class TempRegisterOperand extends RegisterOperand {
        public TempRegisterOperand(Register reg) {
            super(reg);
        }
    }

    public static class IndexedOperand extends Operand {}

    public static class LoadIndexedOperand extends IndexedOperand {
        public final Operand arrayOperand;
        public final Operand indexOperand;
        public LoadIndexedOperand(Operand arrayOperand, Operand indexOperand) {
            this.arrayOperand = arrayOperand;
            this.indexOperand = indexOperand;
            assert !(indexOperand instanceof IndexedOperand) &&
                    !(arrayOperand instanceof IndexedOperand);
        }
        @Override
        public String toString() {
            return arrayOperand + "[" + indexOperand + "]";
        }
    }

    public static class LoadFieldOperand extends IndexedOperand {
        public final Operand structOperand;
        public final int fieldIndex;
        public final String fieldName;
        public LoadFieldOperand(Operand structOperand, String fieldName, int field) {
            this.structOperand = structOperand;
            this.fieldName = fieldName;
            this.fieldIndex = field;
            assert !(structOperand instanceof IndexedOperand);
        }

        @Override
        public String toString() {
            return structOperand + "." + fieldName;
        }
    }

    public static class NewTypeOperand extends Operand {
        public final Type type;
        public NewTypeOperand(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "New(" + type + ")";
        }
    }

}
