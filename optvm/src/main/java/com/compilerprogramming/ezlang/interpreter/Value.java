package com.compilerprogramming.ezlang.interpreter;

import com.compilerprogramming.ezlang.types.Type;

import java.util.ArrayList;

public class Value {
    static public class IntegerValue extends Value {
        public IntegerValue(long value) {
            this.value = value;
        }
        public final long value;
    }
    static public class ArrayValue extends Value {
        public final Type.TypeArray arrayType;
        public final ArrayList<Value> values;
        public ArrayValue(Type.TypeArray arrayType) {
            this.arrayType = arrayType;
            values = new ArrayList<>();
        }
    }
    static public class StructValue extends Value {
        public final Type.TypeStruct structType;
        public final Value[] fields;
        public StructValue(Type.TypeStruct structType) {
            this.structType = structType;
            this.fields = new Value[structType.numFields()];
        }
    }
}
