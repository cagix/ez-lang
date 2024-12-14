package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Register;

import java.util.*;

/**
 * Transform a bytecode function to SSA form
 */
public class SSATransform {

    CompiledFunction function;
    DominatorTree domTree;
    Register[] globals;
    BBSet[] blockSets;
    List<BasicBlock> blocks;
    int[] counters;
    VersionStack[] stacks;

    public SSATransform(CompiledFunction bytecodeFunction) {
        this.function = bytecodeFunction;
        setupGlobals();
        computeDomTreeAndDominanceFrontiers();
        this.blocks = domTree.blocks;
        findGlobalVars();
        insertPhis();
        renameVars();
    }

    private void computeDomTreeAndDominanceFrontiers() {
        domTree = new DominatorTree(function.entry);
    }

    private void setupGlobals() {
        // fixme this should really just look at locals I think
        globals = new Register[function.nextReg];
        blockSets = new BBSet[function.nextReg];
    }

    /**
     * Compute set of registers that are live across multiple blocks
     * i.e. are not exclusively used in a single block.
     */
    private void findGlobalVars() {
        for (BasicBlock block : blocks) {
            var varKill = new HashSet<Integer>();
            for (Instruction instruction: block.instructions) {
                if (instruction.usesVars()) {
                    for (Register reg : instruction.uses()) {
                        if (!varKill.contains(reg.id)) {
                            globals[reg.id] = reg;
                        }
                    }
                }
                if (instruction.definesVar()) {
                    Register reg = instruction.def();
                    varKill.add(reg.id);
                    if (blockSets[reg.id] == null) {
                        blockSets[reg.id] = new BBSet();
                    }
                    blockSets[reg.id].add(block);
                }
            }
        }
    }

    void insertPhis() {
        for (int i = 0; i < globals.length; i++) {
            Register x = globals[i];
            if (x != null) {
                var visited = new BitSet();
                var worklist = new WorkList(blockSets[x.id].blocks);
                var b = worklist.pop();
                while (b != null) {
                    visited.set(b.bid);
                    for (BasicBlock d: b.dominationFrontier) {
                        // insert phi for x in d
                        d.insertPhiFor(x);
                        if (!visited.get(d.bid))
                            worklist.push(d);
                    }
                    b = worklist.pop();
                }
            }
        }
    }

    void renameVars() {
        initVersionCounters();
        search(function.entry);
    }

    /**
     * Creates and pushes new name
     */
    Register makeVersion(Register reg) {
        int version = counters[reg.id];
        if (version != reg.ssaVersion)
            reg = reg.cloneWithVersion(version);
        stacks[reg.id].push(reg);
        counters[reg.id] = counters[reg.id] + 1;
        return reg;
    }

    /**
     * Recursively walk the Dominator Tree, renaming variables.
     * Implementation is based on the algorithm in the Preston Briggs
     * paper Practical Improvements to the Construction and Destruction of
     * Static Single Assignment Form
     */
    void search(BasicBlock block) {
        // Replace v = phi(...) with v_i = phi(...)
        for (Instruction.Phi phi: block.phis()) {
            Register ssaReg = makeVersion(phi.def());
            phi.replaceDef(ssaReg);
        }
        // for each instruction v = x op y
        // first replace x,y
        // then replace v
        for (Instruction instruction: block.instructions) {
            if (instruction instanceof Instruction.Phi)
                continue;
            // first replace x,y
            if (instruction.usesVars()) {
                var uses = instruction.uses();
                Register[] newUses = new Register[uses.size()];
                for (int i = 0; i < newUses.length; i++) {
                    Register oldReg = uses.get(i);
                    newUses[i] = stacks[oldReg.id].top();
                    instruction.replaceUses(newUses);
                }
            }
            // then replace v
            if (instruction.definesVar()) {
                Register ssaReg = makeVersion(instruction.def());
                instruction.replaceDef(ssaReg);
            }
        }
        // Update phis in successor blocks
        for (BasicBlock s: block.successors) {
            int j = whichPred(s,block);
            for (Instruction.Phi phi: s.phis()) {
                Register oldReg = phi.inputs.get(j).reg;
                phi.replaceInput(j, stacks[oldReg.id].top());
            }
        }
        // Recurse down the dominator tree
        for (BasicBlock c: block.dominatedChildren) {
            search(c);
        }
        // Pop stacks for defs
        for (Instruction i: block.instructions) {
            if (i.definesVar()) {
                var reg = i.def();
                stacks[reg.id].pop();
            }
        }
    }

    private int whichPred(BasicBlock s, BasicBlock block) {
        int i = 0;
        for (BasicBlock p: s.predecessors) {
            if (p == block)
                return i;
            i++;
        }
        throw new IllegalStateException();
    }

    private void initVersionCounters() {
        counters = new int[globals.length];
        stacks = new VersionStack[globals.length];
        for (int i = 0; i < globals.length; i++) {
            counters[i] = 0;
            stacks[i] = new VersionStack();
        }
    }

    static class BBSet {
        Set<BasicBlock> blocks = new HashSet<>();
        void add(BasicBlock block) { blocks.add(block); }
    }

    static class VersionStack {
        List<Register> stack = new ArrayList<>();
        void push(Register r) { stack.add(r); }
        Register top() { return stack.getLast(); }
        void pop() { stack.removeLast(); }
    }

    /**
     * Simple worklist
     */
    public static class WorkList {

        private ArrayList<BasicBlock> blocks;
        private final BitSet members;

        WorkList() {
            blocks = new ArrayList<>();
            members = new BitSet();
        }
        WorkList(Collection<BasicBlock> blocks) {
            this();
            addAll(blocks);
        }
        public BasicBlock push( BasicBlock x ) {
            if( x==null ) return null;
            int idx = x.bid;
            if( !members.get(idx) ) {
                members.set(idx);
                blocks.add(x);
            }
            return x;
        }
        public void addAll( Collection<BasicBlock> ary ) {
            for( BasicBlock n : ary )
                push(n);
        }
        BasicBlock pop() {
            if ( blocks.isEmpty() )
                return null;
            var x = blocks.removeFirst();
            members.clear(x.bid);
            return x;
        }
    }
}
