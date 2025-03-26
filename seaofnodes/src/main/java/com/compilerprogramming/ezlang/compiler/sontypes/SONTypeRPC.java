package com.compilerprogramming.ezlang.compiler.sontypes;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *  Return Program Control or Return PC or RPC
 */
public class SONTypeRPC extends SONType {

    // A set of CallEndNode IDs (or StopNode); commonly just one.
    // Basically a sparse bit set
    final HashSet<Integer> _rpcs;

    // If true, invert the meaning of the bits
    final boolean _any;

    private SONTypeRPC(boolean any, HashSet<Integer> rpcs) {
        super(TRPC);
        _any = any;
        _rpcs = rpcs;
    }
    private static SONTypeRPC make(boolean any, HashSet<Integer> rpcs) {
        return new SONTypeRPC(any,rpcs).intern();
    }

    public static SONTypeRPC constant(int cend) {
        HashSet<Integer> rpcs = new HashSet<>();
        rpcs.add(cend);
        return make(false,rpcs);
    }

    public  static final SONTypeRPC BOT = make(true,new HashSet<>());
    private static final SONTypeRPC TEST2 = constant(2);
    private static final SONTypeRPC TEST3 = constant(2);

    public static void gather(ArrayList<SONType> ts) { ts.add(BOT); ts.add(TEST2); ts.add(TEST3); }

    @Override public String str() {
        if( _rpcs.isEmpty() )
            return _any ? "$[ALL]" : "$[]";
        if( _rpcs.size()==1 )
            for( Integer rpc : _rpcs )
                return _any ? "$[-"+rpc+"]" : "$["+rpc+"]";
        return "$["+(_any ? "-" : "")+_rpcs+"]";
    }

    @Override public boolean isConstant() {
        return !_any && _rpcs.size()==1;
    }

    @Override
    public SONTypeRPC xmeet(SONType other) {
        SONTypeRPC rpc = (SONTypeRPC)other;
        // If the two sets are equal, the _any must be unequal (invariant),
        // so they cancel and all bits are set.
        if( _rpcs.equals(rpc._rpcs) )
            return BOT;
        // Classic union of bit sets (which might be infinite).
        HashSet<Integer> lhs = _rpcs, rhs = rpc._rpcs;
        // Smaller on left
        if( lhs.size() > rhs.size() ) { lhs = rpc._rpcs; rhs = _rpcs; }

        HashSet<Integer> rpcs = new HashSet<>();
        boolean any = true;
        // If both sets are infinite, intersect.
        if( _any && rpc._any ) {
            for( Integer i : lhs )  if( rhs.contains(i) )  rpcs.add(i);

        } else if( !_any && !rpc._any ) {
            // if neither set is infinite, union.
            rpcs.addAll( lhs );
            rpcs.addAll( rhs );
            any = false;
        } else {
            // If one is infinite, subtract the other from it.
            HashSet<Integer> sub = _any ? rpc._rpcs : _rpcs;
            HashSet<Integer> inf = _any ? _rpcs : rpc._rpcs;
            for( Integer i : inf )
                if( inf.contains(i) && !sub.contains(i) )
                    rpcs.add(i);
        }
        return make(any,rpcs);
    }

    @Override
    public SONType dual() { return make(!_any,_rpcs); }

    @Override
    int hash() { return _rpcs.hashCode() ^ (_any ? -1 : 0) ; }
    @Override
    public boolean eq( SONType t ) {
        SONTypeRPC rpc = (SONTypeRPC)t; // Contract
        return _any==rpc._any && _rpcs.equals(rpc._rpcs);
    }

}
