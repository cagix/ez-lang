package com.compilerprogramming.ezlang.compiler.sontypes;

import com.compilerprogramming.ezlang.compiler.SB;
import com.compilerprogramming.ezlang.compiler.Utils;
import java.util.ArrayList;

/**
 * Represents a struct type.
 */
public class SONTypeStruct extends SONType {

    // A Struct has a name and a set of fields; the fields themselves have
    // names, types and aliases.  The name has no semantic meaning, but is
    // useful for debugging.  Briefly during parsing a Struct can be a
    // forward-ref; in this case the _fields array is null.

    // Its illegal to attempt to load a field from a forward-ref struct.

    // Example: "int rez = new S.x; struct S { int x; } return rez;" // Error, S not defined
    // Rewrite: "struct S { int x; } int rez = new S.x; return rez;" // Ok, no forward ref
    //
    // During the normal optimization run, struct types "bottom out" at further
    // struct references, so we don't have to handle e.g. cyclic types.  The
    // "bottom out" is again the forward-ref struct.
    public final String _name;
    public Field[] _fields;

    public SONTypeStruct(String name, Field[] fields) {
        super(TSTRUCT);
        _name = name;
        _fields = fields;
    }

    // All fields directly listed
    public static SONTypeStruct make(String name, Field... fields) { return new SONTypeStruct(name, fields).intern(); }
    public static final SONTypeStruct TOP = make("$TOP",new Field[0]);
    public static final SONTypeStruct BOT = make("$BOT",new Field[0]);
    //public static final SONTypeStruct TEST = make("test",new Field[]{Field.TEST});
    // Forward-ref version
    public static SONTypeStruct makeFRef(String name) { return make(name, (Field[])null); }
    // Make a read-only version
    @Override public SONTypeStruct makeRO() {
        if( isFinal() ) return this;
        Field[] flds = new Field[_fields.length];
        for( int i=0; i<flds.length; i++ )
            flds[i] = _fields[i].makeRO();
        return make(_name,flds);
    }

    // Array
    public static SONTypeStruct makeAry(SONTypeInteger len, int lenAlias, SONType body, int bodyAlias) {
        assert body instanceof SONTypeInteger || body instanceof SONTypeFloat || (body instanceof SONTypeNil tn && tn.nullable());
        assert len.isa(SONTypeInteger.U32);
        return make("[" + body.str() + "]",
                    Field.make("#" ,len , lenAlias,true ),
                    Field.make("[]",body,bodyAlias,false));
    }
    // Array
    public static SONTypeStruct makeArray(SONTypeInteger len, int lenAlias, SONType body, int bodyAlias) {
        assert body instanceof SONTypeInteger || body instanceof SONTypeFloat || (body instanceof SONTypeNil tn && tn.nullable());
        assert len.isa(SONTypeInteger.U32);
        return new SONTypeStruct("[" + body.str() + "]",
                new Field[] {
                new Field("#" ,len , lenAlias,true ),
                new Field("[]",body,bodyAlias,false) });
    }

    // A pair of self-cyclic types
//    private static final SONTypeStruct S1F = makeFRef("S1");
//    private static final SONTypeStruct S2F = makeFRef("S2");
//    public  static final SONTypeStruct S1  = make("S1", Field.make("a", SONTypeInteger.BOT, -1, false), Field.make("s2", SONTypeMemPtr.make((byte)2,S2F),-2, false) );
//    private static final SONTypeStruct S2  = make("S2", Field.make("b", SONTypeFloat.F64, -3, false), Field.make("s1", SONTypeMemPtr.make((byte)2,S1F),-4, false) );
//
//    private static final SONTypeStruct ARY = makeAry(SONTypeInteger.U32,-1, SONTypeInteger.BOT,-2);

    public static void gather(ArrayList<SONType> ts) { /* ts.add(TEST); ts.add(BOT); ts.add(S1); ts.add(S2); ts.add(ARY); */ }

    // Find field index by name
    public int find(String fname) {
        for( int i=0; i<_fields.length; i++ )
            if( _fields[i]._fname.equals(fname) )
                return i;
        return -1;
    }
    public Field field(String fname) {
        int idx = find(fname);
        return idx== -1 ? null : _fields[idx];
    }

    // Find field index by alias
    public int findAlias( int alias ) {
        for( int i=0; i<_fields.length; i++ )
            if( _fields[i]._alias==alias )
                return i;
        return -1;
    }


    @Override
    SONType xmeet(SONType t) {
        SONTypeStruct that = (SONTypeStruct) t;
        if( this==TOP ) return that;
        if( that==TOP ) return this;
        if( this==BOT ) return BOT;
        if( that==BOT ) return BOT;

        // Within the same compilation unit, struct names are unique.  If the
        // names differ, its different structs.  Across many compilation units,
        // structs with the same name but different field layouts can be
        // interned... which begs the question:
        // "What is the meet of structs from two different compilation units?"
        // And the answer is: "don't ask".
        if( !_name.equals(that._name) )
            return BOT;         // It's a struct; that's about all we know
        if( this._fields==null ) return that;
        if( that._fields==null ) return this;
        if( _fields.length != that._fields.length ) return BOT;
        // Just do field meets
        Field[] flds = new Field[_fields.length];
        for( int i=0; i<_fields.length; i++ ) {
            Field f0 = _fields[i], f1 = that._fields[i];
            if( !f0._fname.equals(f1._fname) || f0._alias != f1._alias )
                return BOT;
            flds[i] = (Field)f0.meet(f1);
        }
        return make(_name,flds);
    }

    @Override
    public SONTypeStruct dual() {
        if( this==TOP ) return BOT;
        if( this==BOT ) return TOP;
        if( _fields == null ) return this;
        Field[] flds = new Field[_fields.length];
        for( int i=0; i<_fields.length; i++ )
            flds[i] = _fields[i].dual();
        return make(_name,flds);
    }

    // Keeps the same struct, but lower-bounds all fields.
    @Override public SONTypeStruct glb() {
        if( _glb() ) return this;
        // Need to glb each field
        Field[] flds = new Field[_fields.length];
        for( int i=0; i<_fields.length; i++ )
            flds[i] = _fields[i].glb();
        return make(_name,flds);
    }
    private boolean _glb() {
        if( _fields!=null )
          for( Field f : _fields )
              if( f.glb() != f )
                 return false;
        return true;
    }

    // Is forward-reference
    @Override public boolean isFRef() { return _fields==null; }
    // All fields are final
    @Override public boolean isFinal() {
        if( _fields==null ) return true;
        for( Field fld : _fields )
            if( !fld.isFinal() )
                return false;
        return true;
    }

    @Override
    boolean eq(SONType t) {
        SONTypeStruct ts = (SONTypeStruct)t; // Invariant
        if( !_name.equals(ts._name) )
            return false;
//        if( _fields == ts._fields ) return true;
//        if( _fields==null || ts._fields==null ) return false;
//        if( _fields.length!=ts._fields.length )
//            return false;
//        for( int i = 0; i < _fields.length; i++ )
//            if( _fields[i] != ts._fields[i] )
//                return false;
        return true;
    }

    @Override
    int hash() {
        long hash = _name.hashCode();
//        if( _fields != null )
//            for( Field f : _fields )
//                hash = Utils.rot(hash,13) ^ f.hashCode();
//        return Utils.fold(hash);
        return (int) hash;
    }

    @Override
    public SB print(SB sb) {
        if( _fields == null ) return sb.p(_name); // Forward reference struct, just print the name
        if( isAry() ) {
            if( !isFinal() ) sb.p("!");
            return sb.p(_name); // Skip printing generic array fields
        }
        sb.p(_name);
        sb.p(" {");
        for( Field f : _fields )
            f._type.print(sb).p(f._final ? " " : " !").p(f._fname).p("; ");
        return sb.p("}");
    }
    @Override public SB gprint( SB sb ) { return sb.p(_name); }

    @Override public String str() { return _name; }


    public boolean isAry() { return _fields.length==2 && _fields[1]._fname.equals("[]"); }

    public int aryBase() {
        assert isAry();
        if( _offs==null ) _offs = offsets();
        return _offs[1];
    }
    public int aryScale() {
        assert isAry();
        return _fields[1]._type.log_size();
    }


    // Field offsets as packed byte offsets
    private int[] _offs;  // Lazily computed and not part of the type semantics
    public int offset(int idx) {
        if( _offs==null ) _offs = offsets();
        return _offs[idx];
    }
    private int[] offsets() {    // Field byte offsets
        // Compute a layout for a collection of fields
        assert _fields != null; // No forward refs

        // Array layout is different: len,[pad],body...
        if( isAry() )
            return _offs = new int[]{ 0, _fields[1]._type.log_size() < 3 ? 4 : 8 };

        // Compute a layout
        int[] cnts = new int[4]; // Count of fields at log field size
        for( Field f : _fields )
            cnts[f._type.log_size()]++; // Log size is 0(byte), 1(i16/u16), 2(i32/f32), 3(i64/dbl)
        int off = 0, idx = 0; // Base common struct fields go here, e.g. Mark/Klass
        // Compute offsets to the start of each power-of-2 aligned fields.
        int[] offs = new int[4];
        for( int i=3; i>=0; i-- ) {
            offs[i] = off;
            off += cnts[i]<<i;
        }
        // Assign offsets to all fields.
        // Really a hidden radix sort.
        _offs = new int[_fields.length+1];
        for( Field f : _fields ) {
            int log = f._type.log_size();
            _offs[idx++] = offs[log]; // Field offset
            offs[log] += 1<<log;      // Next field offset at same alignment
            cnts[log]--;              // Count down, should be all zero at end
        }
        _offs[_fields.length] = (off+7)& ~7; // Round out to max alignment
        return _offs;
    }

}
