# Optimizing Compiler for Register VM

This module implements various compiler optimization techniques such as:

* Static Single Assignment
* Liveness Analysis
* Graph Coloring Register Allocator (Chaitin)

Our goal here is to perform optimizations on the Intermediate Representation targeting an abstract machine, rather than
a physical machine. Therefore, all our optimization passes will work on the instruction set of this abstract machine.

A VM / Interpreter is provided that can run the generated code.

## Intermediate Representation

* [Register](src/main/java/com/compilerprogramming/ezlang/compiler/Register.java) - implements a virtual register. Virtual registers
  have a name, type, id, frameSlot - the id is unique, but the name is not. Initially the compiler generates unique registers for every local
  and temporary, the number of registers grows as we convert to SSA and back out of SSA. Finally, as we run the Chaitin register allocator
  we shrink down the number of virtual registers to the minimum. When executing code, the abstract machine uses the frameSlot as the location
  of the virtual register in the executing function's stack frame. Registers that do not have overlapping lifetimes can share the same
  frameSlot.
* [RegisterPool](src/main/java/com/compilerprogramming/ezlang/compiler/RegisterPool.java) - simple pool to allow us to find a register
  by its id, and to allocate new virtual registers.
* [BasicBlock](src/main/java/com/compilerprogramming/ezlang/compiler/BasicBlock.java) - Defines our basic block - which contains instructions
  that execute sequentially. A basic block ends with a branch. There are two distinguished basic blocks in every function: entry and exit.
* [BBHelper](src/main/java/com/compilerprogramming/ezlang/compiler/BBHelper.java) - Some utilities that manipulate basic blocks.
* [Operand](src/main/java/com/compilerprogramming/ezlang/compiler/Operand.java) - Operands in instructions. Both definitions and uses are treated
  as operands. Instruction can have at most one definition; but an instruction can have multiple use operands. Operands can hold registers or
  constants or pointers to basic blocks.
* [Instruction](src/main/java/com/compilerprogramming/ezlang/compiler/Instruction.java) - Instructions - sequential instructions reside in
  basic blocks. Some instructions define variables (registers) and some use them.

## Dominators and Liveness Analysis

* [DominatorTree](src/main/java/com/compilerprogramming/ezlang/compiler/DominatorTree.java) - Calculates dominator tree and dominance frontiers.
* [LiveSet](src/main/java/com/compilerprogramming/ezlang/compiler/LiveSet.java) - Bitset used to track liveness of registers. We exploit the fact that 
  each register has a unique integer ID and these ids are allocated in a sequential manner.
* [Liveness](src/main/java/com/compilerprogramming/ezlang/compiler/Liveness.java) - Liveness calculator, works for both SSA and non-SSA forms. Computes
  liveness data per basic block - mainly live-out. Note that the interference graph builder starts here and computes instruction level liveness as necessary.

## Static Single Assignment Form

* [EnterSSA](src/main/java/com/compilerprogramming/ezlang/compiler/EnterSSA.java) - Transforms into SSA, using [algorithm by Preston Briggs](https://dl.acm.org/doi/10.5555/295545.295551). This is the traditional method of constructing 
  SSA Form using Dominator Trees. The input to this transformation is regular IR, output is SSA IR.
* Incremental SSA - This method generate SSA IR directly from the AST, using [Braun's algorithm](https://dl.acm.org/doi/10.1007/978-3-642-37051-9_6) - this is integrated into the
  [compiler](src/main/java/com/compilerprogramming/ezlang/compiler/CompiledFunction.java) itself and can be enabled using an option.
* [ExitSSA](src/main/java/com/compilerprogramming/ezlang/compiler/ExitSSA.java) - Exits SSA form - i.e. implements SSA destruction. Two methods are implemented:
  * [ExitSSABriggs](src/main/java/com/compilerprogramming/ezlang/compiler/ExitSSABriggs.java) - implements method described by [Preston Briggs](https://dl.acm.org/doi/10.5555/295545.295551).
  * [ExitSSABoissinotNoCoalesce](src/main/java/com/compilerprogramming/ezlang/compiler/ExitSSABoissinotNoCoalesce.java) - implements method described by [Benoit Boissinot](https://inria.hal.science/inria-00349925v1/document) - 
    without the coalescing step. Thus, it is the "naive" approach that resembles Sreedhar's method I. 

## Optimizations on SSA Form

* [SparseConditionalConstantPropagation](src/main/java/com/compilerprogramming/ezlang/compiler/SparseConditionalConstantPropagation.java) - Conditional Constant Propagation on SSA form (SCCP). This is an implementation of the paper [Constant propagation with conditional branches](https://dl.acm.org/doi/10.1145/103135.103136).
* [ConstantComparisonPropagation](src/main/java/com/compilerprogramming/ezlang/compiler/ConstantComparisonPropagation.java) - Detects equals and not equals against constants within conditionals,
   and inserts scoped variables with appropriately specialized type within the dominated blocks, so that a second pass of SCCP can further optimize code.
* [SSAEdges](src/main/java/com/compilerprogramming/ezlang/compiler/SSAEdges.java) - SSAEdges are def-use chains used by SCCP algorithm, and also generated during incremental SSA construction using Braun's method.

## Loops 

These components are not used yet

* [LoopFinder](src/main/java/com/compilerprogramming/ezlang/compiler/LoopFinder.java) - Discovers loops. (Not used yet)
* [LoopNest](src/main/java/com/compilerprogramming/ezlang/compiler/LoopNest.java) - Representation of loop nesting. (Not used yet)

## Chaitin Graph Coloring Register Allocation

Note that our goal is to target an abstract machine rather than a real CPU, so we optimize for minimum VM registers, but we have 
unlimited amount of those.

* [InterferenceGraph](src/main/java/com/compilerprogramming/ezlang/compiler/InterferenceGraph.java) - Representation of an Interference Graph
  required by the register allocator.
* [InterferenceGraphBuilder](src/main/java/com/compilerprogramming/ezlang/compiler/InterferenceGraphBuilder.java) - Constructs an InterferenceGraph for a set
  of basic bocks, using basic block level liveness information as a starting point for calculating instruction level liveness.
* [ChaitinGraphColoringRegisterAllocator](src/main/java/com/compilerprogramming/ezlang/compiler/ChaitinGraphColoringRegisterAllocator.java) - basic
  [Chaitin Graph Coloring Register Allocator](https://web.eecs.umich.edu/~mahlke/courses/583f12/reading/chaitin82.pdf) without spilling. Since our target machine here is an abstract machine, we do not really need spilling support
  as we can size each function's stack frame to accommodate the number of registers needed such that each register is really a slot in the stack
  frame. But we will eventually simulate an abstract machine with a limited set of registers and a separate stack frame.

## Compiler

* [CompiledFunction](src/main/java/com/compilerprogramming/ezlang/compiler/CompiledFunction.java) - builds and encapsulates the IR for a single function.
* [Compiler](src/main/java/com/compilerprogramming/ezlang/compiler/Compiler.java) - simple orchestrator of compilation tasks.
* [Optimizer](src/main/java/com/compilerprogramming/ezlang/compiler/Optimizer.java) - simple orchestrator of optimization steps. Currently
  does not have optimization passes, but translates to SSA and out and then runs the graph coloring register allocator.

## VM/Interpreter

A simple VM / Interpreter is provided that can run the IR, both pre and post optimizations.
The SSA form is not executable, hence the IR must be transformed out of SSA for execution.

* [VM/Interpreter](src/main/java/com/compilerprogramming/ezlang/interpreter)
