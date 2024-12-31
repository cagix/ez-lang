package com.compilerprogramming.ezlang.compiler;

import com.compilerprogramming.ezlang.types.Type;

import java.util.*;

/**
 * Implementation of Sparse Conditional Constant Propagation based on descriptions
 * in:
 *
 * <ol>
 *     <li>Constant Propagation with Conditional Branches. Wegman and Zadeck.</li>
 *     <li>Modern Compiler Implementation in C</li>
 *     <li></li>
 * </ol>
 *
 *
 */
public class SparseConditionalConstantPropagation {

    /**
     * Contains a lattice for all possible definitions
     */
    ValueLattice valueLattice;
    /**
     * Executable status for each flow edge
     */
    Map<FlowEdge, Boolean> flowEdges;
    WorkList<Instruction> instructionWorkList;
    WorkList<BasicBlock> flowWorklist;
    BitSet visited = new BitSet();
    /**
     * Def use chains for each register
     * Called SSAEdge in the original paper.
     */
    Map<Integer, SSAEdges.SSADef> ssaEdges;
    CompiledFunction function;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Flow edges:\n");
        for (var edge : flowEdges.keySet()) {
            if (flowEdges.get(edge)) {
                sb.append(edge).append("=Executable").append("\n");
            }
        }
        sb.append("Lattices:\n");
        for (var id: valueLattice.valueLattice.keySet()) {
            var register = function.registerPool.getReg(id);
            sb.append(register.name()).append("=").append(valueLattice.get(register)).append("\n");
        }
        return sb.toString();
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
                        flowWorklist.push(block);   // Is this correct ?
                        flowWorklist.push(s);
                    }
                }
            } else if (instruction.definesVar() || instruction instanceof Instruction.Phi) {
                var def = instruction instanceof Instruction.Phi phi ? phi.value() : instruction.def();
                // Push all uses (instructions) of the def into the worklist
                SSAEdges.SSADef ssaDef = ssaEdges.get(def.id);
                if (ssaDef != null) {
                    for (Instruction use : ssaDef.useList) {
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
                // Copy args to new frame
                // Copy return value in expected location
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
            if (isEdgeExecutable(pred, block)) {
                LatticeElement varValue = valueLattice.get(phiInst.input(j));
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
            var result = new LatticeElement(V_CONSTANT, 0);
            changed = cell.meet(result);
        } else if (left.kind == V_VARYING || right.kind == V_VARYING) {
            changed = cell.setKind(V_VARYING);
        }
        return changed;
    }

    static final class ValueLattice {
        Map<Integer, LatticeElement> valueLattice = new HashMap<>();

        LatticeElement get(Register reg) {
            var cell = valueLattice.get(reg.id);
            if (cell == null) {
                cell = new LatticeElement(V_UNDEFINED, 0);
                valueLattice.put(reg.id, cell);
            }
            return cell;
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
