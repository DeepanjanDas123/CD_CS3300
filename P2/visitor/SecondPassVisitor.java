package visitor;

import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
/**
 * SecondPassVisitor performs type checking:
 * - Validates variable usage (fields, locals, parameters)
 * - Validates method calls (arguments and return types)
 * - Validates assignment compatibility
 * - Checks return types of methods
 * - Checks expressions for type correctness
 */
public class SecondPassVisitor extends GJDepthFirst<String, Void> {

    private SymbolTable st;

    public SecondPassVisitor(SymbolTable st) {
        this.st = st;
    }

    /* ---------------- Main class ---------------- */
    @Override
    public String visit(MainClass n, Void argu) {
        st.currentClass = n.f1.f0.toString();
        st.currentMethod = "main"; // pseudo-method
        n.f14.accept(this, argu);  // PrintStatement
        st.currentMethod = null;
        st.currentClass = null;
        return null;
    }

    /* ---------------- Class Declaration ---------------- */
    @Override
    public String visit(ClassDeclaration n, Void argu) {
        st.currentClass = n.f1.f0.toString();

        // visit fields
        n.f3.accept(this, argu);

        // visit methods
        n.f4.accept(this, argu);

        st.currentClass = null;
        return null;
    }

    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) {
        st.currentClass = n.f1.f0.toString();

        n.f5.accept(this, argu); // fields
        n.f6.accept(this, argu); // methods

        st.currentClass = null;
        return null;
    }

    /* ---------------- Method Declaration ---------------- */
    @Override
    public String visit(MethodDeclaration n, Void argu) {
        st.currentMethod = n.f2.f0.toString();

        // Get the MethodInfo
        MethodInfo method = st.lookupMethod(st.currentMethod);
        if (method == null) {
            ErrorHandler.report("Symbol not found");
        }

        // Visit local variable declarations
        n.f7.accept(this, argu);

        // Visit statements
        n.f8.accept(this, argu);

        // Check return type
        String retType;
        if (n.f10 != null && n.f10.f0 != null && n.f10.f0.choice instanceof LambdaExpression
            && TypeUtils.isLambda(method.returnType)) {
            // Return is a lambda literal and the method expects a Function -> do contextual check
            LambdaExpression lambda = (LambdaExpression) n.f10.f0.choice;
            retType = checkLambdaWithExpected(lambda, method.returnType);
        } else {
            // Normal path (non-lambda return or no expected function type)
            retType = n.f10.accept(this, argu);
        }

        if (!TypeUtils.isAssignable(method.returnType, retType, st)) {
            ErrorHandler.report("Type error");
        }

        st.currentMethod = null;
        return null;
    }

    /* ---------------- Variable Declarations ---------------- */
    @Override
    public String visit(VarDeclaration n, Void argu) {
        String type = n.f0.accept(this, argu);
        String name = n.f1.f0.toString();

        if (!TypeUtils.isValidType(type, st)) {
            ErrorHandler.report("Symbol not found");
        }

        return null;
    }

    /* ---------------- Type ---------------- */
    @Override
    public String visit(Type n, Void argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(IntegerType n, Void argu) { return "int"; }

    @Override
    public String visit(BooleanType n, Void argu) { return "boolean"; }

    @Override
    public String visit(ArrayType n, Void argu) { return "int[]"; }

    @Override
    public String visit(LambdaType n, Void argu) {
        return "Function<" + n.f2.f0.toString() + "," + n.f4.f0.toString() + ">";
    }

    /* ---------------- Statements ---------------- */
    @Override
    public String visit(PrintStatement n, Void argu) {
        String type = n.f2.accept(this, argu);
        if (!TypeUtils.equals(type, "int")) {
            ErrorHandler.report("Type error");
        }
        return null;
    }

    @Override
    public String visit(AssignmentStatement n, Void argu) {
        String varName = n.f0.f0.toString();
        String varType = st.lookupVar(varName);
        if (varType == null) {
            ErrorHandler.report("Symbol not found");
        }

        if (n.f2 != null && n.f2.f0 != null && n.f2.f0.choice instanceof LambdaExpression) {
            LambdaExpression lambda = (LambdaExpression) n.f2.f0.choice;
            String lambdaType = checkLambdaWithExpected(lambda, varType);
            if (!TypeUtils.isAssignable(varType, lambdaType, st)) {
                ErrorHandler.report("Type error");
            }
            return null;
        }

        String exprType = n.f2.accept(this, argu);
        if (!TypeUtils.isAssignable(varType, exprType, st)) {
            ErrorHandler.report("Type error");
        }
        return null;
    }

    @Override
    public String visit(ArrayAssignmentStatement n, Void argu) {
        String varName = n.f0.f0.toString();
        String varType = st.lookupVar(varName);
        if (varType == null) {
            ErrorHandler.report("Symbol not found");
        }
        if (!TypeUtils.isArray(varType)) {
            ErrorHandler.report("Type error");
        }

        String indexType = n.f2.accept(this, argu);
        if (!TypeUtils.equals(indexType, "int")) {
            ErrorHandler.report("Type error");
        }

        String exprType = n.f5.accept(this, argu);
        if (!TypeUtils.isAssignable(TypeUtils.arrayBase(varType), exprType, st)) {
            ErrorHandler.report("Type error");
        }
        return null;
    }

    @Override
    public String visit(Block n, Void argu) {
        n.f1.accept(this, argu);
        return null;
    }

    @Override
    public String visit(IfthenStatement n, Void argu) {
        String condType = n.f2.accept(this, argu);
        if (!TypeUtils.equals(condType, "boolean")) {
            ErrorHandler.report("Type error");
        }
        n.f4.accept(this, argu);
        return null;
    }

    @Override
    public String visit(IfthenElseStatement n, Void argu) {
        String condType = n.f2.accept(this, argu);
        if (!TypeUtils.equals(condType, "boolean")) {
            ErrorHandler.report("Type error");
        }
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
        return null;
    }

    @Override
    public String visit(WhileStatement n, Void argu) {
        String condType = n.f2.accept(this, argu);
        if (!TypeUtils.equals(condType, "boolean")) {
            ErrorHandler.report("Type error");
        }
        n.f4.accept(this, argu);
        return null;
    }

    /* ---------------- Expressions ---------------- */
    @Override
    public String visit(IntegerLiteral n, Void argu) { return "int"; }

    @Override
    public String visit(TrueLiteral n, Void argu) { return "boolean"; }

    @Override
    public String visit(FalseLiteral n, Void argu) { return "boolean"; }

    @Override
    public String visit(ThisExpression n, Void argu) { return st.currentClass; }

    @Override
    public String visit(Identifier n, Void argu) {
        String name = n.f0.toString();
        String varType = st.lookupVar(name);
        if (varType != null) return varType;

        if (st.getClass(name) != null) return name;

        ErrorHandler.report("Symbol not found");
        return "error";
    }

    @Override
    public String visit(AddExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, "int") || !TypeUtils.equals(t2, "int")) {
            ErrorHandler.report("Type error");
        }
        return "int";
    }

    @Override
    public String visit(MinusExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, "int") || !TypeUtils.equals(t2, "int")) {
            ErrorHandler.report("Type error");
        }
        return "int";
    }

    @Override
    public String visit(TimesExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, "int") || !TypeUtils.equals(t2, "int")) {
            ErrorHandler.report("Type error");
        }
        return "int";
    }

    @Override
    public String visit(DivExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, "int") || !TypeUtils.equals(t2, "int")) {
            ErrorHandler.report("Type error");
        }
        return "int";
    }

    @Override
    public String visit(AndExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, "boolean") || !TypeUtils.equals(t2, "boolean")) {
            ErrorHandler.report("Type error");
        }
        return "boolean";
    }

    @Override
    public String visit(OrExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, "boolean") || !TypeUtils.equals(t2, "boolean")) {
            ErrorHandler.report("Type error");
        }
        return "boolean";
    }

    @Override
    public String visit(CompareExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, "int") || !TypeUtils.equals(t2, "int")) {
            ErrorHandler.report("Type error");
        }
        return "boolean";
    }

    @Override
    public String visit(neqExpression n, Void argu) {
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!TypeUtils.equals(t1, t2)) {
            ErrorHandler.report("Type error");
        }
        return "boolean";
    }

    @Override
    public String visit(ArrayLookup n, Void argu) {
        String arrType = n.f0.accept(this, argu);
        String indexType = n.f2.accept(this, argu);
        if (!TypeUtils.isArray(arrType)) {
            ErrorHandler.report("Type error");
        }
        if (!TypeUtils.equals(indexType, "int")) {
            ErrorHandler.report("Type error");
        }
        return TypeUtils.arrayBase(arrType);
    }

    @Override
    public String visit(ArrayLength n, Void argu) {
        String arrType = n.f0.accept(this, argu);
        if (!TypeUtils.isArray(arrType)) {
            ErrorHandler.report("Type error");
        }
        return "int";
    }

    /**
     * Build argument types list from optional ExpressionList.
     * This avoids fragile CSV splitting (which breaks when argument types include commas,
     * e.g. Function<X,Y>).
     */
    private List<String> getArgTypes(NodeOptional f4Node, Void argu) {
        List<String> argTypes = new ArrayList<>();
        if (f4Node.present()) {
            ExpressionList el = (ExpressionList) f4Node.node;
            // first expression
            argTypes.add(el.f0.accept(this, argu));
            // rest
            for (Enumeration<Node> e = el.f1.elements(); e.hasMoreElements();) {
                Node node = e.nextElement();
                if (node instanceof ExpressionRest) {
                    ExpressionRest er = (ExpressionRest) node;
                    argTypes.add(er.f1.accept(this, argu));
                } else {
                    ErrorHandler.report("Type error");
                }
            }
        }
        return argTypes;
    }

    @Override
    public String visit(MessageSend n, Void argu) {
        // Get the type of the object on which method is called
        String objType = n.f0.accept(this, argu);
        String methodName = n.f2.f0.toString();

        // --- handle Function<T,R>.apply() ---
        if (TypeUtils.isLambda(objType)) {
            if (!st.hasFunctionImport) {
                ErrorHandler.report("Symbol not found");
            }
            if (!methodName.equals("apply")) {
                ErrorHandler.report("Symbol not found");
            }

            TypeUtils.LambdaType fn = TypeUtils.parseLambda(objType);

            // Build argument list using AST and handle lambda args contextually
            List<String> args = new ArrayList<>();
            if (n.f4.present()) {
                ExpressionList el = (ExpressionList) n.f4.node;
                // first expression
                if (el.f0 != null && el.f0.f0 != null && el.f0.f0.choice instanceof LambdaExpression) {
                    String lambdaType = checkLambdaWithExpected((LambdaExpression) el.f0.f0.choice, fn.argType);
                    args.add(lambdaType);
                } else {
                    args.add(el.f0.accept(this, argu));
                }
                // rest
                for (Enumeration<Node> e = el.f1.elements(); e.hasMoreElements();) {
                    ExpressionRest er = (ExpressionRest) e.nextElement();
                    if (er.f1 != null && er.f1.f0 != null && er.f1.f0.choice instanceof LambdaExpression) {
                        String lambdaType = checkLambdaWithExpected((LambdaExpression) er.f1.f0.choice, fn.argType);
                        args.add(lambdaType);
                    } else {
                        args.add(er.f1.accept(this, argu));
                    }
                }
            }

            if (args.size() != 1 || !TypeUtils.isAssignable(fn.argType, args.get(0), st)) {
                ErrorHandler.report("Type error");
            }
            return fn.returnType;
        }

        // --- normal class method lookup ---
        if (TypeUtils.isPrimitive(objType)) {
            ErrorHandler.report("Type error");
        }

        ClassInfo cInfo = st.getClass(objType);
        if (cInfo == null) {
            ErrorHandler.report("Symbol not found");
        }

        MethodInfo mInfo = cInfo.lookupMethod(methodName, st);
        if (mInfo == null) {
            ErrorHandler.report("Symbol not found");
        }

        // Build argument type list using AST, with contextual lambda checks when param is a function
        List<String> argTypes = new ArrayList<>();
        List<String> paramTypes = mInfo.getParamTypes();

        if (n.f4.present()) {
            ExpressionList el = (ExpressionList) n.f4.node;
            int idx = 0;
            // first
            if (el.f0 != null && el.f0.f0 != null && el.f0.f0.choice instanceof LambdaExpression) {
                String expected = (idx < paramTypes.size()) ? paramTypes.get(idx) : null;
                argTypes.add(checkLambdaWithExpected((LambdaExpression) el.f0.f0.choice, expected));
            } else {
                argTypes.add(el.f0.accept(this, argu));
            }
            idx++;
            // rest
            for (Enumeration<Node> e = el.f1.elements(); e.hasMoreElements();) {
                ExpressionRest er = (ExpressionRest) e.nextElement();
                if (er.f1 != null && er.f1.f0 != null && er.f1.f0.choice instanceof LambdaExpression) {
                    String expected = (idx < paramTypes.size()) ? paramTypes.get(idx) : null;
                    argTypes.add(checkLambdaWithExpected((LambdaExpression) er.f1.f0.choice, expected));
                } else {
                    argTypes.add(er.f1.accept(this, argu));
                }
                idx++;
            }
        }

        // Check argument count
        if (argTypes.size() != paramTypes.size()) {
            ErrorHandler.report("Type error");
        }

        // Check each argument type
        for (int i = 0; i < argTypes.size(); i++) {
            if (!TypeUtils.isAssignable(paramTypes.get(i), argTypes.get(i), st)) {
                ErrorHandler.report("Type error");
            }
        }

        return mInfo.returnType;
    }



    @Override
    public String visit(ExpressionList n, Void argu) {
        String firstType = n.f0.accept(this, argu); // type of the first expression
        StringBuilder sb = new StringBuilder(firstType);

        for (Node node : n.f1.nodes) {
            ExpressionRest er = (ExpressionRest) node;
            sb.append(",");
            sb.append(er.f1.accept(this, argu));
        }

        return sb.toString();
    }

    @Override
    public String visit(ExpressionRest n, Void argu) {
        return n.f1.accept(this, argu); // just the type of this expression
    }

    @Override
    public String visit(LambdaExpression n, Void argu) {
        String argType = n.f1.f0.toString();
        String retType = n.f4.accept(this, argu);
        return "Function<" + argType + "," + retType + ">";
    }

    @Override
    public String visit(NotExpression n, Void argu) {
        String type = n.f1.accept(this, argu);
        if (!TypeUtils.equals(type, "boolean")) {
            ErrorHandler.report("Type error");
        }
        return "boolean";
    }

    @Override
    public String visit(BracketExpression n, Void argu) {
        return n.f1.accept(this, argu);
    }

    @Override
    public String visit(AllocationExpression n, Void argu) {
        String className = n.f1.f0.toString();
        if (st.getClass(className) == null) {
            ErrorHandler.report("Symbol not found");
            return "error";
        }
        return className;
    }

    @Override
    public String visit(PrimaryExpression n, Void argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(Expression n, Void argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ArrayAllocationExpression n, Void argu) {
        String sizeType = n.f3.accept(this, argu);
        if (!TypeUtils.equals(sizeType, "int")) {
            ErrorHandler.report("Type error");
        }
        return "int[]";
    }

    private String checkLambdaWithExpected(LambdaExpression lambda, String expectedFuncType) {
        // Parse expected if available
        String expectedArg = null;
        String expectedRet = null;
        if (expectedFuncType != null && TypeUtils.isLambda(expectedFuncType)) {
            TypeUtils.LambdaType exp = TypeUtils.parseLambda(expectedFuncType);
            expectedArg = exp.argType;   // may be null or "?"
            expectedRet = exp.returnType;
        }

        // param name from AST
        String paramName = lambda.f1.f0.toString();

        // Find current method info
        MethodInfo method = null;
        if (st.currentMethod != null) method = st.lookupMethod(st.currentMethod);

        // Save previous binding (if any) so we can restore it
        String previous = null;
        if (method != null) previous = method.locals.get(paramName);

        // Temporarily bind paramName -> expectedArg (may be null)
        if (method != null) {
            method.locals.put(paramName, expectedArg);
        }

        String bodyType;
        try {
            // Type-check body under this temporary binding
            bodyType = lambda.f4.accept(this, null);

            // If expected return present, ensure bodyType is assignable to it
            if (expectedRet != null) {
                if (!TypeUtils.isAssignable(expectedRet, bodyType, st)) {
                    ErrorHandler.report("Type error");
                }
                // resolved function type uses expectedArg (or ?)
                String resolvedArg = (expectedArg == null) ? "?" : expectedArg;
                return "Function<" + resolvedArg + "," + bodyType + ">";
            } else {
                // No expected function type: return wildcard arg and discovered return
                String argRep = (expectedArg == null) ? "?" : expectedArg;
                return "Function<" + argRep + "," + bodyType + ">";
            }
        } finally {
            // Restore previous binding (or remove)
            if (method != null) {
                if (previous == null) method.locals.remove(paramName);
                else method.locals.put(paramName, previous);
            }
        }
    }


    // @Override
    // public String visit(NodeToken n, Void argu) {
    //     return n.toString();
    // }

}
