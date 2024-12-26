# Optimizing Compiler for Register VM

This module implements various compiler optimization techniques such as:

* Static Single Assignment
* Liveness Analysis
* Graph Coloring Register Allocator (Chaitin)

Our goal here is to perform optimizations on the Intermediate Representation targeting an abstract machine, rather than
a physical machine. Therefore, all our optimization passes will work on the instruction set of this abstract machine.


## Guide

* [Register](src/main/java/com/compilerprogramming/ezlang/compiler/Register.java) - implements a virtual register. Virtual registers
  have a name, type, and id - the id is unique, but name is not. Initially the compiler generates unique registers for every local
  and temporary, the number of registers grows as we convert to SSA and back out of SSA. Finally, as we run the Chaitin register allocator
  we shrink down the number of virtual registers to the minimum.
* [RegisterPool](src/main/java/com/compilerprogramming/ezlang/compiler/RegisterPool.java) - simple pool to allow us to find a register
  by its id, and to allocate new virtual registers.
* [CompiledFunction](src/main/java/com/compilerprogramming/ezlang/compiler/CompiledFunction.java) - encapsulates the IR for a single function.
* [BasicBlock](src/main/java/com/compilerprogramming/ezlang/compiler/BasicBlock.java) - Defines our basic block - which contains instructions
  that execute sequentially. A basic block ends with a branch. There are two distinguished basic blocks in every function: entry and exit. 
* [BBHelper](src/main/java/com/compilerprogramming/ezlang/compiler/BBHelper.java) - Some utilities that manipulate basic blocks.
* [Operand](src/main/java/com/compilerprogramming/ezlang/compiler/Operand.java) - Operands in instructions.
* [Instruction](src/main/java/com/compilerprogramming/ezlang/compiler/Instruction.java) - Instructions in basic blocks.
* [DominatorTree](src/main/java/com/compilerprogramming/ezlang/compiler/DominatorTree.java) - Calculates dominator tree and dominance frontiers.
* [LiveSet](src/main/java/com/compilerprogramming/ezlang/compiler/LiveSet.java) - Bitset used to track liveness of registers.
* [Liveness](src/main/java/com/compilerprogramming/ezlang/compiler/Liveness.java) - Liveness calculator, works for both SSA and non-SSA forms.
* [EnterSSA](src/main/java/com/compilerprogramming/ezlang/compiler/EnterSSA.java) - Transforms into SSA, using algorithm by Preston Briggs.
* [ExitSSA](src/main/java/com/compilerprogramming/ezlang/compiler/ExitSSA.java) - Exits SSA form, using algorithm by Preston Briggs.
* [LoopFinder](src/main/java/com/compilerprogramming/ezlang/compiler/LoopFinder.java) - Discovers loops.
* [LoopNest](src/main/java/com/compilerprogramming/ezlang/compiler/LoopNest.java) - Representation of loop nesting.
* [InterferenceGraph](src/main/java/com/compilerprogramming/ezlang/compiler/InterferenceGraph.java) - Representation of an Interference Graph
  required by the register allocator.
* [InterferenceGraphBuilder](src/main/java/com/compilerprogramming/ezlang/compiler/InterferenceGraph.java) - Constructs InteferenceGraph for a set
  of basic bocks, using liveness information.
* [ChaitinGraphColoringRegisterAllocator](src/main/java/com/compilerprogramming/ezlang/compiler/ChaitinGraphColoringRegisterAllocator.java) - basic
  Chaitin Graph Coloring Register Allocator - WIP.
