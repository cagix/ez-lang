package com.compilerprogramming.ezlang.compiler;

import java.util.*;

public class BasicBlock {
    public final int bid;
    public final boolean loopHead;
    public final List<BasicBlock> successors = new ArrayList<>(); // successors
    public final List<BasicBlock> predecessors = new ArrayList<>();
    public final List<Instruction> instructions = new ArrayList<>();
    /**
     * The preorder traversal number, also acts as a flag indicating whether the
     * BB is yet to be visited (_pre==0 means not yet visited).
     */
    int pre;
    /**
     * The depth of the BB in the dominator tree
     */
    int domDepth;
    /**
     * Reverse post order traversal number;
     * Sort node list in ascending order by this to traverse graph in reverse post order.
     * In RPO order if an edge exists from A to B we visit A followed by B, but cycles have to
     * be dealt with in another way.
     */
    int rpo;
    /**
     * Immediate dominator is the closest strict dominator.
     * @see DominatorTree
     */
    public BasicBlock idom;
    /**
     * Nodes for whom this node is the immediate dominator,
     * thus the dominator tree.
     */
    public List<BasicBlock> dominatedChildren = new ArrayList<>();
    /**
     * Dominance frontier
     */
    public Set<BasicBlock> dominationFrontier = new HashSet<>();

    /**
     * Nearest Loop to which this BB belongs
     */
    public LoopNest loop;

    // Liveness computation
    /**
     * VarKill contains all the variables that are defined
     * in the block.
     */
    LiveSet varKill;
    /**
     * UEVar contains upward-exposed variables in the block,
     * i.e. those variables that are used in the block prior to
     * any redefinition in the block.
     */
    LiveSet UEVar;
    /**
     * LiveOut is the union of variables that are live at the
     * head of some block that is a successor of this block.
     */
    LiveSet liveOut;

    /**
     * Inputs to successor block's phi function
     */
    LiveSet phiUses;
    /**
     * Phi definitions in this block
     */
    LiveSet phiDefs;
    /**
     * Live in set
     */
    LiveSet liveIn;
    // -----------------------

    public BasicBlock(int bid, boolean loopHead) {
        this.bid = bid;
        this.loopHead = loopHead;
    }
    public BasicBlock(int bid) {
        this(bid, false);
    }
    // For testing only
    public BasicBlock(int bid, BasicBlock... preds) {
        this.bid = bid;
        this.loopHead = false;
        for (BasicBlock bb : preds)
            bb.addSuccessor(this);
    }
    public void add(Instruction instruction) {
        instructions.add(instruction);
    }
    public void addSuccessor(BasicBlock successor) {
        successors.add(successor);
        successor.predecessors.add(this);
    }

    /**
     * Initially the phi has the form
     * v = phi(v,v,...)
     */
    public void insertPhiFor(Register var) {
        for (Instruction i: instructions) {
            if (i instanceof Instruction.Phi phi) {
                if (phi.value().nonSSAId() == var.nonSSAId())
                    // already added
                    return;
            }
            else break;
        }
        List<Register> inputs = new ArrayList<>();
        for (int i = 0; i < predecessors.size(); i++)
            inputs.add(var);
        Instruction.Phi phi = new Instruction.Phi(var, inputs);
        instructions.add(0, phi);
    }
    public List<Instruction.Phi> phis() {
        List<Instruction.Phi> list = new ArrayList<>();
        for (Instruction i: instructions) {
            if (i instanceof Instruction.Phi phi)
                list.add(phi);
            else break;
        }
        return list;
    }
    public static StringBuilder toStr(StringBuilder sb, BasicBlock bb, BitSet visited, boolean dumpLiveness)
    {
        if (visited.get(bb.bid))
            return sb;
        visited.set(bb.bid);
        sb.append("L").append(bb.bid).append(":\n");
        for (Instruction n: bb.instructions) {
            sb.append("    ");
            n.toStr(sb).append("\n");
        }
        if (dumpLiveness) {
            if (bb.phiDefs != null) sb.append("    #PHIDEFS = ").append(bb.phiDefs.toString()).append("\n");
            if (bb.phiUses != null) sb.append("    #PHIUSES = ").append(bb.phiUses.toString()).append("\n");
            if (bb.UEVar != null)   sb.append("    #UEVAR   = ").append(bb.UEVar.toString()).append("\n");
            if (bb.varKill != null) sb.append("    #VARKILL = ").append(bb.varKill.toString()).append("\n");
            if (bb.liveIn != null)  sb.append("    #LIVEIN  = ").append(bb.liveIn.toString()).append("\n");
            if (bb.liveOut != null) sb.append("    #LIVEOUT = ").append(bb.liveOut.toString()).append("\n");
        }
        for (BasicBlock succ: bb.successors) {
            toStr(sb, succ, visited, dumpLiveness);
        }
        return sb;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicBlock that = (BasicBlock) o;
        return bid == that.bid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid);
    }

    public String label() {
        return "BB(" + bid + ")";
    }

    public String uniqueName() {
        return "BB_" + bid;
    }

    //////////////// dominator calculations /////////////////////

    public void resetDomInfo() {
        domDepth = 0;
        idom = null;
        dominatedChildren.clear();
        dominationFrontier.clear();
    }

    public void resetRPO() {
        pre = 0;
        rpo = 0;
    }

    public boolean dominates(BasicBlock other) {
        if (this == other) return true;
        while (other.domDepth > domDepth) other = other.idom;
        return this == other;
    }

    /////////////////// End of dominator calculations //////////////////////////////////
}
