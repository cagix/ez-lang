package com.compilerprogramming.ezlang.compiler;

import java.util.List;

/**
 * Compute LiveOut for each Basic Block
 *
 * Original Implementation was based on description in 'Engineering a Compiler' 2nd ed.
 * pages 446-447.
 *
 * It turned out that this dataflow implementation cannot correctly handle
 * phis, because with phis, the inputs are live at the predecessor blocks.
 * We have to look at alternative approaches when input is SSA form.
 * Surprisingly even with this approach, the lost copy and swap problems
 * appeared to work correctly.
 *
 * The new approach is based on formula described in
 * Computing Liveness Sets for SSA-Form Programs
 * Florian Brandner, Benoit Boissinot, Alain Darte, Benoît Dupont de Dinechin, Fabrice Rastello
 *
 * The implementation is the unoptimized simple one.
 * However, we have a modification to ensure that if we see a block
 * which loops to itself and has Phi cycles, then the Phi is only added to
 * PhiDefs.
 */
public class Liveness {

    public Liveness(CompiledFunction function) {
        List<BasicBlock> blocks = BBHelper.findAllBlocks(function.entry);
        RegisterPool regPool = function.registerPool;
        initBlocks(regPool, blocks);
        init(blocks);
        computeLiveness(blocks);
        function.hasLiveness = true;
    }

    private void initBlocks(RegisterPool regPool, List<BasicBlock> blocks) {
        int numRegisters = regPool.numRegisters();
        for (BasicBlock block : blocks) {
            block.UEVar = new LiveSet(numRegisters);
            block.varKill = new LiveSet(numRegisters);
            block.liveOut = new LiveSet(numRegisters);
            block.liveIn = new LiveSet(numRegisters);
            block.phiUses = new LiveSet(numRegisters);
            block.phiDefs = new LiveSet(numRegisters);
        }
    }

    private void init(List<BasicBlock> blocks) {
        for (BasicBlock block : blocks) {
            // We st up phiDefs first because when we
            // look at phi uses we need to refer back here
            // see comments on phi cycles below
            for (Instruction instruction : block.instructions) {
                if (instruction instanceof Instruction.Phi phi) {
                    block.phiDefs.add(phi.value());
                }
                else break;
            }
            for (Instruction instruction : block.instructions) {
                for (Register use : instruction.uses()) {
                    if (!block.varKill.contains(use))
                        block.UEVar.add(use);
                }
                if (instruction.definesVar() && !(instruction instanceof Instruction.Phi)) {
                    Register def = instruction.def();
                    block.varKill.add(def);
                }
                if (instruction instanceof Instruction.Phi phi) {
                    for (int i = 0; i < block.predecessors.size(); i++) {
                        BasicBlock pred = block.predecessors.get(i);
                        Register use = phi.input(i);
                        // We can have a block referring it its own phis
                        // if there is loop back and there are cycles
                        // such as e.g. the swap copy problem
                        if (pred == block &&
                            block.phiDefs.contains(use))
                            continue;
                        pred.phiUses.add(use);
                    }
                }
            }
        }
    }

    private void computeLiveness(List<BasicBlock> blocks) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock block : blocks) {
                if (recomputeLiveOut(block))
                    changed = true;
            }
        }
    }

    // See 'Computing Liveness Sets for SSA-Form Programs'
    // LiveIn(B) = PhiDefs(B) U UpwardExposed(B) U (LiveOut(B) \ Defs(B))
    // LiveOut(B) = U all S  (LiveIn(S) \ PhiDefs(S)) U PhiUses(B)
    private boolean recomputeLiveOut(BasicBlock block) {
        LiveSet oldLiveOut = block.liveOut.dup();
        LiveSet t = block.liveOut.dup().subtract(block.varKill);
        block.liveIn.union(block.phiDefs).union(block.UEVar).union(t);
        block.liveOut.clear();
        for (BasicBlock s: block.successors) {
            t = s.liveIn.dup().subtract(s.phiDefs);
            block.liveOut.union(t);
        }
        block.liveOut.union(block.phiUses);
        return !oldLiveOut.equals(block.liveOut);
    }
}
