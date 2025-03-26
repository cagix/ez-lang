package com.compilerprogramming.ezlang.semantic;

import com.compilerprogramming.ezlang.exceptions.CompilerException;
import com.compilerprogramming.ezlang.lexer.Token;
import com.compilerprogramming.ezlang.parser.AST;
import com.compilerprogramming.ezlang.parser.ASTVisitor;
import com.compilerprogramming.ezlang.types.Scope;
import com.compilerprogramming.ezlang.types.Type;
import com.compilerprogramming.ezlang.types.TypeDictionary;

/**
 * The goal of this semantic analysis pass is to define
 * functions and struct types.
 */
public class SemaAssignTypes implements ASTVisitor {
    Scope currentScope;
    AST.StructDecl currentStructDecl;
    AST.FuncDecl currentFuncDecl;
    final TypeDictionary typeDictionary;

    public SemaAssignTypes(TypeDictionary typeDictionary) {
        this.typeDictionary = typeDictionary;
    }

    @Override
    public ASTVisitor visit(AST.Program program, boolean enter) {
        if (enter) {
            currentScope = program.scope;
        }
        else {
            currentScope = currentScope.parent;
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.FuncDecl funcDecl, boolean enter) {
        if (enter) {
            currentScope = funcDecl.scope;
            currentFuncDecl = funcDecl;
        }
        else {
            currentScope = currentScope.parent;
            currentFuncDecl = null;
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.StructDecl structDecl, boolean enter) {
        if (enter) {
            currentScope = structDecl.scope;
            currentStructDecl = structDecl;
        }
        else {
            currentScope = currentScope.parent;
            currentStructDecl = null;
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.VarDecl varDecl, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.BinaryExpr binaryExpr, boolean enter) {
        if (!enter) {
            validType(binaryExpr.expr1.type, true);
            validType(binaryExpr.expr2.type, true);
            if (binaryExpr.expr1.type instanceof Type.TypeInteger &&
                binaryExpr.expr2.type instanceof Type.TypeInteger) {
                // booleans are int too
                binaryExpr.type = typeDictionary.INT;
            }
            else if (((binaryExpr.expr1.type instanceof Type.TypeNull &&
                     binaryExpr.expr2.type instanceof Type.TypeNullable) ||
                    (binaryExpr.expr1.type instanceof Type.TypeNullable &&
                     binaryExpr.expr2.type instanceof Type.TypeNull) ||
                    (binaryExpr.expr1.type instanceof Type.TypeNull &&
                     binaryExpr.expr2.type instanceof Type.TypeNull)) &&
                    (binaryExpr.op.str.equals("==") || binaryExpr.op.str.equals("!="))) {
                binaryExpr.type = typeDictionary.INT;
            }
            else {
                throw new CompilerException("Binary operator " + binaryExpr.op + " not supported for operands");
            }
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.UnaryExpr unaryExpr, boolean enter) {
        if (enter) {
            return this;
        }
        validType(unaryExpr.expr.type, false);
        if (unaryExpr.expr.type instanceof Type.TypeInteger ti) {
            unaryExpr.type = unaryExpr.expr.type;
        }
        else {
            throw new CompilerException("Unary operator " + unaryExpr.op + " not supported for operand");
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.GetFieldExpr fieldExpr, boolean enter) {
        if (enter)
            return this;
        validType(fieldExpr.object.type, false);
        Type.TypeStruct structType = null;
        if (fieldExpr.object.type instanceof Type.TypeStruct ts) {
            structType = ts;
        }
        else if (fieldExpr.object.type instanceof Type.TypeNullable ptr &&
                ptr.baseType instanceof Type.TypeStruct ts) {
            structType = ts;
        }
        else
            throw new CompilerException("Unexpected struct type " + fieldExpr.object.type);
        var fieldType = structType.getField(fieldExpr.fieldName);
        if (fieldType == null)
            throw new CompilerException("Struct " + structType + " does not have field named " + fieldExpr.fieldName);
        fieldExpr.type = fieldType;
        return this;
    }

    @Override
    public ASTVisitor visit(AST.SetFieldExpr fieldExpr, boolean enter) {
        if (enter)
            return this;
        validType(fieldExpr.object.type, true);
        Type.TypeStruct structType = null;
        if (fieldExpr.object.type instanceof Type.TypeStruct ts) {
            structType = ts;
        }
        else if (fieldExpr.object.type instanceof Type.TypeNullable ptr &&
                ptr.baseType instanceof Type.TypeStruct ts) {
            structType = ts;
        }
        else
            throw new CompilerException("Unexpected struct type " + fieldExpr.object.type);
        var fieldType = structType.getField(fieldExpr.fieldName);
        if (fieldType == null)
            throw new CompilerException("Struct " + structType + " does not have field named " + fieldExpr.fieldName);
        validType(fieldExpr.value.type, true);
        checkAssignmentCompatible(fieldType, fieldExpr.value.type);
        fieldExpr.type = fieldType;
        return this;
    }


    @Override
    public ASTVisitor visit(AST.CallExpr callExpr, boolean enter) {
        if (!enter) {
            validType(callExpr.callee.type, false);
            if (callExpr.callee.type instanceof Type.TypeFunction f) {
                callExpr.type = f.returnType;
            }
            else
                throw new CompilerException("Call target must be a function");
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.SimpleTypeExpr simpleTypeExpr, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.NullableSimpleTypeExpr simpleTypeExpr, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.ArrayTypeExpr arrayTypeExpr, boolean enter) {
        return this;
    }

    public ASTVisitor visit(AST.NullableArrayTypeExpr arrayTypeExpr, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.ReturnTypeExpr returnTypeExpr, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.LiteralExpr literalExpr, boolean enter) {
        if (enter) {
            if (literalExpr.value.kind == Token.Kind.NUM) {
                literalExpr.type = typeDictionary.INT;
            }
            else if (literalExpr.value.kind == Token.Kind.IDENT
                     && literalExpr.value.str.equals("null")) {
                literalExpr.type = typeDictionary.NULL;
            }
            else {
                throw new CompilerException("Unsupported literal " + literalExpr.value);
            }
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.ArrayLoadExpr arrayIndexExpr, boolean enter) {
        if (!enter) {
            validType(arrayIndexExpr.array.type, false);
            Type.TypeArray arrayType = null;
            if (arrayIndexExpr.array.type instanceof Type.TypeArray ta) {
                arrayType = ta;
            }
            else if (arrayIndexExpr.array.type instanceof Type.TypeNullable ptr &&
                    ptr.baseType instanceof Type.TypeArray ta) {
                arrayType = ta;
            }
            else
                throw new CompilerException("Unexpected array type " + arrayIndexExpr.array.type);
            if (!(arrayIndexExpr.expr.type instanceof Type.TypeInteger))
                throw new CompilerException("Array index must be integer type");
            arrayIndexExpr.type = arrayType.getElementType();
            validType(arrayIndexExpr.type, false);
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.ArrayStoreExpr arrayIndexExpr, boolean enter) {
        if (!enter) {
            validType(arrayIndexExpr.array.type, false);
            Type.TypeArray arrayType = null;
            if (arrayIndexExpr.array.type instanceof Type.TypeArray ta) {
                arrayType = ta;
            }
            else if (arrayIndexExpr.array.type instanceof Type.TypeNullable ptr &&
                    ptr.baseType instanceof Type.TypeArray ta) {
                arrayType = ta;
            }
            else
                throw new CompilerException("Unexpected array type " + arrayIndexExpr.array.type);
            if (!(arrayIndexExpr.expr.type instanceof Type.TypeInteger))
                throw new CompilerException("Array index must be integer type");
            arrayIndexExpr.type = arrayType.getElementType();
            validType(arrayIndexExpr.type, false);
            validType(arrayIndexExpr.value.type, true);
            checkAssignmentCompatible(arrayIndexExpr.type, arrayIndexExpr.value.type);
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.NewExpr newExpr, boolean enter) {
        if (enter)
            return this;
        if (newExpr.typeExpr.type == null)
            throw new CompilerException("Unresolved type in new expression");
        validType(newExpr.typeExpr.type, false);
        if (newExpr.typeExpr.type instanceof Type.TypeNullable)
            throw new CompilerException("new cannot be used to create a Nullable type");
        if (newExpr.typeExpr.type instanceof Type.TypeStruct typeStruct) {
            newExpr.type = newExpr.typeExpr.type;
        }
        else if (newExpr.typeExpr.type instanceof Type.TypeArray arrayType) {
            newExpr.type = newExpr.typeExpr.type;
        }
        else
            throw new CompilerException("Unsupported type in new expression");
        return this;
    }

    @Override
    public ASTVisitor visit(AST.InitExpr initExpr, boolean enter) {
        if (enter)
            return this;
        if (initExpr.newExpr.type == null)
            throw new CompilerException("Unresolved type in new expression");
        validType(initExpr.newExpr.type, false);
        if (initExpr.newExpr.type instanceof Type.TypeNullable)
            throw new CompilerException("new cannot be used to create a Nullable type");
        if (initExpr.newExpr.type instanceof Type.TypeStruct typeStruct) {
            for (AST.Expr expr: initExpr.initExprList) {
                if (expr instanceof AST.SetFieldExpr setFieldExpr) {
                    var fieldType = typeStruct.getField(setFieldExpr.fieldName);
                    checkAssignmentCompatible(fieldType, setFieldExpr.value.type);
                }
            }
        }
        else if (initExpr.newExpr.type instanceof Type.TypeArray arrayType) {
            for (AST.Expr expr: initExpr.initExprList) {
                checkAssignmentCompatible(arrayType.getElementType(), expr.type);
            }
        }
        else
            throw new CompilerException("Unsupported type in new expression");
        initExpr.type = initExpr.newExpr.type;
        return this;
    }

    @Override
    public ASTVisitor visit(AST.NameExpr nameExpr, boolean enter) {
        if (!enter)
            return this;
        var symbol = currentScope.lookup(nameExpr.name);
        if (symbol == null) {
            throw new CompilerException("Unknown symbol " + nameExpr.name);
        }
        validType(symbol.type, false);
        nameExpr.symbol = symbol;
        nameExpr.type = symbol.type;
        return this;
    }

    @Override
    public ASTVisitor visit(AST.BreakStmt breakStmt, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.ContinueStmt continueStmt, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.ReturnStmt returnStmt, boolean enter) {
        if (enter)
            return this;
        Type.TypeFunction functionType = (Type.TypeFunction) currentFuncDecl.symbol.type;
        if (returnStmt.expr != null) {
            validType(returnStmt.expr.type, true);
            checkAssignmentCompatible(functionType.returnType, returnStmt.expr.type);
        }
        else if (!(functionType.returnType instanceof Type.TypeVoid)) {
            throw new CompilerException("A return value of type " + functionType.returnType + " is expected");
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.IfElseStmt ifElseStmt, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.WhileStmt whileStmt, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.VarStmt varStmt, boolean enter) {
        if (!enter) {
            validType(varStmt.expr.type, true);
            var symbol = currentScope.lookup(varStmt.varName);
            symbol.type = typeDictionary.merge(varStmt.expr.type, symbol.type);
            if (symbol.type == typeDictionary.NULL)
                throw new CompilerException("Variable " + varStmt.varName + " cannot be Null type");
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.BlockStmt blockStmt, boolean enter) {
        if (enter) {
            currentScope = blockStmt.scope;
        }
        else {
            currentScope = currentScope.parent;
        }
        return this;
    }

    @Override
    public ASTVisitor visit(AST.VarDeclStmt varDeclStmt, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.ExprStmt exprStmt, boolean enter) {
        return this;
    }

    @Override
    public ASTVisitor visit(AST.AssignStmt assignStmt, boolean enter) {
        if (!enter) {
            validType(assignStmt.lhs.type, false);
            validType(assignStmt.rhs.type, true);
            checkAssignmentCompatible(assignStmt.lhs.type, assignStmt.rhs.type);
        }
        return this;
    }

    public void analyze(AST.Program program) {
        program.accept(this);
    }

    private void validType(Type t, boolean allowNull) {
        if (t == null)
            throw new CompilerException("Undefined type");
        if (t == typeDictionary.UNKNOWN)
            throw new CompilerException("Undefined type");
        if (!allowNull && t == typeDictionary.NULL)
            throw new CompilerException("Null type not allowed");
    }

    private void checkAssignmentCompatible(Type var, Type value) {
        if (!var.isAssignable(value))
            throw new CompilerException("Value of type " + value + " cannot be assigned to type " + var);
    }
}
