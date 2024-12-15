package com.compilerprogramming.ezlang.interpreter;

import com.compilerprogramming.ezlang.compiler.BasicBlock;
import com.compilerprogramming.ezlang.compiler.CompiledFunction;
import com.compilerprogramming.ezlang.compiler.Instruction;
import com.compilerprogramming.ezlang.compiler.Operand;
import com.compilerprogramming.ezlang.exceptions.CompilerException;
import com.compilerprogramming.ezlang.exceptions.InterpreterException;
import com.compilerprogramming.ezlang.types.Symbol;
import com.compilerprogramming.ezlang.types.Type;
import com.compilerprogramming.ezlang.types.TypeDictionary;

public class Interpreter {

    TypeDictionary typeDictionary;

    public Interpreter(TypeDictionary typeDictionary) {
        this.typeDictionary = typeDictionary;
    }

    public Value run(String functionName) {
        Symbol symbol = typeDictionary.lookup(functionName);
        if (symbol instanceof Symbol.FunctionTypeSymbol functionSymbol) {
            Frame frame = new Frame(functionSymbol);
            ExecutionStack execStack = new ExecutionStack(1024);
            return interpret(execStack, frame);
        }
        else {
            throw new InterpreterException("Unknown function: " + functionName);
        }
    }

    public Value interpret(ExecutionStack execStack, Frame frame) {
        CompiledFunction currentFunction = frame.bytecodeFunction;
        BasicBlock currentBlock = currentFunction.entry;
        int ip = -1;
        int base = frame.base;
        boolean done = false;
        Value returnValue = null;

        while (!done) {
            Instruction instruction;

            ip++;
            instruction = currentBlock.instructions.get(ip);
            switch (instruction) {
                case Instruction.Return returnInst -> {
                    if (returnInst.from instanceof Operand.ConstantOperand constantOperand) {
                        execStack.stack[base] = new Value.IntegerValue(constantOperand.value);
                    }
                    else if (returnInst.from instanceof Operand.RegisterOperand registerOperand) {
                        execStack.stack[base] = execStack.stack[base+registerOperand.slot()];
                    }
                    else throw new IllegalStateException();
                    returnValue = execStack.stack[base];
                }
                case Instruction.Move moveInst -> {
                    if (moveInst.to instanceof Operand.RegisterOperand toReg) {
                        if (moveInst.from instanceof Operand.RegisterOperand fromReg) {
                            execStack.stack[base + toReg.slot()] = execStack.stack[base + fromReg.slot()];
                        }
                        else if (moveInst.from instanceof Operand.ConstantOperand constantOperand) {
                            execStack.stack[base + toReg.slot()] = new Value.IntegerValue(constantOperand.value);
                        }
                        else throw new IllegalStateException();
                    }
                    else throw new IllegalStateException();
                }
                case Instruction.Jump jumpInst -> {
                    currentBlock = jumpInst.jumpTo;
                    ip = -1;
                    if (currentBlock == currentFunction.exit)
                        done = true;
                }
                case Instruction.ConditionalBranch cbrInst -> {
                    boolean condition;
                    if (cbrInst.condition instanceof Operand.RegisterOperand registerOperand) {
                        Value value = execStack.stack[base + registerOperand.slot()];
                        if (value instanceof Value.IntegerValue integerValue) {
                            condition = integerValue.value != 0;
                        }
                        else {
                            condition = value != null;
                        }
                    }
                    else if (cbrInst.condition instanceof Operand.ConstantOperand constantOperand) {
                        condition = constantOperand.value != 0;
                    }
                    else throw new IllegalStateException();
                    if (condition)
                        currentBlock = cbrInst.trueBlock;
                    else
                        currentBlock = cbrInst.falseBlock;
                    ip = -1;
                    if (currentBlock == currentFunction.exit)
                        done = true;
                }
                case Instruction.Call callInst -> {
                    // Copy args to new frame
                    int baseReg = base+currentFunction.frameSize();
                    int offset = 0;
                    // In this version return reg
                    if (!(callInst.callee.returnType instanceof Type.TypeVoid))
                        offset = 1;
                    int reg = baseReg + offset;
                    for (Operand.RegisterOperand arg: callInst.args) {
                        execStack.stack[base + reg] = execStack.stack[base + arg.slot()];
                        reg += 1;
                    }
                    // Call function
                    Frame newFrame = new Frame(frame, baseReg, callInst.callee);
                    interpret(execStack, newFrame);
                    // Copy return value in expected location
                    if (!(callInst.callee.returnType instanceof Type.TypeVoid)) {
                        execStack.stack[base + callInst.returnOperand.slot()] = execStack.stack[baseReg];
                    }
                }
                case Instruction.Unary unaryInst -> {
                    // We don't expect constant here because we fold constants in unary expressions
                    Operand.RegisterOperand unaryOperand = (Operand.RegisterOperand) unaryInst.operand;
                    Value unaryValue = execStack.stack[base + unaryOperand.slot()];
                    if (unaryValue instanceof Value.IntegerValue integerValue) {
                        switch (unaryInst.unop) {
                            case "-": execStack.stack[base + unaryInst.result.slot()] = new Value.IntegerValue(-integerValue.value); break;
                            // Maybe below we should explicitly set Int
                            case "!": execStack.stack[base + unaryInst.result.slot()] = new Value.IntegerValue(integerValue.value==0?1:0); break;
                            default: throw new CompilerException("Invalid unary op");
                        }
                    }
                    else
                        throw new IllegalStateException("Unexpected unary operand: " + unaryOperand);
                }
                case Instruction.Binary binaryInst -> {
                    long x, y;
                    long value = 0;
                    if (binaryInst.left instanceof Operand.ConstantOperand constant)
                        x = constant.value;
                    else if (binaryInst.left instanceof Operand.RegisterOperand registerOperand)
                        x = ((Value.IntegerValue) execStack.stack[base + registerOperand.slot()]).value;
                    else throw new IllegalStateException();
                    if (binaryInst.right instanceof Operand.ConstantOperand constant)
                        y = constant.value;
                    else if (binaryInst.right instanceof Operand.RegisterOperand registerOperand)
                        y = ((Value.IntegerValue) execStack.stack[base + registerOperand.slot()]).value;
                    else throw new IllegalStateException();
                    switch (binaryInst.binOp) {
                        case "+": value = x + y; break;
                        case "-": value = x - y; break;
                        case "*": value = x * y; break;
                        case "/": value = x / y; break;
                        case "%": value = x % y; break;
                        case "==": value = x == y ? 1 : 0; break;
                        case "!=": value = x != y ? 1 : 0; break;
                        case "<": value = x < y ? 1: 0; break;
                        case ">": value = x > y ? 1 : 0; break;
                        case "<=": value = x <= y ? 1 : 0; break;
                        case ">=": value = x <= y ? 1 : 0; break;
                        default: throw new IllegalStateException();
                    }
                    execStack.stack[base + binaryInst.result.slot()] = new Value.IntegerValue(value);
                }
                case Instruction.NewArray newArrayInst -> {
                    execStack.stack[base + newArrayInst.destOperand.slot()] = new Value.ArrayValue(newArrayInst.type);
                }
                case Instruction.NewStruct newStructInst -> {
                    execStack.stack[base + newStructInst.destOperand.slot()] = new Value.StructValue(newStructInst.type);
                }
                case Instruction.AStoreAppend arrayAppendInst -> {
                    Value.ArrayValue arrayValue = (Value.ArrayValue) execStack.stack[base + arrayAppendInst.array.slot()];
                    if (arrayAppendInst.value instanceof Operand.ConstantOperand constant) {
                        arrayValue.values.add(new Value.IntegerValue(constant.value));
                    }
                    else if (arrayAppendInst.value instanceof Operand.RegisterOperand registerOperand) {
                        arrayValue.values.add(execStack.stack[base + registerOperand.slot()]);
                    }
                    else throw new IllegalStateException();
                }
                case Instruction.ArrayStore arrayStoreInst -> {
                    if (arrayStoreInst.arrayOperand instanceof Operand.RegisterOperand arrayOperand) {
                        Value.ArrayValue arrayValue = (Value.ArrayValue) execStack.stack[base + arrayOperand.slot()];
                        int index = 0;
                        if (arrayStoreInst.indexOperand instanceof Operand.ConstantOperand constant) {
                            index = (int) constant.value;
                        }
                        else if (arrayStoreInst.indexOperand instanceof Operand.RegisterOperand registerOperand) {
                            Value.IntegerValue indexValue = (Value.IntegerValue) execStack.stack[base + registerOperand.slot()];
                            index = (int) indexValue.value;
                        }
                        else throw new IllegalStateException();
                        Value value;
                        if (arrayStoreInst.sourceOperand instanceof Operand.ConstantOperand constantOperand) {
                            value = new Value.IntegerValue(constantOperand.value);
                        }
                        else if (arrayStoreInst.sourceOperand instanceof Operand.RegisterOperand registerOperand) {
                            value = execStack.stack[base + registerOperand.slot()];
                        }
                        else throw new IllegalStateException();
                        arrayValue.values.set(index, value);
                    } else throw new IllegalStateException();
                }
                case Instruction.ArrayLoad arrayLoadInst -> {
                    if (arrayLoadInst.arrayOperand instanceof Operand.RegisterOperand arrayOperand) {
                        Value.ArrayValue arrayValue = (Value.ArrayValue) execStack.stack[base + arrayOperand.slot()];
                        if (arrayLoadInst.indexOperand instanceof Operand.ConstantOperand constant) {
                            execStack.stack[base + arrayLoadInst.destOperand.slot()] = arrayValue.values.get((int) constant.value);
                        }
                        else if (arrayLoadInst.indexOperand instanceof Operand.RegisterOperand registerOperand) {
                            Value.IntegerValue index = (Value.IntegerValue) execStack.stack[base + registerOperand.slot()];
                            execStack.stack[base + arrayLoadInst.destOperand.slot()] = arrayValue.values.get((int) index.value);
                        }
                        else throw new IllegalStateException();
                    } else throw new IllegalStateException();
                }
                case Instruction.SetField setFieldInst -> {
                    if (setFieldInst.structOperand instanceof Operand.RegisterOperand structOperand) {
                        Value.StructValue structValue = (Value.StructValue) execStack.stack[base + structOperand.slot()];
                        int index = setFieldInst.fieldIndex;
                        Value value;
                        if (setFieldInst.sourceOperand instanceof Operand.ConstantOperand constant) {
                            value = new Value.IntegerValue(constant.value);
                        }
                        else if (setFieldInst.sourceOperand instanceof Operand.RegisterOperand registerOperand) {
                            value = execStack.stack[base + registerOperand.slot()];
                        }
                        else throw new IllegalStateException();
                        structValue.fields[index] = value;
                    } else throw new IllegalStateException();
                }
                case Instruction.GetField getFieldInst -> {
                    if (getFieldInst.structOperand instanceof Operand.RegisterOperand structOperand) {
                        Value.StructValue structValue = (Value.StructValue) execStack.stack[base + structOperand.slot()];
                        int index = getFieldInst.fieldIndex;
                        execStack.stack[base + getFieldInst.destOperand.slot()] = structValue.fields[index];
                    } else throw new IllegalStateException();
                }
                case Instruction.ArgInstruction argInst -> {}
                default -> throw new IllegalStateException("Unexpected value: " + instruction);
            }
        }
        return returnValue;
    }

    static class Frame {
        Frame caller;
        int base;
        CompiledFunction bytecodeFunction;

        public Frame(Symbol.FunctionTypeSymbol functionSymbol) {
            this.caller = null;
            this.base = 0;
            this.bytecodeFunction = (CompiledFunction) functionSymbol.code();
        }

        Frame(Frame caller, int base, Type.TypeFunction functionType) {
            this.caller = caller;
            this.base = base;
            this.bytecodeFunction = (CompiledFunction) functionType.code;
        }
    }
}
