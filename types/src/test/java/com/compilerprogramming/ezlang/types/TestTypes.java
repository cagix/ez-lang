package com.compilerprogramming.ezlang.types;

public class TestTypes {

    EZType buildStruct1(TypeDictionary typeDictionary) {
        EZType.EZTypeStruct s = new EZType.EZTypeStruct("S1");
        s.addField("a", typeDictionary.INT);
        s.addField("b", typeDictionary.INT);
        s.complete();
        return typeDictionary.intern(s);
    }

    EZType buildStruct2(TypeDictionary typeDictionary) {
        EZType.EZTypeStruct s = new EZType.EZTypeStruct("S2");
        s.addField("a", typeDictionary.INT);
        s.addField("b", typeDictionary.INT);
        s.complete();
        return typeDictionary.intern(s);
    }

}
