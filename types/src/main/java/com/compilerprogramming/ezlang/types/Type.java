package com.compilerprogramming.ezlang.types;

import com.compilerprogramming.ezlang.exceptions.CompilerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Currently, we support Int, Struct, and Array of Int/Struct.
 * Arrays and Structs are reference types.
 */
public abstract class Type {

    // Type classes
    static final byte TVOID = 0;
    static final byte TUNKNOWN = 1;
    static final byte TNULL = 2;
    static final byte TINT = 3;      // Int, Bool
    static final byte TNULLABLE = 4;   // Null, or not null ptr
    static final byte TFUNC = 5;     // Function types
    static final byte TSTRUCT = 6;
    static final byte TARRAY = 7;

    public final byte tclass;    // type class
    public final String name;      // type name, always unique

    protected Type(byte tclass, String name) {
        this.tclass = tclass;
        this.name = name;
    }

    public boolean isPrimitive() { return false; }
    public String describe() { return toString(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return tclass == type.tclass && Objects.equals(name, type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tclass, name);
    }

    @Override
    public String toString() {
        return name;
    }
    public String name() { return name; }

    public boolean isAssignable(Type other) {
        if (other instanceof TypeVoid || other instanceof TypeUnknown)
            return false;
        if (this == other || equals(other)) return true;
        if (this instanceof TypeNullable nullable) {
            if (other instanceof TypeNull)
                return true;
            return nullable.baseType.isAssignable(other);
        }
        else if (other instanceof TypeNullable nullable) {
            // At compile time we allow nullable value to be
            // assigned to base type, but null check must be inserted
            // Optimizer may remove null check
            return isAssignable(nullable.baseType);
        }
        return false;
    }

    /**
     * Represents no type - useful for defining functions
     * that do not return a value
     */
    public static class TypeVoid extends Type {
        public TypeVoid() {
            super(TVOID, "$Void");
        }
    }

    public static class TypeUnknown extends Type {
        public TypeUnknown() {
            super(TUNKNOWN, "$Unknown");
        }
    }

    public static class TypeNull extends Type {
        public TypeNull() { super(TNULL, "$Null"); }
    }

    public static class TypeInteger extends Type {

        public TypeInteger() {
            super (TINT, "Int");
        }
        @Override
        public boolean isPrimitive() {
            return true;
        }
    }

    public static class TypeStruct extends Type {
        ArrayList<String> fieldNames = new ArrayList<>();
        ArrayList<Type> fieldTypes = new ArrayList<>();
        public boolean pending = true;

        public TypeStruct(String name) {
            super(TSTRUCT, name);
        }
        public void addField(String name, Type type) {
            if (!pending)
                throw new CompilerException("Cannot add field to an already defined struct");
            if (fieldNames.contains(name))
                throw new CompilerException("Field " + name + " already exists in struct " + this.name);
            if (type == null)
                throw new CompilerException("Cannot a field with null type");
            fieldNames.add(name);
            fieldTypes.add(type);
        }
        public String describe() {
            StringBuilder sb = new StringBuilder();
            sb.append("struct ").append(name()).append("{");
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                Type fieldType = fieldTypes.get(i);
                sb.append(fieldName).append(": ").append(fieldType.name()).append(";");
            }
            sb.append("}");
            return sb.toString();
        }
        public Type getField(String name) {
            int index = fieldNames.indexOf(name);
            if (index < 0)
                return null;
            return fieldTypes.get(index);
        }
        public int getFieldIndex(String name) {
            return  fieldNames.indexOf(name);
        }
        public int numFields() { return fieldNames.size(); }
        public void complete() { pending = false; }
    }

    public static class TypeArray extends Type {
        Type elementType;

        public TypeArray(Type baseType) {
            super(TARRAY, "[" + baseType.name() + "]");
            this.elementType = baseType;
            if (baseType instanceof TypeArray)
                throw new CompilerException("Array of array type not supported");
        }
        public Type getElementType() {
            return elementType;
        }
    }

    // This is really a dedicated Union type for T|Null.
    public static class TypeNullable extends Type {
        public final Type baseType;
        public TypeNullable(Type baseType) {
            super(TNULLABLE, baseType.name()+"?");
            this.baseType = baseType;
        }
    }

    public static class TypeFunction extends Type {
        public final List<Symbol> args = new ArrayList<>();
        public Type returnType;
        public Object code;
        public TypeFunction(String name) {
            super(TFUNC, name);
        }
        public void addArg(Symbol arg) {
            args.add(arg);
        }
        public void setReturnType(Type returnType) {
            this.returnType = returnType;
        }
        public String describe() {
            StringBuilder sb = new StringBuilder();
            sb.append("func ").append(name()).append("(");
            boolean first = true;
            for (Symbol arg: args) {
                if (!first)
                    sb.append(",");
                first = false;
                sb.append(arg.name).append(": ").append(arg.type.name());
            }
            sb.append(")");
            if (!(returnType instanceof Type.TypeVoid)) {
                sb.append("->").append(returnType.name());
            }
            return sb.toString();
        }
    }
}
