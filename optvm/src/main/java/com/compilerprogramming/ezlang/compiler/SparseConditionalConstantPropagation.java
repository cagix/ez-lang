package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.exceptions.CompilerException;
import com.compilerprogramming.ezlang.types.Type;

import java.util.*;

/**
 * Implementation of Sparse Conditional Constant Propagation based on descriptions
 * in:
 *
 * <ol>
 *     <li>Constant Propagation with Conditional Branches. Wegman and Zadeck.</li>
 *     <li>Modern Compiler Implementation in C, Andrew Appel, section 19.3</li>
 *     <li>Building an Optimizing Compiler, Bob Morgan, section 8.3</li>
 * </ol>
 */
public class SparseConditionalConstantPropagation {

    /**
     * Contains a lattice for each SSA definition
     */
    ValueLattice valueLattice;
    /**
     * Executable status for each flow edge, initially all edges are
     * marked non-executable except the start block
     */
    Map<FlowEdge, Boolean> flowEdges;
    /**
     * Worklist of ssaedges (the term used by SCCP paper)
     */
    WorkList<Instruction> instructionWorkList;
    /**
     * As edges between basic blocks become executable, we
     * add them the impacted blocks to the worklist for processing.
     */
    WorkList<BasicBlock> flowWorklist;
    /**
     * We don't evaluate a block more than once (except for Phi instructions
     * in the block). So we have to track which blocks have already been
     * evaluated.
     */
    BitSet visited = new BitSet();
    /**
     * Def use chains for each register
     * Called SSAEdge in the original paper.
     */
    Map<Register, SSAEdges.SSADef> ssaEdges;
    CompiledFunction function;

    /** Used to track reachable blocks when the SCCP changes are applied */
    BitSet executableBlocks = new BitSet();

    public SparseConditionalConstantPropagation constantPropagation(CompiledFunction function) {
        init(function);
        while (!flowWorklist.isEmpty() || !instructionWorkList.isEmpty()) {
            while (!instructionWorkList.isEmpty()) {
                Instruction i = instructionWorkList.pop();
                visitInstruction(i);
            }
            while (!flowWorklist.isEmpty()) {
                BasicBlock b = flowWorklist.pop();
                visitBlock(b);
            }
        }
        return this;
    }

    private void visitBlock(BasicBlock b) {
        for (var phi : b.phis()) {
            visitInstruction(phi);
        }
        if (!visited.get(b.bid)) {
            visited.set(b.bid);
            for (var i : b.instructions) {
                visitInstruction(i);
            }
        }
    }

    private void visitInstruction(Instruction instruction) {
        BasicBlock block = instruction.block;
        if (evalInstruction(instruction)) {
            if (instruction instanceof Instruction.ConditionalBranch || instruction instanceof Instruction.Jump) {
                for (BasicBlock s : block.successors) {
                    if (isEdgeExecutable(block, s)) {
                        flowWorklist.push(block);   // Push both this block and successor to worklist?
                        flowWorklist.push(s);
                    }
                }
            } else if (instruction.definesVar() || instruction instanceof Instruction.Phi) {
                var def = instruction instanceof Instruction.Phi phi ? phi.value() : instruction.def();
                // Push all uses (instructions) of the def into the worklist
                SSAEdges.SSADef ssaDef = ssaEdges.get(def);
                if (ssaDef != null) {
                    for (Instruction use : ssaDef.useList) {
                        if (visited.get(use.block.bid))
                            // Don't visit the instruction if block hasn't been
                            // visited
                            instructionWorkList.push(use);
                    }
                }
            }
        }
    }

    private void init(CompiledFunction function) {
        this.function = function;
        ssaEdges = SSAEdges.buildDefUseChains(function);
        valueLattice = new ValueLattice();
        flowEdges = new HashMap<>();
        for (BasicBlock block : function.getBlocks()) {
            for (BasicBlock s : block.successors) {
                flowEdges.put(new FlowEdge(block, s), false);
            }
        }
        instructionWorkList = new WorkList<>();
        flowWorklist = new WorkList<>();
        flowWorklist.push(function.entry);
        visited = new BitSet();
    }

    public SparseConditionalConstantPropagation apply(EnumSet<Options> options) {
        /*
        The constant propagation algorithm does not change the flow graph - it computes
        information about the flow graph. The compiler now uses this information to improve
        the graph in the following ways:

        * The instructions corresponding to temporaries that evaluate as constants are modified
        to be load constant instructions.

        • An edge that has not become executable is eliminated, and the conditional branching
        instruction representing that edge is modified to be a simpler instruction.
        The phi-nodes at the head of the edge are modified to have one less operand.

        • Blocks that become unreachable are eliminated.

         Bob Morgan. Building an Optimizing Compiler
         */
        if (options.contains(Options.DUMP_SCCP_PREAPPLY)) {
            System.out.println("SCCP analysis\n");
            System.out.println(toString());
        }
        markExecutableBlocks();
        removeBranchesThatAreNotExecutable();
        replaceVarsWithConstants();
        // Unreachable blocks are eliminated as there are no paths to them
        if (options.contains(Options.DUMP_SCCP_POSTAPPLY)) function.dumpIR(false, "Post SCCP\n");
        return this;
    }

    private void markExecutableBlocks() {
        var blocks = function.getBlocks();
        executableBlocks = new BitSet(blocks.size());
        executableBlocks.set(function.entry.bid);
        for (FlowEdge edge: flowEdges.keySet()) {
            if (flowEdges.get(edge)) {
                executableBlocks.set(edge.source.bid);
                executableBlocks.set(edge.target.bid);
            }
        }
    }

    /**
     * Where we know which branch will be executed on a CBR,
     * we replace such a branch with a jump to the known
     * basic block
     */
    private void removeBranchesThatAreNotExecutable() {
        for (var flowEdge : flowEdges.keySet()) {
            if (!flowEdges.get(flowEdge)) {
                if (executableBlocks.get(flowEdge.source.bid) ||
                    executableBlocks.get(flowEdge.target.bid))
                    removeEdge(flowEdge.source, flowEdge.target);
            }
        }
    }

    private void removeEdge(BasicBlock source, BasicBlock target) {
        int j = target.whichPred(source);
        // Replace cbr with jump
        int idx = source.instructions.size()-1;
        Instruction instruction = source.instructions.get(idx);
        if (instruction instanceof Instruction.ConditionalBranch cbr) {
            BasicBlock remainingExecutableBlock = (cbr.falseBlock == target) ? cbr.trueBlock : cbr.falseBlock;
            source.instructions.set(idx, new Instruction.Jump(remainingExecutableBlock));
        }
        // Remove phis in target corresponding to the input
        for (var phi: target.phis()) {
            phi.removeInput(j);
        }
        // update cfg
        source.removeSuccessor(target);
    }

    /**
     * Where a definition is known to be a constant,
     * replace all uses with the constant and then delete
     * the defining instruction.
     */
    private void replaceVarsWithConstants() {
        for (var register: valueLattice.getRegisters()) {
            var latticeElement = valueLattice.get(register);
            if (latticeElement.kind == V_CONSTANT) {
                var constant = new Operand.ConstantOperand(latticeElement.value, register.type);
                var defUseChain = this.ssaEdges.get(register);
                // replace uses with constant
                for (var usingInstruction: defUseChain.useList) {
                    if (executableBlocks.get(usingInstruction.block.bid))
                        usingInstruction.replaceWithConstant(register, constant);
                }
                defUseChain.useList.clear();
                var block = defUseChain.instruction.block;
                // delete defining instruction
                block.deleteInstruction(defUseChain.instruction);
                ssaEdges.remove(register);
            }
        }
    }

    static final byte V_UNDEFINED = 1;  // TOP
    static final byte V_CONSTANT = 2;
    static final byte V_VARYING = 3;    // BOTTOM

    // Associated with each register
    static final class LatticeElement {
        private byte kind;
        private long value;

        public LatticeElement(byte kind, long value) {
            this.kind = kind;
            this.value = value;
        }

        boolean meet(long value) {
            byte oldKind = this.kind;
            long oldValue = this.value;
            if (kind == V_UNDEFINED) {
                kind = V_CONSTANT;
                this.value = value;
            } else if (kind == V_CONSTANT && this.value != value) {
                kind = V_VARYING;
            }
            return kind != oldKind || value != oldValue;
        }

        boolean meet(LatticeElement other) {
            byte oldKind = this.kind;
            long oldValue = this.value;
            if (kind == V_UNDEFINED) {
                kind = other.kind;
                value = other.value;
            } else if (kind == V_CONSTANT) {
                if (other.kind == V_CONSTANT) {
                    if (other.value != value) {
                        kind = V_VARYING;
                    }
                } else if (other.kind == V_VARYING) {
                    kind = V_VARYING;
                }
            }
            return kind != oldKind || value != oldValue;
        }

        public boolean setKind(byte kind) {
            byte oldKind = this.kind;
            this.kind = kind;
            return oldKind != kind;
        }
        @Override
        public String toString() {
            if (kind == V_UNDEFINED) {
                return "undefined";
            }
            else if (kind == V_CONSTANT) {
                return String.valueOf(value);
            }
            return "varying";
        }
    }

    // A CFG edge
    static final class FlowEdge {
        BasicBlock source;
        BasicBlock target;

        public FlowEdge(BasicBlock source, BasicBlock target) {
            this.target = target;
            this.source = source;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FlowEdge that = (FlowEdge) o;
            return (source.bid == that.source.bid) && (target.bid == that.target.bid);
        }

        @Override
        public int hashCode() {
            return source.bid + target.bid;
        }

        @Override
        public String toString() {
            return "L"+source.bid+"->L"+target.bid;
        }
    }

    /**
     * This is based on the description of CP_Evaluate(I) in
     * Building an Optimizing Compiler. It evaluates an instruction and
     * if the instruction defines an SSA variable, then it updates the lattice
     * value of that variable. If the lattice changes then this returns true,
     * else false. For branches the change in executable status of an edge is
     * used instead of the lattice value change.
     */
    private boolean evalInstruction(Instruction instruction) {
        BasicBlock block = instruction.block;
        boolean changed = false;
        switch (instruction) {
            case Instruction.Ret retInst -> {
                // TODO is this correct?
            }
            case Instruction.Move moveInst -> {
                if (moveInst.to() instanceof Operand.RegisterOperand toReg) {
                    var cell = valueLattice.get(toReg.reg);
                    if (moveInst.from() instanceof Operand.RegisterOperand fromReg) {
                        changed = cell.meet(valueLattice.get(fromReg.reg));
                    } else if (moveInst.from() instanceof Operand.ConstantOperand constantOperand) {
                        changed = cell.meet(constantOperand.value);
                    } else throw new IllegalStateException();
                } else throw new IllegalStateException();
            }
            case Instruction.Jump jumpInst -> {
                changed = markEdgeExecutable(block, jumpInst.jumpTo);
            }
            case Instruction.ConditionalBranch cbrInst -> {
                if (cbrInst.condition() instanceof Operand.RegisterOperand registerOperand) {
                    var cell = valueLattice.get(registerOperand.reg);
                    if (cell.kind == V_CONSTANT) {
                        if (cell.value != 0) {
                            changed = markEdgeExecutable(block, cbrInst.trueBlock);
                        } else {
                            changed = markEdgeExecutable(block, cbrInst.falseBlock);
                        }
                    } else if (cell.kind == V_VARYING) {
                        boolean changed0 = markEdgeExecutable(block, cbrInst.trueBlock);
                        boolean changed1 = markEdgeExecutable(block, cbrInst.falseBlock);
                        changed = changed0 || changed1;
                    }
                } else if (cbrInst.condition() instanceof Operand.ConstantOperand constantOperand) {
                    if (constantOperand.value != 0) {
                        changed = markEdgeExecutable(block, cbrInst.trueBlock);
                    } else {
                        changed = markEdgeExecutable(block, cbrInst.falseBlock);
                    }
                } else throw new IllegalStateException();
            }
            case Instruction.Call callInst -> {
                if (!(callInst.callee.returnType instanceof Type.TypeVoid)) {
                    var cell = valueLattice.get(callInst.returnOperand().reg);
                    changed = cell.setKind(V_VARYING);
                }
            }
            case Instruction.Unary unaryInst -> {
                Operand.RegisterOperand unaryOperand = (Operand.RegisterOperand) unaryInst.operand();
                var cell = valueLattice.get(unaryInst.result().reg);
                var input = valueLattice.get(unaryOperand.reg);
                if (input.kind == V_CONSTANT) {
                    changed = cell.meet(unaryInst.unop.equals("-") ? -input.value : (input.value == 0 ? 1 : 0));
                } else {
                    changed = cell.meet(input);
                }
            }
            case Instruction.Binary binaryInst -> {
                var cell = valueLattice.get(binaryInst.result().reg);
                LatticeElement left, right;
                if (binaryInst.left() instanceof Operand.ConstantOperand constant)
                    left = new LatticeElement(V_CONSTANT, constant.value);
                else if (binaryInst.left() instanceof Operand.RegisterOperand registerOperand)
                    left = valueLattice.get(registerOperand.reg);
                else throw new IllegalStateException();
                if (binaryInst.right() instanceof Operand.ConstantOperand constant)
                    right = new LatticeElement(V_CONSTANT, constant.value);
                else if (binaryInst.right() instanceof Operand.RegisterOperand registerOperand)
                    right = valueLattice.get(registerOperand.reg);
                else throw new IllegalStateException();
                switch (binaryInst.binOp) {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                    case "%":
                        changed = evalArith(cell, left, right, binaryInst.binOp);
                        break;
                    case "==":
                    case "!=":
                    case "<":
                    case ">":
                    case "<=":
                    case ">=":
                        changed = evalLogical(cell, left, right, binaryInst.binOp);
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
            case Instruction.NewArray newArrayInst -> {
                var cell = valueLattice.get(newArrayInst.destOperand().reg);
                changed = cell.setKind(V_VARYING);
            }
            case Instruction.NewStruct newStructInst -> {
                var cell = valueLattice.get(newStructInst.destOperand().reg);
                changed = cell.setKind(V_VARYING);
            }
            case Instruction.AStoreAppend arrayAppendInst -> {
            }
            case Instruction.ArrayStore arrayStoreInst -> {
            }
            case Instruction.ArrayLoad arrayLoadInst -> {
                var cell = valueLattice.get(arrayLoadInst.destOperand().reg);
                changed = cell.setKind(V_VARYING);
            }
            case Instruction.SetField setFieldInst -> {
            }
            case Instruction.GetField getFieldInst -> {
                var cell = valueLattice.get(getFieldInst.destOperand().reg);
                changed = cell.setKind(V_VARYING);
            }
            case Instruction.ArgInstruction argInst -> {
                var cell = valueLattice.get(argInst.def());
                changed = cell.setKind(V_VARYING);
            }
            case Instruction.Phi phiInst -> {
                changed = visitPhi(block, phiInst);
            }
            default -> throw new IllegalStateException("Unexpected value: " + instruction);
        }
        return changed;
    }

    private boolean visitPhi(BasicBlock block, Instruction.Phi phiInst) {
        LatticeElement oldValue = valueLattice.get(phiInst.value());
        LatticeElement newValue = new LatticeElement(V_UNDEFINED, 0);
        for (int j = 0; j < block.predecessors.size(); j++) {
            BasicBlock pred = block.predecessors.get(j);
            // We ignore non-executable edges
            if (isEdgeExecutable(pred, block)) {
                LatticeElement varValue = valueLattice.get(phiInst.inputAsRegister(j));
                newValue.meet(varValue);
            }
        }
        return oldValue.meet(newValue);
    }

    private boolean isEdgeExecutable(BasicBlock source, BasicBlock target) {
        return flowEdges.get(new FlowEdge(source, target));
    }
    private boolean markEdgeExecutable(BasicBlock source, BasicBlock target) {
        var edge = new FlowEdge(source, target);
        var oldValue = flowEdges.get(edge);
        assert oldValue != null;
        if (!oldValue) {
            // Mark edge as executable
            flowEdges.put(edge, true);
            return true;
        }
        return false;
    }

    private static boolean evalLogical(LatticeElement cell, LatticeElement left, LatticeElement right, String binOp) {
        boolean changed = false;
        if (left.kind == V_CONSTANT && right.kind == V_CONSTANT) {
            long leftValue = left.value;
            long rightValue = right.value;
            long result;
            switch (binOp) {
                case "==":
                    result = leftValue == rightValue ? 1 : 0;
                    break;
                case "!=":
                    result = leftValue != rightValue ? 1 : 0;
                    break;
                case "<":
                    result = leftValue < rightValue ? 1 : 0;
                    break;
                case ">":
                    result = leftValue > rightValue ? 1 : 0;
                    break;
                case "<=":
                    result = leftValue <= rightValue ? 1 : 0;
                    break;
                case ">=":
                    result = leftValue >= rightValue ? 1 : 0;
                    break;
                default:
                    throw new IllegalStateException();
            }
            changed = cell.meet(result);
        } else if (left.kind == V_VARYING || right.kind == V_VARYING) {
            // We could constrain the result here to the set [0-1]
            // but we don't track ranges or sets of values
            changed = cell.setKind(V_VARYING);
        }
        return changed;
    }

    private static boolean evalArith(LatticeElement cell, LatticeElement left, LatticeElement right, String binOp) {
        boolean changed = false;
        if (left.kind == V_CONSTANT && right.kind == V_CONSTANT) {
            long leftValue = left.value;
            long rightValue = right.value;
            long result;
            switch (binOp) {
                case "+":
                    result = leftValue + rightValue;
                    break;
                case "-":
                    result = leftValue - rightValue;
                    break;
                case "/":
                    if (rightValue == 0) throw new CompilerException("Division by zero");
                    result = leftValue / rightValue;
                    break;
                case "*":
                    result = leftValue * rightValue;
                    break;
                case "%":
                    result = leftValue % rightValue;
                    break;
                default:
                    throw new IllegalStateException();
            }
            changed = cell.meet(result);
        } else if (binOp.equals("*") && ((left.kind == V_CONSTANT && left.value == 0) || (right.kind == V_CONSTANT && right.value == 0))) {
            // multiplication with 0 yields 0
            changed = cell.meet(0);
        } else if (left.kind == V_VARYING || right.kind == V_VARYING) {
            changed = cell.setKind(V_VARYING);
        }
        return changed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Flow edges:\n");
        for (var edge : flowEdges.keySet()) {
            if (flowEdges.get(edge)) {
                sb.append(edge).append("=Executable").append("\n");
            }
            else {
                sb.append(edge).append("=NOT Executable").append("\n");
            }
        }
        sb.append("Lattices:\n");
        for (var register: valueLattice.getRegisters()) {
            sb.append(register.name()).append("=").append(valueLattice.get(register)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Maintains a Lattice for each SSA variable - i.e register
     * Initial value of lattice is TOP/Undefined
     */
    static final class ValueLattice {

        private final Map<Register, LatticeElement> valueLattice = new HashMap<>();

        LatticeElement get(Register reg) {
            var cell = valueLattice.get(reg);
            if (cell == null) {
                // Initial value is UNDEFINED/TOP
                cell = new LatticeElement(V_UNDEFINED, 0);
                valueLattice.put(reg, cell);
            }
            return cell;
        }
        Set<Register> getRegisters() {
            return valueLattice.keySet();
        }
    }

    static final class WorkList<E> {
        Set<E> members = new HashSet<>();
        List<E> list = new ArrayList<>();

        void push(E element) {
            if (members.add(element)) {
                list.add(element);
            }
        }

        E pop() {
            if (list.isEmpty()) return null;
            var x = list.removeFirst();
            members.remove(x);
            return x;
        }

        boolean isEmpty() {
            return list.isEmpty();
        }
    }
}
