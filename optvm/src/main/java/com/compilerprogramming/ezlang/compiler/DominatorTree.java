package com.compilerprogramming.ezlang.compiler;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * The dominator tree construction algorithm is based on figure 9.24,
 * chapter 9, p 532, of Engineering a Compiler.
 * <p>
 * The algorithm is also described in the paper 'A Simple, Fast
 * Dominance Algorithm' by Keith D. Cooper, Timothy J. Harvey and
 * Ken Kennedy.
 */
public class DominatorTree {
    BasicBlock entry;
    // List of basic blocks reachable from _entry block, including the _entry
    List<BasicBlock> blocks;

    int preorder;
    int rpostorder;

    /**
     * Builds a Dominator Tree.
     *
     * @param entry The entry block
     */
    public DominatorTree(BasicBlock entry) {
        this.entry = entry;
        blocks = BBHelper.findAllBlocks(entry);
        calculateDominatorTree();
        populateTree();
        setDepth();
        calculateDominanceFrontiers();
        //calculateDominanceFrontiersMethod2();
    }

    private void calculateDominatorTree() {
        resetDomInfo();
        annotateBlocksWithRPO();
        sortBlocksByRPO();

        // Set IDom entry for root to itself (see note below)
        entry.idom = entry;
        boolean changed = true;
        while (changed) {
            changed = false;
            // for all nodes, b, in reverse postorder (except root)
            for (BasicBlock bb : blocks) {
                if (bb == entry) // skip root
                    continue;
                // NewIDom = first (processed) predecessor of b, pick one
                BasicBlock firstPred = findFirstPredecessorWithIdom(bb);
                assert firstPred != null;
                BasicBlock newIDom = firstPred;
                // for all other predecessors, p, of b
                for (BasicBlock predecessor : bb.predecessors) {
                    if (predecessor == firstPred) continue; // all other predecessors
                    if (predecessor.idom != null) {
                        // i.e. IDoms[p] calculated
                        newIDom = intersect(predecessor, newIDom);
                    }
                }
                if (bb.idom != newIDom) {
                    bb.idom = newIDom;
                    changed = true;
                }
            }
        }
        // There is a contradiction between what is described in the book
        // and the algo - as the book says the IDOM for root is undefined
        // But we set this to itself when calculating. So after calculations
        // are done, set this to null. This is technically more correct IMO.
        entry.idom = null;
    }

    private void resetDomInfo() {
        for (BasicBlock bb : blocks)
            bb.resetDomInfo();
    }

    /**
     * Assign rpo number to all the basic blocks.
     * The rpo number defines the Reverse Post Order traversal of blocks.
     * The Dominance calculator requires the rpo number.
     */
    private void annotateBlocksWithRPO() {
        preorder = 1;
        rpostorder = blocks.size();
        for (BasicBlock n : blocks) n.resetRPO();
        postOrderWalkSetRPO(entry);
    }

    // compute rpo using a depth first search
    private void postOrderWalkSetRPO(BasicBlock n) {
        n.pre = preorder++;
        for (BasicBlock s : n.successors) {
            if (s.pre == 0)
                postOrderWalkSetRPO(s);
        }
        n.rpo = rpostorder--;
    }

    /**
     * Reverse post order gives a topological sort order
     */
    private void sortBlocksByRPO() {
        blocks.sort(Comparator.comparingInt(n -> n.rpo));
    }

    /**
     * Finds nearest common ancestor
     * <p>
     * The algorithm starts at the two nodes whose sets are being intersected, and walks
     * upward from each toward the root. By comparing the nodes with their RPO numbers
     * the algorithm finds the common ancestor - the immediate dominator of i and j.
     */
    private BasicBlock intersect(BasicBlock i, BasicBlock j) {
        BasicBlock finger1 = i;
        BasicBlock finger2 = j;
        while (finger1 != finger2) {
            while (finger1.rpo > finger2.rpo) {
                finger1 = finger1.idom;
                assert finger1 != null;
            }
            while (finger2.rpo > finger1.rpo) {
                finger2 = finger2.idom;
                assert finger2 != null;
            }
        }
        return finger1;
    }

    /**
     * Look for the first predecessor whose immediate dominator has been calculated.
     * Because of the order in which this search occurs, we will always find at least 1
     * such predecessor.
     */
    private BasicBlock findFirstPredecessorWithIdom(BasicBlock n) {
        for (BasicBlock p : n.predecessors) {
            if (p.idom != null) return p;
        }
        return null;
    }

    /**
     * Setup the dominator tree.
     * Each block gets the list of blocks it strictly dominates.
     */
    private void populateTree() {
        for (BasicBlock block : blocks) {
            BasicBlock idom = block.idom;
            if (idom == null) // root
                continue;
            // add edge from idom to n
            idom.dominatedChildren.add(block);
        }
    }

    /**
     * Sets the dominator depth on each block
     */
    private void setDepth() {
        entry.domDepth = 1;
        setDepth_(entry);
    }

    /**
     * Sets the dominator depth on each block
     */
    private void setDepth_(BasicBlock block) {
        BasicBlock idom = block.idom;
        if (idom != null) {
            assert idom.domDepth > 0;
            block.domDepth = idom.domDepth + 1;
        }
        else {
            // root (entry) block's idom is null
            assert block.domDepth == 1;
        }
        for (BasicBlock child : block.dominatedChildren)
            setDepth_(child);
    }

    /**
     * Calculates dominance-frontiers for nodes
     */
    private void calculateDominanceFrontiers() {
        // Dominance-Frontier Algorithm - fig 5 in 'A Simple, Fast Dominance Algorithm'
        //for all nodes, b
        //  if the number of predecessors of b ≥ 2
        //      for all predecessors, p, of b
        //          runner ← p
        //          while runner != doms[b]
        //              add b to runner’s dominance frontier set
        //              runner = doms[runner]
        for (BasicBlock b: blocks)
            b.dominationFrontier.clear();   // empty set
        for (BasicBlock b : blocks) {
            if (b.predecessors.size() >= 2) {
                for (BasicBlock p : b.predecessors) {
                    BasicBlock runner = p;
                    // re runner != null: Dominance frontier calc fails in infinite loop
                    // scenario - need to check what the correct solution is
                    while (runner != b.idom && runner != null) {
                        runner.dominationFrontier.add(b);
                        runner = runner.idom;
                    }
                }
            }
        }
    }

    // We have an alternative approach to calculating DOM Frontiers to
    // allow us to validate above
    private void calculateDominanceFrontiersMethod2()
    {
        for (BasicBlock b: blocks)
            b.dominationFrontier.clear();   // empty set
        computeDF(entry);
    }

    // Implementation based on description in pg 440 of
    // Modern Compiler implementation in C
    // Appel
    private void computeDF(BasicBlock n) {
        var S = new HashSet<BasicBlock>();
        for (BasicBlock y: n.successors) {
            if (y.idom != n)
                S.add(y);
        }
        for (BasicBlock c: n.dominatedChildren) {
            computeDF(c);
            for (BasicBlock w: c.dominationFrontier) {
                // Note that the printed book has an error below
                // and errata gives the correct version
                if (!n.dominates(w) || n==w) {
                    S.add(w);
                }
            }
        }
        n.dominationFrontier.addAll(S);
    }

    public String generateDotOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph DomTree {\n");
        for (BasicBlock n : blocks) {
            sb.append(n.uniqueName()).append(" [label=\"").append(n.label()).append("\"];\n");
        }
        for (BasicBlock n : blocks) {
            BasicBlock idom = n.idom;
            if (idom == null) continue;
            sb.append(idom.uniqueName()).append("->").append(n.uniqueName()).append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public String listDomFrontiers() {
        StringBuilder sb = new StringBuilder();
        for (BasicBlock n : blocks) {
            n.listDomFrontiers(sb);
        }
        return sb.toString();
    }
}
