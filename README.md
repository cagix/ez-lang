# EZ Programming Language

This project is part of the https://compilerprogramming.github.io/ project.

The EZ (pronounced EeZee) programming language is designed to allow us to learn various compiler techniques.

## The EZ Language

The EZ programming language is a tiny statically typed language with syntax inspired by Swift.
The language has the following features:

* Integer, Struct and 1-Dimensional Array types
* If and While statements
* Functions

The language syntax is described [ANTLR Grammar](antlr-parser/src/main/antlr4/com/compilerprogramming/ezlang/antlr/EZLanguage.g4).
The language is intentionally very simple and is meant to have just enough functionality to experiment with compiler implementation techniques.

## Modules

The project is under development and subject to change. At this point in time, we have following initial implementations:

* lexer - a simple tokenizer
* parser - a recursive descent parser and AST
* types - the type definitions
* semantic - semantic analyzer
* stackvm - a bytecode compiler that generates stack IR (bytecode interpreter not yet available)
* registervm - a bytecode compiler that generates a linear register IR and a bytecode interpreter that can execute the IR

## How can you contribute?

Obviously firstly any contributes that improve and fix bugs are welcome. I am not keen on language extensions at this stage, but eventually
we will be extending the language to explore more advanced features.

I am also interested in creating implementations of this project in C++, Go, Rust, swift, D, C, etc. If you are interested in working on such a 
port please contact me via [Discussions](https://github.com/orgs/CompilerProgramming/discussions).

## Community Discussions

There is a [community discussion forum](https://github.com/orgs/CompilerProgramming/discussions).

## What's next

The project has only just got started, there is lots to do!. See the plan in the [website](https://compilerprogramming.github.io/).
More documentation to follow, but for now please refer to the source code and the site above.

