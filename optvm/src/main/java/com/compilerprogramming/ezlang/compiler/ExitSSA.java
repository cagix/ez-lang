package com.compilerprogramming.ezlang.compiler;

import java.util.*;

/**
 * Converts from SSA form to non-SSA form.
 * Implementation is based on description in
 * 'Practical Improvements to the Construction and Destruction
 * of Static Single Assignment Form' by Preston Briggs.
 */
public class ExitSSA {

    CompiledFunction function;
    NameStack[] stacks;
    DominatorTree tree;

    public ExitSSA(CompiledFunction function) {
        this.function = function;
        if (!function.isSSA) throw new IllegalStateException();
        if (!function.hasLiveness) {
            new Liveness().computeLiveness(function);
        }
        tree = new DominatorTree(function.entry);
        initStack();
        insertCopies(function.entry);
        removePhis();
    }

    private void removePhis() {
        for (BasicBlock block : tree.blocks) {
            block.instructions.removeIf(instruction -> instruction instanceof Instruction.Phi);
        }
    }

    /* Algorithm for iterating through blocks to perform phi replacement */
    private void insertCopies(BasicBlock block) {
        List<Register> pushed = new ArrayList<>();
        for (Instruction i: block.instructions) {
            // replace all uses u with stacks[i]
            if (i.usesVars()) {
                replaceUses(i);
            }
        }
        scheduleCopies(block, pushed);
        for (BasicBlock c: block.dominatedChildren) {
            insertCopies(c);
        }
        for (Register name: pushed) {
            stacks[name.id].pop();
        }
    }

    /**
     * replace all uses u with stacks[i]
     */
    private void replaceUses(Instruction i) {
        var oldUses = i.uses();
        Register[] newUses = new Register[oldUses.size()];
        for (int u = 0; u < oldUses.size(); u++) {
            Register use = oldUses.get(u);
            if (!stacks[use.id].isEmpty())
                newUses[u] = stacks[use.id].top();
            else
                newUses[u] = use;
        }
        i.replaceUses(newUses);
    }

    static class CopyItem {
        Register src;
        Register dest;
        boolean removed;

        public CopyItem(Register src, Register dest) {
            this.src = src;
            this.dest = dest;
            this.removed = false;
        }
    }

    private void scheduleCopies(BasicBlock block, List<Register> pushed) {
        /* Pass 1 - Initialize data structures */
        /* In this pass we count the number of times a name is used by other phi-nodes */
        List<CopyItem> copySet = new ArrayList<>();
        Map<Integer, Register> map = new HashMap<>();
        BitSet usedByAnother = new BitSet(function.registerPool.numRegisters()*2);
        for (BasicBlock s: block.successors) {
            int j = SSATransform.whichPred(s, block);
            for (Instruction.Phi phi: s.phis()) {
                Register dst = phi.def();
                Register src = phi.inputs.get(j).reg;   // jth operand of phi node
                copySet.add(new CopyItem(src, dst));
                map.put(src.id, src);
                map.put(dst.id, dst);
                usedByAnother.set(src.id);
            }
        }

        /* Pass 2: setup up the worklist of initial copies */
        /* In this pass we build a worklist of names that are not used in other phi nodes */
        List<CopyItem> workList = new ArrayList<>();
        for (CopyItem copyItem: copySet) {
            if (!usedByAnother.get(copyItem.dest.id)) {
                copyItem.removed = true;
                workList.add(copyItem);
            }
        }
        copySet.removeIf(copyItem -> copyItem.removed);

        /* Pass 3: iterate over the worklist, inserting copies */
        /* Copy operations whose destinations are not used by other copy operations can be scheduled immediately */
        /* Each time we insert a copy operation we add the source of that op to the worklist */
        while (!workList.isEmpty() || !copySet.isEmpty()) {
            while (!workList.isEmpty()) {
                CopyItem copyItem = workList.remove(0);
                Register src = copyItem.src;
                Register dest = copyItem.dest;
                if (block.liveOut.get(dest.id)) {
                    /* Insert a copy from dest to a new temp t at phi node defining dest */
                    Register t = insertCopy(block, dest);
                    stacks[dest.id].push(t);
                    pushed.add(t);
                }
                /* Insert a copy operation from map[src] to dst at end of BB */
                appendCopy(block, map.get(src.id), dest);
                map.put(src.id, dest);
                /* If src is the name of a dest in copySet add item to worklist */
                CopyItem item = isDest(copySet, src);
                if (item != null) {
                    workList.add(item);
                }
            }
            if (!copySet.isEmpty()) {
                CopyItem copyItem = copySet.remove(0);
                /* Insert a copy from dst to new temp at the end of Block */
                Register t = appendCopy(block, copyItem.dest);
                map.put(copyItem.dest.id, t);
                workList.add(copyItem);
            }
        }
    }

    private void insertAtEnd(BasicBlock bb, Instruction i) {
        assert bb.instructions.size() > 0;
        int pos = bb.instructions.size()-1;
        bb.instructions.add(pos, i);
    }

    private void insertAfterPhi(BasicBlock bb, Register phiDef, Instruction newInst) {
        assert bb.instructions.size() > 0;
        int pos = 0;
        while (pos < bb.instructions.size()) {
            Instruction i = bb.instructions.get(pos);
            if (i instanceof Instruction.Phi phi &&
                    phi.def().id == phiDef.id) {
                pos += 1;
                break;
            }
        }
        if (pos == bb.instructions.size()) {
            throw new IllegalStateException();
        }
        bb.instructions.add(pos, newInst);
    }


    /* Insert a copy from dest to new temp at end of BB, and return temp */
    private Register appendCopy(BasicBlock block, Register dest) {
        var temp = function.registerPool.newReg(dest.name(), dest.type);
        var inst = new Instruction.Move(new Operand.RegisterOperand(dest), new Operand.RegisterOperand(temp));
        insertAtEnd(block, inst);
        return temp;
    }

    /* If src is the name of a dest in copySet return the item */
    private CopyItem isDest(List<CopyItem> copySet, Register src) {
        for (CopyItem copyItem: copySet) {
            if (copyItem.dest.id == src.id)
                return copyItem;
        }
        return null;
    }

    /* Insert a copy from src to dst at end of BB */
    private void appendCopy(BasicBlock block, Register src, Register dest) {
        var inst = new Instruction.Move(new Operand.RegisterOperand(src), new Operand.RegisterOperand(dest));
        insertAtEnd(block, inst);
    }

    /* Insert a copy dest to a new temp at phi node defining dest, return temp */
    private Register insertCopy(BasicBlock block, Register dst) {
        var temp = function.registerPool.newReg(dst.name(), dst.type);
        var inst = new Instruction.Move(new Operand.RegisterOperand(dst), new Operand.RegisterOperand(temp));
        insertAfterPhi(block, dst, inst);
        return temp;
    }

    private void initStack() {
        stacks = new NameStack[function.registerPool.numRegisters()];
        for (int i = 0; i < stacks.length; i++)
            stacks[i] = new NameStack();
    }

    static class NameStack {
        List<Register> stack = new ArrayList<>();
        void push(Register r) { stack.add(r); }
        Register top() { return stack.getLast(); }
        void pop() { stack.removeLast(); }
        boolean isEmpty() { return stack.isEmpty(); }
    }
}
