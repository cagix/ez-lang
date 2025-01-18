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
        function.livenessAnalysis();
        System.out.println(function.toStr(new StringBuilder(), true));
        tree = new DominatorTree(function.entry);
        initStack();
        insertCopies(function.entry);
        removePhis();
        function.isSSA = false;
    }

    private void removePhis() {
        for (BasicBlock block : tree.blocks) {
            block.instructions.removeIf(instruction -> instruction instanceof Instruction.Phi);
        }
    }

    /* Algorithm for iterating through blocks to perform phi replacement */
    private void insertCopies(BasicBlock block) {
        List<Integer> pushed = new ArrayList<>();
        for (Instruction i: block.instructions) {
            // replace all uses u with stacks[i]
            replaceUses(i);
        }
        scheduleCopies(block, pushed);
        for (BasicBlock c: block.dominatedChildren) {
            insertCopies(c);
        }
        for (Integer name: pushed) {
            stacks[name].pop();
        }
    }

    /**
     * replace all uses u with stacks[i]
     */
    private void replaceUses(Instruction i) {
        if (i instanceof Instruction.Phi)
            // FIXME check this can never be valid
            // tests 8/9 in TestInterpreter invoke on Phi but
            // replacements are same as existing inputs
            return;
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
        final Register src;
        final Register dest;
        final BasicBlock destBlock;
        boolean removed;

        public CopyItem(Register src, Register dest, BasicBlock destBlock) {
            this.src = src;
            this.dest = dest;
            this.destBlock = destBlock;
            this.removed = false;
        }
    }

    private void scheduleCopies(BasicBlock block, List<Integer> pushed) {
        /* Pass 1 - Initialize data structures */
        /* In this pass we count the number of times a name is used by other phi-nodes */
        List<CopyItem> copySet = new ArrayList<>();
        Map<Integer, Register> map = new HashMap<>();
        BitSet usedByAnother = new BitSet(function.registerPool.numRegisters()*2);
        for (BasicBlock s: block.successors) {
            int j = s.whichPred(block);
            for (Instruction.Phi phi: s.phis()) {
                Register dst = phi.value();
                Register src = phi.inputAsRegister(j);   // jth operand of phi node
                copySet.add(new CopyItem(src, dst, s));
                map.put(src.id, src);
                map.put(dst.id, dst);
                usedByAnother.set(src.id);
            }
        }

        /* Pass 2: setup up the worklist of initial copies */
        /* In this pass we build a worklist of names that are not used in other phi nodes */
        List<CopyItem> workList = new ArrayList<>();
        for (CopyItem copyItem: copySet) {
            if (usedByAnother.get(copyItem.dest.id) != true) {
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
                final CopyItem copyItem = workList.remove(0);
                final Register src = copyItem.src;
                final Register dest = copyItem.dest;
                final BasicBlock destBlock = copyItem.destBlock;
                /* Engineering a Compiler: We can avoid the lost copy
                   problem by checking the liveness of the target name
                   for each copy that we try to insert. When we discover
                   a copy target that is live, we must preserve the live
                   value in a temporary name and rewrite subsequent uses to
                   refer to the temporary name.
                 */
                if (block.liveOut.get(dest.id)) {
                    /* Insert a copy from dest to a new temp t at phi node defining dest */
                    final Register t = addMoveToTempAfterPhi(destBlock, dest);
                    stacks[dest.id].push(t);
                    pushed.add(dest.id);
                }
                /* Insert a copy operation from map[src] to dest at end of BB */
                addMoveAtBBEnd(block, map.get(src.id), dest);
                map.put(src.id, dest);
                /* If src is the name of a dest in copySet add item to worklist */
                /* see comment on phi cycles below. */
                CopyItem item = isCycle(copySet, src);
                if (item != null) {
                    workList.add(item);
                }
            }
            /* Engineering a Compiler: To solve the swap problem
               we can detect cases where phi functions reference the
               targets of other phi functions in the same block. For each
               cycle of references, it must insert a copy to a temporary
               that breaks the cycle. Then we can schedule the copies to
               respect the dependencies implied by the phi functions.
             */
            if (!copySet.isEmpty()) {
                CopyItem copyItem = copySet.remove(0);
                /* Insert a copy from dst to new temp at the end of Block */
                Register t = addMoveToTempAtBBEnd(block, copyItem.dest);
                map.put(copyItem.dest.id, t);
                workList.add(copyItem);
            }
        }
    }

    private void insertAtEnd(BasicBlock bb, Instruction i) {
        assert bb.instructions.size() > 0;
        // Last instruction is a branch - so new instruction will
        // go before that
        int pos = bb.instructions.size()-1;
        bb.instructions.add(pos, i);
    }

    private void insertAfterPhi(BasicBlock bb, Register phiDef, Instruction newInst) {
        assert bb.instructions.size() > 0;
        int insertionPos = -1;
        for (int pos = 0; pos < bb.instructions.size(); pos++) {
            Instruction i = bb.instructions.get(pos);
            if (i instanceof Instruction.Phi phi) {
                if (phi.value().id == phiDef.id) {
                    insertionPos = pos+1;   // After phi
                    break;
                }
            }
        }
        if (insertionPos < 0) {
            throw new IllegalStateException();
        }
        bb.instructions.add(insertionPos, newInst);
    }

    /* Insert a copy from dest to new temp at end of BB, and return temp */
    private Register addMoveToTempAtBBEnd(BasicBlock block, Register dest) {
        var temp = function.registerPool.newTempReg(dest.name(), dest.type);
        var inst = new Instruction.Move(new Operand.RegisterOperand(dest), new Operand.RegisterOperand(temp));
        insertAtEnd(block, inst);
        return temp;
    }

    /* If src is the name of a dest in copySet remove the item */
    private CopyItem isCycle(List<CopyItem> copySet, Register src) {
        for (int i = 0; i < copySet.size(); i++) {
            CopyItem copyItem = copySet.get(i);
            if (copyItem.dest.id == src.id) {
                copySet.remove(i);
                return copyItem;
            }
        }
        return null;
    }

    /* Insert a copy from src to dst at end of BB */
    private void addMoveAtBBEnd(BasicBlock block, Register src, Register dest) {
        var inst = new Instruction.Move(new Operand.RegisterOperand(src), new Operand.RegisterOperand(dest));
        insertAtEnd(block, inst);
    }

    /* Insert a copy dest to a new temp at phi node defining dest, return temp */
    private Register addMoveToTempAfterPhi(BasicBlock block, Register dest) {
        var temp = function.registerPool.newTempReg(dest.name(), dest.type);
        var inst = new Instruction.Move(new Operand.RegisterOperand(dest), new Operand.RegisterOperand(temp));
        insertAfterPhi(block, dest, inst);
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
