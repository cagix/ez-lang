package com.compilerprogramming.ezlang.compiler;

import java.util.List;

/**
 * Compute LiveOut for each Basic Block
 *
 * Original Implementation was based on description in 'Engineering a Compiler' 2nd ed.
 * pages 446-447.
 *
 * It turned out that this dataflow implementation cannot correctly handle
 * phis. Therefore, we have to look at alternative approaches when input is SSA form.
 * Surprisingly even with the flawed approach, the lost copy and swap problems
 * appeared to work correctly.
 *
 * Phis need special considerations:
 *
 * Phi inputs are live out at the predecessor blocks, but not live in for phi block.
 * Phi def is live in at phi block but not in live out at predecessor blocks.
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
            // Any vars created by phi instructions are added to phiDefs
            // for this block; these vars will be live on entry to block
            // but not live out from predecessor blocks.
            //
            // We set up phiDefs first because when we
            // look at phi uses we need to refer back here
            // see comments on phi cycles below
            for (Instruction instruction : block.instructions) {
                if (instruction instanceof Instruction.Phi phi) {
                    block.phiDefs.add(phi.value());
                }
                // There is a scenario where other instructions can appear
                // between phi instructions - this happens during the SSA deconstruction
                // using Brigg's method. But we don't calculate liveness
                // in the middle of that process, so assuming all phis are together
                // at the top of the block is okay
                else break;
            }
            for (Instruction instruction : block.instructions) {
                if (instruction instanceof Instruction.Phi phi) {
                    // Any uses in a Phi are added to the phiUses of predecessor
                    // block. These uses will be in live out of predecessor block but
                    // not live in for current block.
                    for (int i = 0; i < block.predecessors.size(); i++) {
                        BasicBlock pred = block.predecessors.get(i);
                        if (!phi.isRegisterInput(i))
                            continue;
                        Register use = phi.inputAsRegister(i);
                        // We can have a block referring it its own phis
                        // if there is loop back and there are cycles
                        // such as e.g. the swap copy problem
                        if (pred == block &&
                                block.phiDefs.contains(use))
                            continue;
                        pred.phiUses.add(use);
                    }
                }
                else {
                    // Non phi instructions follow regular
                    // logic. Any var that is used before being defined
                    // is added to upward expose set.
                    for (Register use : instruction.uses()) {
                        if (!block.varKill.contains(use))
                            block.UEVar.add(use);
                    }
                    if (instruction.definesVar()) {
                        Register def = instruction.def();
                        block.varKill.add(def);
                    }
                }
            }
        }
    }

    private void computeLiveness(List<BasicBlock> blocks) {
        boolean changed = true;
        while (changed) {
            changed = false;
            // TODO we should process in RPO order
            for (BasicBlock block : blocks) {
                if (recomputeLiveOut(block))
                    changed = true;
            }
        }
    }

    // See 'Computing Liveness Sets for SSA-Form Programs'
    //
    // LiveIn(B) = PhiDefs(B) U UpwardExposed(B) U (LiveOut(B) \ Defs(B))
    // LiveOut(B) = U all S  (LiveIn(S) \ PhiDefs(S)) U PhiUses
    //
    // For a phi-function a_0 = phi(a_1, ..., a_n ) in block B_0, where a_i comes from block B_i, then:
    // * a_0 is considered to be live-in for B_0, but, with respect to this phi-function, it is
    //   not live-out for B_i, i > 0.
    // * a_i, i > 0, is considered to be live-out of B_i , but, with respect to this phi-function,
    //   it is not live-in for B_0.
    // This corresponds to placing a copy of a_i to a_0 on each edge from B_i to B_0.
    //
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
