import java.util.*;
import syntaxtree.*;
import visitor.*;


public class MJIRGenerator extends GJDepthFirst<Void, Env> {

    private final Map<String, ClassInfo> classTable;
    private final IR.Program program = new IR.Program();
    private String currentClassName = null;
    // Unique lambda counter for generating lambda procedure labels
    private int lambdaCounter = 0;

    // put next to other fields in MJIRGenerator
    private IR.Procedure curProc = null;


    private List<IR.Procedure> deferredProcedures = new ArrayList<>();


    public MJIRGenerator(Map<String, ClassInfo> classTable) {
        // Make a mutable copy of the class table (incoming map may be unmodifiable)
        this.classTable = new LinkedHashMap<>();
        if (classTable != null) this.classTable.putAll(classTable);
        ensureFunctionClass();
    }

    public IR.Program getProgram() { return program; }

    public void finalizeProcedures() {
        // Append any deferred procedures to the program list in stable order
        for (IR.Procedure p : deferredProcedures) {
            program.procedures.add(p);
        }
        deferredProcedures.clear();
    }

    /* ---- Top-level visitors ---- */

    @Override
    public Void visit(Goal n, Env argu) {
        n.f0.accept(this, argu); // optional import
        n.f1.accept(this, argu); // main class
        n.f2.accept(this, argu); // other class declarations
        return null;
    }

    @Override
    public Void visit(ImportFunction n, Env argu) { return null; }

    @Override
    public Void visit(MainClass n, Env argu) {
        String mainArg = n.f11.f0.toString();
        Env menv = new Env("Main", "main", 2);
        menv.addVar(mainArg, 1, "String[]");
        // The PrintStatement is a child; let the visitor dispatch handle it.
        n.f14.accept(this, menv);
        return null;
    }

    @Override
    public Void visit(ClassDeclaration n, Env argu) {
        String old = currentClassName;
        currentClassName = n.f1.f0.toString();
        n.f3.accept(this, argu); // var decls
        n.f4.accept(this, argu); // method decls
        currentClassName = old;
        return null;
    }

    @Override
    public Void visit(ClassExtendsDeclaration n, Env argu) {
        String old = currentClassName;
        currentClassName = n.f1.f0.toString();
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        currentClassName = old;
        return null;
    }

    /* ---- Method translation ---- */

    @Override
    public Void visit(MethodDeclaration n, Env argu) {
        // method name
        String methodName = n.f2.f0.toString();

        // lookup argCount via ClassInfo/MethodInfo  (your existing code)
        ClassInfo ci = classTable.get(currentClassName);
        MethodInfo mi = null;
        if (ci != null) {
            mi = ci.getMethodSlots().get(methodName);
            if (mi == null) {
                for (MethodInfo m : ci.getDeclaredMethods())
                    if (m.getName().equals(methodName)) { mi = m; break; }
            }
        }
        int argCount = (mi != null) ? mi.getArgCount() : 1;

        // create procedure and add to program
        IR.Procedure proc = new IR.Procedure(ClassInfo.methodLabel(currentClassName, methodName), argCount);
        program.procedures.add(proc);

        // --- Set curProc so all subsequent statement/expr emission goes into proc.body ---
        IR.Procedure savedProc = this.curProc;
        this.curProc = proc;

        try {
            // create environment (temps start at argCount)
            Env menv = new Env(currentClassName, methodName, argCount);
            // map 'this' -> TEMP 0
            menv.addVar("this", 0, currentClassName);

            // map formal parameters (if any)  (keep your existing logic)
            if (n.f4.present()) {
                FormalParameterList fpl = (FormalParameterList) n.f4.node;
                int ptemp = 1;
                FormalParameter fp0 = fpl.f0;
                menv.addVar(fp0.f1.f0.toString(), ptemp, typeToString(fp0.f0));
                ptemp++;
                NodeListOptional rest = fpl.f1;
                for (Enumeration<Node> e = rest.elements(); e.hasMoreElements();) {
                    FormalParameterRest fpr = (FormalParameterRest)e.nextElement();
                    FormalParameter fp = fpr.f1;
                    menv.addVar(fp.f1.f0.toString(), ptemp, typeToString(fp.f0));
                    ptemp++;
                }
            }

            // local variable declarations: allocate temps and record types
            if (n.f7 != null) {
                for (Enumeration<Node> e = n.f7.elements(); e.hasMoreElements();) {
                    VarDeclaration vd = (VarDeclaration) e.nextElement();
                    String lname = vd.f1.f0.toString();
                    String ltype = typeToString(vd.f0);
                    int t = menv.allocLocal();
                    menv.addVar(lname, t, ltype);
                }
            }

            // translate statements: use accept so the visit(Statement...) methods run
            if (n.f8 != null) {
                for (Enumeration<Node> e = n.f8.elements(); e.hasMoreElements();) {
                    Node stmt = e.nextElement();
                    stmt.accept(this, menv);
                }
            }

            // return expression
            IR.Exp ret = translateExpression(n.f10, menv);
            int retTemp = menv.allocLocal();
            proc.body.add(new IR.MoveStmt(retTemp, ret));
            proc.body.add(new IR.MoveStmt(0, new IR.TempExp(retTemp)));
        } finally {
            // restore old curProc (important)
            this.curProc = savedProc;
        }
        return null;
    }


    /* ---- Statement visitors ----
       Each statement visitor appends IR to the current procedure (or program.mainStmts when no procedure).
    */

    @Override
    public Void visit(PrintStatement n, Env env) {
        IR.Exp e = translateExpression(n.f2, env);
        IR.PrintStmt ps = new IR.PrintStmt(e);
        IR.Procedure cur = currentProcedure();
        if (cur == null) program.mainStmts.add(ps); else cur.body.add(ps);
        return null;
    }

    @Override
    public Void visit(AssignmentStatement n, Env env) {
        // Identifier = Expression ;
        String id = n.f0.f0.toString();
        IR.Exp rhs = translateExpression(n.f2, env);
        Integer t = env.lookup(id);
        IR.Procedure cur = currentProcedure();
        if (t != null) {
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(t, rhs));
            else cur.body.add(new IR.MoveStmt(t, rhs));
        } else {
            // field assignment to this.id
            FieldInfo fi = lookupFieldInClassHierarchy(env.currentClassName, id);
            if (fi == null) {
                // fallback: create local
                int nt = env.allocLocal();
                env.addVar(id, nt, "int");
                if (cur == null) program.mainStmts.add(new IR.MoveStmt(nt, rhs));
                else cur.body.add(new IR.MoveStmt(nt, rhs));
            } else {
                if (cur == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(0), fi.getOffset(), rhs));
                else cur.body.add(new IR.HStoreStmt(new IR.TempExp(0), fi.getOffset(), rhs));
            }
        }
        return null;
    }

    @Override
    public Void visit(ArrayAssignmentStatement n, Env env) {
        // Identifier [ Expression ] = Expression ;
        String arrName = n.f0.f0.toString();
        IR.Exp idx = translateExpression(n.f2, env);
        IR.Exp val = translateExpression(n.f5, env);

        IR.Procedure cur = currentProcedure();

        IR.Exp base = varToAddrExp(arrName, env, cur);
        IR.Exp idxTimes = new IR.BinOpExp("TIMES", ensureSimple(idx, env), new IR.IntegerExp(4));
        IR.Exp offsetPlus = new IR.BinOpExp("PLUS", idxTimes, new IR.IntegerExp(4));
        IR.Exp elemAddr = new IR.BinOpExp("PLUS", ensureSimple(base, env), ensureSimple(offsetPlus, env));

        int addrTemp = env.allocLocal();
        if (cur == null) program.mainStmts.add(new IR.MoveStmt(addrTemp, elemAddr));
        else cur.body.add(new IR.MoveStmt(addrTemp, elemAddr));

        if (cur == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(addrTemp), 0, val));
        else cur.body.add(new IR.HStoreStmt(new IR.TempExp(addrTemp), 0, val));

        return null;
    }

    // If the grammar-level wrapper still exists, keep it but delegate to the concrete node:
    @Override
    public Void visit(IfStatement n, Env env) {
        // delegate to the concrete choice node (either IfthenStatement or IfthenElseStatement)
        Node choice = n.f0.choice;
        return choice.accept(this, env);
    }


    private IR.Exp normalizeCond(IR.Exp rawCond, Env env) {
        // If rawCond is already a Temp or simple, we still wrap it in NE <rawCond> 0.
        IR.Exp simple = ensureSimple(rawCond, env);
        return new IR.BinOpExp("NE", simple, new IR.IntegerExp(0));
    }

    @Override
    public Void visit(IfthenElseStatement n, Env env) {
        // n.f2 is the condition Expression, n.f4 is then-stmt, n.f6 is else-stmt
        IR.Exp cond = translateExpression(n.f2, env);
        IR.Exp normalized = normalizeCond(cond, env);

        String elseL = LabelGen.newLabel("Lelse");
        String endL  = LabelGen.newLabel("Lend");
        IR.Procedure cur = currentProcedure();

        // CJUMP normalized elseL  -- jump to else when condition is false (i.e., normalized == 0)
        if (cur == null) program.mainStmts.add(new IR.CJumpStmt(normalized, elseL));
        else cur.body.add(new IR.CJumpStmt(normalized, elseL));

        // then branch
        n.f4.accept(this, env);

        // after then, jump to end
        if (cur == null) program.mainStmts.add(new IR.JumpStmt(endL));
        else cur.body.add(new IR.JumpStmt(endL));

        // else label and else branch
        if (cur == null) program.mainStmts.add(new IR.LabelStmt(elseL));
        else cur.body.add(new IR.LabelStmt(elseL));
        n.f6.accept(this, env);

        // end label
        if (cur == null) program.mainStmts.add(new IR.LabelStmt(endL));
        else cur.body.add(new IR.LabelStmt(endL));

        return null;
    }

    @Override
    public Void visit(IfthenStatement n, Env env) {
        // n.f2 is condition, n.f4 is the then-statement
        IR.Exp cond = translateExpression(n.f2, env);
        IR.Exp normalized = normalizeCond(cond, env);

        String endL = LabelGen.newLabel("Lend");
        IR.Procedure cur = currentProcedure();

        // CJUMP normalized endL -- if condition false jump to end (i.e., skip then)
        if (cur == null) program.mainStmts.add(new IR.CJumpStmt(normalized, endL));
        else cur.body.add(new IR.CJumpStmt(normalized, endL));

        // then branch
        n.f4.accept(this, env);

        // end label
        if (cur == null) program.mainStmts.add(new IR.LabelStmt(endL));
        else cur.body.add(new IR.LabelStmt(endL));

        return null;
    }


    @Override
    public Void visit(WhileStatement n, Env env) {
        // labels
        String startL = LabelGen.newLabel("Lstart");
        String endL   = LabelGen.newLabel("Lend");
        IR.Procedure cur = currentProcedure();

        // start label
        if (cur == null) program.mainStmts.add(new IR.LabelStmt(startL));
        else cur.body.add(new IR.LabelStmt(startL));

        // cond
        IR.Exp cond = translateExpression(n.f2, env);
        IR.Exp normalized = normalizeCond(cond, env);

        // if condition false, jump to end
        if (cur == null) program.mainStmts.add(new IR.CJumpStmt(normalized, endL));
        else cur.body.add(new IR.CJumpStmt(normalized, endL));

        // body
        n.f4.accept(this, env);

        // jump back to start
        if (cur == null) program.mainStmts.add(new IR.JumpStmt(startL));
        else cur.body.add(new IR.JumpStmt(startL));

        // end label
        if (cur == null) program.mainStmts.add(new IR.LabelStmt(endL));
        else cur.body.add(new IR.LabelStmt(endL));

        return null;
    }



    @Override
    public Void visit(Block n, Env env) {
        if (n.f1 != null) {
            for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements();) {
                Node s = e.nextElement();
                s.accept(this, env);
            }
        }
        return null;
    }

    /* ---- Expression helpers and translation ---- */

    private IR.Procedure currentProcedure() {
        // Prefer explicit curProc if set
        if (this.curProc != null) return this.curProc;
        // Fallback: last procedure in program.procedures (if any)
        if (program == null) return null;
        if (program.procedures == null || program.procedures.size() == 0) return null;
        return program.procedures.get(program.procedures.size() - 1);
    }


    private IR.Exp translateExpression(Node exprNode, Env env) {
        if (exprNode == null) return new IR.IntegerExp(0);

        // DEBUG: show which AST node we are translating
        String nodeClass = exprNode.getClass().getSimpleName();
        String nodeShort;
        try {
            nodeShort = exprNode.toString().replace("\n"," ").trim();
            if (nodeShort.length() > 80) nodeShort = nodeShort.substring(0,80) + "...";
        } catch (Throwable t) {
            nodeShort = "<toString failed>";
        }
        //println("[P3][TRACE] translateExpression: nodeClass=" + nodeClass + " snippet=\"" + nodeShort + "\"");


        // Expression wrapper
        if (exprNode instanceof Expression) {
            return translateExpression(((Expression)exprNode).f0.choice, env);
        }

        // PrimaryExpression wrapper: unwrap and handle the normal choices (identifier, literal, this, allocation, bracket, etc.)
        if (exprNode instanceof PrimaryExpression) {
            Node choice = ((PrimaryExpression) exprNode).f0.choice;

            // Identifier: try local temp first, then field (this.x), else fallback to 0
            if (choice instanceof Identifier) {
                String id = ((Identifier) choice).f0.toString();
                Integer t = (env == null) ? null : env.lookup(id);
                if (t != null) return new IR.TempExp(t);

                if (env != null && env.currentClassName != null) {
                    FieldInfo finfo = lookupFieldInClassHierarchy(env.currentClassName, id);
                    if (finfo != null) {
                        int tmp = env.allocLocal();
                        IR.Procedure cur = currentProcedure();
                        if (cur == null) program.mainStmts.add(new IR.HLoadStmt(tmp, new IR.TempExp(0), finfo.getOffset()));
                        else cur.body.add(new IR.HLoadStmt(tmp, new IR.TempExp(0), finfo.getOffset()));
                        return new IR.TempExp(tmp);
                    }
                }

                //println("[P3][WARN] Unresolved identifier '" + id + "'. Returning 0.");
                return new IR.IntegerExp(0);
            }

            // Integer literal
            if (choice instanceof IntegerLiteral) {
                String s = ((IntegerLiteral) choice).f0.toString();
                try {
                    int v = Integer.parseInt(s);
                    return new IR.IntegerExp(v);
                } catch (NumberFormatException ex) {
                    //println("[P3][WARN] Bad integer literal: '" + s + "'. Using 0.");
                    return new IR.IntegerExp(0);
                }
            }

            // true / false
            if (choice instanceof TrueLiteral) return new IR.IntegerExp(1);
            if (choice instanceof FalseLiteral) return new IR.IntegerExp(0);

            // this
            if (choice instanceof ThisExpression) {
                return new IR.TempExp(0); // 'this' is TEMP 0 by convention
            }

            // Array allocation expression (new int[expr])
            if (choice instanceof ArrayAllocationExpression) {
                return translateExpression(choice, env);
            }

            // Object allocation expression (new Class())
            if (choice instanceof AllocationExpression) {
                return translateExpression(choice, env);
            }

            // Not expression: "!" Expr
            if (choice instanceof NotExpression) {
                return translateExpression(choice, env);
            }

            // Bracket expression: "(" Expression ")"
            if (choice instanceof BracketExpression) {
                return translateExpression(choice, env);
            }

            // Fallback: translate underlying node
            return translateExpression(choice, env);
        }



        if (exprNode instanceof BracketExpression) {
            BracketExpression be = (BracketExpression) exprNode;
            MessageSend ms = findMessageSendInSubtree(be.f1);
            if (ms != null) return translateExpression(ms, env);
            return translateExpression(be.f1, env);
        }


        // Literals & simple nodes
        if (exprNode instanceof IntegerLiteral) {
            int v = Integer.parseInt(((IntegerLiteral)exprNode).f0.tokenImage);
            return new IR.IntegerExp(v);
        }
        if (exprNode instanceof TrueLiteral) return new IR.IntegerExp(1);
        if (exprNode instanceof FalseLiteral) return new IR.IntegerExp(0);
        if (exprNode instanceof ThisExpression) return new IR.TempExp(0);

        if (exprNode instanceof Identifier) {
            String name = ((Identifier)exprNode).f0.toString();
            Integer t = env.lookup(name);
            if (t != null) return new IR.TempExp(t);
            // field access on this
            FieldInfo fi = lookupFieldInClassHierarchy(env.currentClassName, name);
            if (fi != null) {
                IR.Procedure cur = currentProcedure();
                int dst = env.allocLocal();
                if (cur == null) program.mainStmts.add(new IR.HLoadStmt(dst, new IR.TempExp(0), fi.getOffset()));
                else cur.body.add(new IR.HLoadStmt(dst, new IR.TempExp(0), fi.getOffset()));
                return new IR.TempExp(dst);
            }
            return new IR.IntegerExp(0);
        }

        // Binary ops and compare (AddExpression etc.)
        if (exprNode instanceof AddExpression) {
            AddExpression ae = (AddExpression) exprNode;
            IR.Exp l = translateExpression(ae.f0, env);
            IR.Exp r = translateExpression(ae.f2, env);
            return new IR.BinOpExp("PLUS", ensureSimple(l, env), ensureSimple(r, env));
        }
        if (exprNode instanceof MinusExpression) {
            MinusExpression me = (MinusExpression) exprNode;
            IR.Exp l = translateExpression(me.f0, env);
            IR.Exp r = translateExpression(me.f2, env);
            return new IR.BinOpExp("MINUS", ensureSimple(l, env), ensureSimple(r, env));
        }
        if (exprNode instanceof TimesExpression) {
            TimesExpression te = (TimesExpression) exprNode;
            IR.Exp l = translateExpression(te.f0, env);
            IR.Exp r = translateExpression(te.f2, env);
            return new IR.BinOpExp("TIMES", ensureSimple(l, env), ensureSimple(r, env));
        }
        if (exprNode instanceof DivExpression) {
            DivExpression de = (DivExpression) exprNode;
            IR.Exp l = translateExpression(de.f0, env);
            IR.Exp r = translateExpression(de.f2, env);
            return new IR.BinOpExp("DIV", ensureSimple(l, env), ensureSimple(r, env));
        }
        if (exprNode instanceof CompareExpression) {
            CompareExpression ce = (CompareExpression) exprNode;
            IR.Exp l = translateExpression(ce.f0, env);
            IR.Exp r = translateExpression(ce.f2, env);
            return new IR.BinOpExp("LE", ensureSimple(l, env), ensureSimple(r, env));
        }
        if (exprNode instanceof neqExpression) {
            neqExpression ne = (neqExpression) exprNode;
            IR.Exp l = translateExpression(ne.f0, env);
            IR.Exp r = translateExpression(ne.f2, env);
            return new IR.BinOpExp("NE", ensureSimple(l, env), ensureSimple(r, env));
        }

        // AllocationExpression -> new Class()
        if (exprNode instanceof AllocationExpression) {
            AllocationExpression ae = (AllocationExpression) exprNode;
            String cname = ae.f1.f0.toString();
            return translateNewObject(cname, env);
        }

        // ArrayAllocationExpression: new int [ Expression ]
        if (exprNode instanceof ArrayAllocationExpression) {
            ArrayAllocationExpression aae = (ArrayAllocationExpression) exprNode;

            // 1) Evaluate length expression and hoist to a temp (nTemp)
            IR.Exp lenRaw = translateExpression(aae.f3, env);
            IR.Exp lenSimple = ensureSimple(lenRaw, env);

            // ensure we have a TEMP holding the length (so we can reuse it)
            int nTemp;
            IR.Procedure cur = currentProcedure();
            if (lenSimple instanceof IR.TempExp) {
                nTemp = ((IR.TempExp) lenSimple).t;
            } else {
                nTemp = env.allocLocal();
                if (cur == null) program.mainStmts.add(new IR.MoveStmt(nTemp, lenSimple));
                else cur.body.add(new IR.MoveStmt(nTemp, lenSimple));
            }

            // 2) compute sizeBytes = n * 4 + 4
            IR.Exp times4 = new IR.BinOpExp("TIMES", new IR.TempExp(nTemp), new IR.IntegerExp(4));
            IR.Exp sizeBytes = new IR.BinOpExp("PLUS", times4, new IR.IntegerExp(4));

            // 3) allocate array block: arrTemp = HALLOCATE(sizeBytes)
            int arrTemp = env.allocLocal();
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(arrTemp, new IR.HAllocateExp(sizeBytes)));
            else cur.body.add(new IR.MoveStmt(arrTemp, new IR.HAllocateExp(sizeBytes)));

            // 4) store length into header: HSTORE arrTemp 0 nTemp
            if (cur == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(arrTemp), 0, new IR.TempExp(nTemp)));
            else cur.body.add(new IR.HStoreStmt(new IR.TempExp(arrTemp), 0, new IR.TempExp(nTemp)));

            // 5) zero initialize elements:
            //    prepare loop variables and labels
            int iTemp = env.allocLocal();
            int nMinus1 = env.allocLocal(); // n - 1
            String Lstart = newLabel("Larr_start");
            String Lend   = newLabel("Larr_end");

            // i = 0
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(iTemp, new IR.IntegerExp(0)));
            else cur.body.add(new IR.MoveStmt(iTemp, new IR.IntegerExp(0)));

            // nMinus1 = n - 1
            if (cur == null)
                program.mainStmts.add(new IR.MoveStmt(nMinus1, new IR.BinOpExp("MINUS", new IR.TempExp(nTemp), new IR.IntegerExp(1))));
            else
                cur.body.add(new IR.MoveStmt(nMinus1, new IR.BinOpExp("MINUS", new IR.TempExp(nTemp), new IR.IntegerExp(1))));

            // start label
            if (cur == null) program.mainStmts.add(new IR.LabelStmt(Lstart));
            else cur.body.add(new IR.LabelStmt(Lstart));

            // CJUMP LE iTemp nMinus1 Lend
            // Note: CJUMP semantics: if cond==1 -> continue (fall through into body); else -> jump to Lend.
            if (cur == null)
                program.mainStmts.add(new IR.CJumpStmt(new IR.BinOpExp("LE", new IR.TempExp(iTemp), new IR.TempExp(nMinus1)), Lend));
            else
                cur.body.add(new IR.CJumpStmt(new IR.BinOpExp("LE", new IR.TempExp(iTemp), new IR.TempExp(nMinus1)), Lend));

            // --- BODY (falls through from CJUMP when true) ---
            // elemAddr = arrTemp + ((iTemp * 4) + 4)
            int elemAddr = env.allocLocal();
            IR.Exp offsetExpr = new IR.BinOpExp("PLUS",
                    new IR.BinOpExp("TIMES", new IR.TempExp(iTemp), new IR.IntegerExp(4)),
                    new IR.IntegerExp(4));
            if (cur == null)
                program.mainStmts.add(new IR.MoveStmt(elemAddr, new IR.BinOpExp("PLUS", new IR.TempExp(arrTemp), offsetExpr)));
            else
                cur.body.add(new IR.MoveStmt(elemAddr, new IR.BinOpExp("PLUS", new IR.TempExp(arrTemp), offsetExpr)));

            // HSTORE elemAddr 0 0   (store integer 0 into element slot)
            if (cur == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(elemAddr), 0, new IR.IntegerExp(0)));
            else cur.body.add(new IR.HStoreStmt(new IR.TempExp(elemAddr), 0, new IR.IntegerExp(0)));

            // i = i + 1
            if (cur == null)
                program.mainStmts.add(new IR.MoveStmt(iTemp, new IR.BinOpExp("PLUS", new IR.TempExp(iTemp), new IR.IntegerExp(1))));
            else
                cur.body.add(new IR.MoveStmt(iTemp, new IR.BinOpExp("PLUS", new IR.TempExp(iTemp), new IR.IntegerExp(1))));

            // jump back to start
            if (cur == null) program.mainStmts.add(new IR.JumpStmt(Lstart));
            else cur.body.add(new IR.JumpStmt(Lstart));

            // end label
            if (cur == null) program.mainStmts.add(new IR.LabelStmt(Lend));
            else cur.body.add(new IR.LabelStmt(Lend));

            // 6) return array temp
            return new IR.TempExp(arrTemp);
        }

        // NOT: "!" Expression
        if (exprNode instanceof NotExpression) {
            NotExpression ne = (NotExpression) exprNode;
            IR.Exp inner = translateExpression(ne.f1, env);
            // !e  =>  (NE e 0)
            return new IR.BinOpExp("NE", ensureSimple(inner, env), new IR.IntegerExp(0));
        }

        // AND: PrimaryExpression "&&" PrimaryExpression  (short-circuit)
        if (exprNode instanceof AndExpression) {
            AndExpression ae = (AndExpression) exprNode;
            IR.Procedure cur = currentProcedure();
            // evaluate left and right, hoist to temps
            IR.Exp left = ensureSimple(translateExpression(ae.f0, env), env);
            IR.Exp right = ensureSimple(translateExpression(ae.f2, env), env);

            int dst = env.allocLocal();
            String Lright = newLabel("Land_right");
            String Lend = newLabel("Land_end");

            // dst = 0 (default)
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(dst, new IR.IntegerExp(0)));
            else cur.body.add(new IR.MoveStmt(dst, new IR.IntegerExp(0)));

            // If left != 0 then fall through and evaluate right; else jump to Lend (dst==0)
            IR.Exp cond = new IR.BinOpExp("NE", left, new IR.IntegerExp(0));
            if (cur == null) program.mainStmts.add(new IR.CJumpStmt(cond, Lend));
            else cur.body.add(new IR.CJumpStmt(cond, Lend));

            // Fall-through: left != 0 -> evaluate right into dst = (right != 0)
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(dst, new IR.BinOpExp("NE", right, new IR.IntegerExp(0))));
            else cur.body.add(new IR.MoveStmt(dst, new IR.BinOpExp("NE", right, new IR.IntegerExp(0))));

            if (cur == null) program.mainStmts.add(new IR.JumpStmt(Lend)); else cur.body.add(new IR.JumpStmt(Lend));
            if (cur == null) program.mainStmts.add(new IR.LabelStmt(Lend)); else cur.body.add(new IR.LabelStmt(Lend));
            return new IR.TempExp(dst);
        }

        // OR: PrimaryExpression "||" PrimaryExpression  (short-circuit)
        if (exprNode instanceof OrExpression) {
            OrExpression oe = (OrExpression) exprNode;
            IR.Procedure cur = currentProcedure();
            IR.Exp left = ensureSimple(translateExpression(oe.f0, env), env);
            IR.Exp right = ensureSimple(translateExpression(oe.f2, env), env);

            int dst = env.allocLocal();
            String Lend = newLabel("Lor_end");

            // default: dst = 0
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(dst, new IR.IntegerExp(0)));
            else cur.body.add(new IR.MoveStmt(dst, new IR.IntegerExp(0)));

            // If left != 0 then fall-through into body that sets dst = 1 and jumps to end.
            IR.Exp cond = new IR.BinOpExp("NE", left, new IR.IntegerExp(0));
            if (cur == null) program.mainStmts.add(new IR.CJumpStmt(cond, Lend));
            else cur.body.add(new IR.CJumpStmt(cond, Lend));

            // left != 0: set dst = 1 and jump to end
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(dst, new IR.IntegerExp(1)));
            else cur.body.add(new IR.MoveStmt(dst, new IR.IntegerExp(1)));
            if (cur == null) program.mainStmts.add(new IR.JumpStmt(Lend)); else cur.body.add(new IR.JumpStmt(Lend));

            // else evaluate right and set dst = (right != 0)
            if (cur == null) program.mainStmts.add(new IR.LabelStmt(Lend)); else cur.body.add(new IR.LabelStmt(Lend));
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(dst, new IR.BinOpExp("NE", right, new IR.IntegerExp(0))));
            else cur.body.add(new IR.MoveStmt(dst, new IR.BinOpExp("NE", right, new IR.IntegerExp(0))));

            return new IR.TempExp(dst);
        }

        // ---- LambdaExpression lowering (replacement) ----
        if (exprNode instanceof LambdaExpression) {
            LambdaExpression le = (LambdaExpression) exprNode;

            // Remember the outer procedure (so we can emit closure allocation into it)
            IR.Procedure outerProc = currentProcedure(); // may be null -> program.mainStmts

            // Parameter name
            String paramName = le.f1.f0.toString();

            // Free variables (exclude paramName and 'this')
            List<String> freeVars = collectFreeVariables(le.f3, paramName);

            // Unique lambda label using same convention as other methods
            String ownerClass = (env == null || env.currentClassName == null) ? "Llambda" : env.currentClassName;
            String lambdaLabel = ClassInfo.methodLabel(ownerClass, "lambda" + (lambdaCounter++));


            // Create lambda procedure: argCount = 2 (closure=this, param)
            IR.Procedure lambdaProc = new IR.Procedure(lambdaLabel, 2);

            // Lambda environment: temps start at 2 (0=closure,1=param)
            Env lenv = new Env(env.currentClassName, lambdaLabel, 2);
            // map parameter to TEMP 1
            lenv.addVar(paramName, 1, "int");

            // For each capture, allocate a temp in lenv and emit HLOAD INTO lambdaProc to fill it from closure (TEMP 0)
            List<Integer> captureTemps = new ArrayList<>();
            for (int i = 0; i < freeVars.size(); ++i) {
                String cname = freeVars.get(i);
                int ctemp = lenv.allocLocal();            // new local temp in lambda proc
                lenv.addVar(cname, ctemp, "int");
                captureTemps.add(ctemp);

                // HLOAD ctemp TEMP0 (offset 4 + 4*i) inside lambdaProc
                lambdaProc.body.add(new IR.HLoadStmt(ctemp, new IR.TempExp(0), 4 + 4 * i));
            }

            // Temporarily add lambdaProc so currentProcedure() refers to it while translating the lambda body
            program.procedures.add(lambdaProc);

            // Translate the lambda body using lenv. This translation will emit into lambdaProc.body because currentProcedure() points to lambdaProc.
            IR.Exp lambdaBodyExp = translateExpression(le.f3, lenv);

            // Append return sequence into lambdaProc
            int lambdaRetTemp = lenv.allocLocal();
            lambdaProc.body.add(new IR.MoveStmt(lambdaRetTemp, lambdaBodyExp));
            lambdaProc.body.add(new IR.MoveStmt(0, new IR.TempExp(lambdaRetTemp)));

            // Remove lambdaProc from program.procedures and stash in deferredProcedures to prevent outer emission confusion
            int last = program.procedures.size() - 1;
            if (last >= 0 && program.procedures.get(last) == lambdaProc) {
                program.procedures.remove(last);
            } else {
                program.procedures.remove(lambdaProc);
            }
            if (deferredProcedures == null) deferredProcedures = new ArrayList<>();
            deferredProcedures.add(lambdaProc);

            // --- Now emit closure creation into outerProc (not lambdaProc) ---

            IR.Procedure dst = outerProc; // may be null -> emit to program.mainStmts

            // 1) allocate a vtable block (1 slot => 4 bytes)
            int vtabTemp = env.allocLocal();
            if (dst == null) program.mainStmts.add(new IR.MoveStmt(vtabTemp, new IR.HAllocateExp(new IR.IntegerExp(4))));
            else dst.body.add(new IR.MoveStmt(vtabTemp, new IR.HAllocateExp(new IR.IntegerExp(4))));

            // store lambda label into vtable[0]
            if (dst == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(vtabTemp), 0, new IR.LabelExp(lambdaLabel)));
            else dst.body.add(new IR.HStoreStmt(new IR.TempExp(vtabTemp), 0, new IR.LabelExp(lambdaLabel)));

            // 2) allocate closure object: 4 (vptr) + 4 * #captures
            int closureSize = 4 + 4 * freeVars.size();
            int closureTemp = env.allocLocal();
            if (dst == null) program.mainStmts.add(new IR.MoveStmt(closureTemp, new IR.HAllocateExp(new IR.IntegerExp(closureSize))));
            else dst.body.add(new IR.MoveStmt(closureTemp, new IR.HAllocateExp(new IR.IntegerExp(closureSize))));

            // store vptr into closure[0]
            if (dst == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(closureTemp), 0, new IR.TempExp(vtabTemp)));
            else dst.body.add(new IR.HStoreStmt(new IR.TempExp(closureTemp), 0, new IR.TempExp(vtabTemp)));

            // 3) fill captures: for each free var evaluate its value in outer env and HSTORE into closure[4+4*i]
            for (int i = 0; i < freeVars.size(); ++i) {
                String cname = freeVars.get(i);
                int off = 4 + 4 * i;

                // If capture is an outer local/temp
                Integer outerTemp = env.lookup(cname);
                IR.Exp valExp;
                if (outerTemp != null) {
                    valExp = new IR.TempExp(outerTemp);
                } else {
                    // Otherwise assume it's a field -> HLOAD from TEMP0 of outer into a temp then store
                    FieldInfo finfo = lookupFieldInClassHierarchy(env.currentClassName, cname);
                    if (finfo != null) {
                        int ttemp = env.allocLocal();
                        if (dst == null) program.mainStmts.add(new IR.HLoadStmt(ttemp, new IR.TempExp(0), finfo.getOffset()));
                        else dst.body.add(new IR.HLoadStmt(ttemp, new IR.TempExp(0), finfo.getOffset()));
                        valExp = new IR.TempExp(ttemp);
                    } else {
                        // fallback zero (shouldn't happen for well-typed programs)
                        valExp = new IR.IntegerExp(0);
                    }
                }

                if (dst == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(closureTemp), off, valExp));
                else dst.body.add(new IR.HStoreStmt(new IR.TempExp(closureTemp), off, valExp));
            }

            // Return closure pointer as the value of the lambda expression
            return new IR.TempExp(closureTemp);
        }







        // ArrayLookup: PrimaryExpression [ PrimaryExpression ]
        if (exprNode instanceof ArrayLookup) {
            ArrayLookup al = (ArrayLookup) exprNode;
            IR.Exp arr = translateExpression(al.f0, env);
            IR.Exp idx = translateExpression(al.f2, env);
            IR.Exp idx4 = new IR.BinOpExp("TIMES", ensureSimple(idx, env), new IR.IntegerExp(4));
            IR.Exp offset = new IR.BinOpExp("PLUS", idx4, new IR.IntegerExp(4));
            IR.Exp addr = new IR.BinOpExp("PLUS", ensureSimple(arr, env), ensureSimple(offset, env));
            int addrt = env.allocLocal();
            IR.Procedure cur = currentProcedure();
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(addrt, addr)); else cur.body.add(new IR.MoveStmt(addrt, addr));
            int dst = env.allocLocal();
            if (cur == null) program.mainStmts.add(new IR.HLoadStmt(dst, new IR.TempExp(addrt), 0)); else cur.body.add(new IR.HLoadStmt(dst, new IR.TempExp(addrt), 0));
            return new IR.TempExp(dst);
        }

        // ArrayLength: PrimaryExpression . length
        if (exprNode instanceof ArrayLength) {
            ArrayLength al = (ArrayLength) exprNode;
            IR.Exp arr = translateExpression(al.f0, env);
            int dst = env.allocLocal();
            IR.Procedure cur = currentProcedure();
            if (cur == null) program.mainStmts.add(new IR.HLoadStmt(dst, ensureSimple(arr, env), 0)); else cur.body.add(new IR.HLoadStmt(dst, ensureSimple(arr, env), 0));
            return new IR.TempExp(dst);
        }

        // MessageSend: PrimaryExpression "." Identifier "(" ( ExpressionList )? ")"
        if (exprNode instanceof MessageSend) {
            MessageSend ms = (MessageSend) exprNode;

            // 1) Evaluate receiver and hoist to a simple expression (TEMP or literal)
            IR.Exp recvRaw = translateExpression(ms.f0, env);
            IR.Exp recvSimple = ensureSimple(recvRaw, env);

            // 2) Evaluate args left-to-right and hoist them
            List<IR.Exp> argTemps = new ArrayList<>();
            if (ms.f4.present()) {
                ExpressionList el = (ExpressionList) ms.f4.node;
                argTemps.add(ensureSimple(translateExpression(el.f0, env), env));
                NodeListOptional rest = el.f1;
                for (Enumeration<Node> e = rest.elements(); e.hasMoreElements();) {
                    ExpressionRest er = (ExpressionRest) e.nextElement();
                    argTemps.add(ensureSimple(translateExpression(er.f1, env), env));
                }
            }

            // 3) Build CALL args: receiver first
            List<IR.Exp> callArgs = new ArrayList<>();
            callArgs.add(recvSimple);
            callArgs.addAll(argTemps);

            String methodName = ms.f2.f0.toString();
            MethodInfo targetMethod = null;
            String recvType = null;

            // 4) Try to resolve recvType (robustly)
            if (ms.f0 instanceof PrimaryExpression) {
                Node choice = ((PrimaryExpression)ms.f0).f0.choice;
                if (choice instanceof ThisExpression) {
                    recvType = env.currentClassName;
                } else if (choice instanceof AllocationExpression) {
                    recvType = ((AllocationExpression)choice).f1.f0.toString();
                } else if (choice instanceof Identifier) {
                    String id = ((Identifier)choice).f0.toString();
                    recvType = env.lookupType(id);
                    if (recvType == null) {
                        FieldInfo fi = lookupFieldInClassHierarchy(env.currentClassName, id);
                        if (fi != null) recvType = fi.getTypeName();
                    }
                } else {
                    recvType = inferStaticType(ms.f0, env);
                }
            } else {
                recvType = inferStaticType(ms.f0, env);
            }

            // heuristic: if receiver is TEMP 0, it's "this"
            if (recvType == null && recvSimple instanceof IR.TempExp) {
                if (((IR.TempExp)recvSimple).t == 0) recvType = env.currentClassName;
            }

            // DIAGNOSTIC: print what we resolved (safe even if targetMethod unknown)
            //println("[P3][DBG] MessageSend recvSimple=" + prettyExp(recvSimple)
                   // + " recvType=" + recvType + " method=" + methodName + " in class=" + env.currentClassName);

            // 5) Try to find MethodInfo using recvType
            if (recvType != null) {
                ClassInfo rci = classTable.get(recvType);
                if (rci != null) {
                    targetMethod = rci.getMethodSlots().get(methodName);
                    if (targetMethod == null) {
                        for (MethodInfo mm : rci.getDeclaredMethods()) {
                            if (mm.getName().equals(methodName)) { targetMethod = mm; break; }
                        }
                    }
                }
            }

            // 6) Last-resort global search
            if (targetMethod == null) {
                for (ClassInfo cc : classTable.values()) {
                    for (MethodInfo mm : cc.getMethodSlots().values()) {
                        if (mm.getName().equals(methodName)) { targetMethod = mm; break; }
                    }
                    if (targetMethod != null) break;
                    for (MethodInfo mm : cc.getDeclaredMethods()) {
                        if (mm.getName().equals(methodName)) { targetMethod = mm; break; }
                    }
                    if (targetMethod != null) break;
                }
            }

            // DIAGNOSTIC: print what methodInfo we found (or not) - safe when targetMethod is null
            if (targetMethod != null) {
                //println("[P3][DBG] MessageSend targetMethod=" + targetMethod.getName()
                        //+ " slot=" + targetMethod.getSlotIndex() + " label=" + targetMethod.getLabel());
            } else {
                //println("[P3][DBG] MessageSend targetMethod=NULL");
            }

            // If method not found, allow special-case for "apply" (closure). Otherwise give safe fallback.
            int slot = -1;
            if (targetMethod != null) {
                slot = targetMethod.getSlotIndex();
            } else {
                if ("apply".equals(methodName)) {
                    // fallback: closures put their function label at vtable slot 0
                    slot = 0;
                    //println("[P3][DBG] MessageSend: unresolved 'apply' -> assuming slot 0 for closure apply");
                } else {
                    // method unresolved and is not apply: emit safe zero (no-op) expression
                    return new IR.IntegerExp(0);
                }
            }

            // 7) Emit vtable lookup + CALL
            IR.Procedure cur = currentProcedure();
            int vptrTemp = env.allocLocal();
            if (cur == null) program.mainStmts.add(new IR.HLoadStmt(vptrTemp, recvSimple, 0));
            else cur.body.add(new IR.HLoadStmt(vptrTemp, recvSimple, 0));

            int funTemp = env.allocLocal();
            if (cur == null) program.mainStmts.add(new IR.HLoadStmt(funTemp, new IR.TempExp(vptrTemp), slot * 4));
            else cur.body.add(new IR.HLoadStmt(funTemp, new IR.TempExp(vptrTemp), slot * 4));

            IR.CallExp call = new IR.CallExp(new IR.TempExp(funTemp), callArgs);
            int dst = env.allocLocal();
            if (cur == null) program.mainStmts.add(new IR.MoveStmt(dst, call));
            else cur.body.add(new IR.MoveStmt(dst, call));

            return new IR.TempExp(dst);
        }





        // fallback
        return new IR.IntegerExp(0);
    }

    // Ensure expression is simple; hoist if necessary
    private IR.Exp ensureSimple(IR.Exp e, Env env) {
        // treat only a few as already "simple"
        if (e instanceof IR.TempExp || e instanceof IR.IntegerExp || e instanceof IR.LabelExp) return e;

        IR.Procedure cur = currentProcedure();
        if (cur == null) {
            int t = TempGen.next();
            program.mainStmts.add(new IR.MoveStmt(t, e));
            return new IR.TempExp(t);
        } else {
            int t = env.allocLocal();
            cur.body.add(new IR.MoveStmt(t, e));
            return new IR.TempExp(t);
        }
    }


    /* ---- Object/array helpers ---- */

    private IR.Exp translateNewObject(String className, Env env) {
        ClassInfo ci = classTable.get(className);
        if (ci == null) return new IR.IntegerExp(0);
        IR.Procedure cur = currentProcedure();

        // allocate vtable
        int vtbl = env.allocLocal();
        IR.Exp vtblSize = new IR.IntegerExp(ci.getVTableSizeBytes());
        if (cur == null) program.mainStmts.add(new IR.MoveStmt(vtbl, new IR.HAllocateExp(vtblSize)));
        else cur.body.add(new IR.MoveStmt(vtbl, new IR.HAllocateExp(vtblSize)));

        int slot = 0;
        for (MethodInfo mi : ci.getMethodSlots().values()) {
            int off = slot * 4;
            if (cur == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(vtbl), off, new IR.LabelExp(mi.getLabel())));
            else cur.body.add(new IR.HStoreStmt(new IR.TempExp(vtbl), off, new IR.LabelExp(mi.getLabel())));
            slot++;
        }

        // allocate object
        int obj = env.allocLocal();
        IR.Exp objSize = new IR.IntegerExp(ci.getObjectSizeBytes());
        if (cur == null) program.mainStmts.add(new IR.MoveStmt(obj, new IR.HAllocateExp(objSize)));
        else cur.body.add(new IR.MoveStmt(obj, new IR.HAllocateExp(objSize)));

        // store vtable pointer at offset 0 in object
        if (cur == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(obj), 0, new IR.TempExp(vtbl)));
        else cur.body.add(new IR.HStoreStmt(new IR.TempExp(obj), 0, new IR.TempExp(vtbl)));

        // initialize fields to 0
        for (FieldInfo f : ci.getAllFields()) {
            if (cur == null) program.mainStmts.add(new IR.HStoreStmt(new IR.TempExp(obj), f.getOffset(), new IR.IntegerExp(0)));
            else cur.body.add(new IR.HStoreStmt(new IR.TempExp(obj), f.getOffset(), new IR.IntegerExp(0)));
        }

        return new IR.TempExp(obj);
    }

    private IR.Exp varToAddrExp(String name, Env env, IR.Procedure cur) {
        Integer t = env.lookup(name);
        if (t != null) return new IR.TempExp(t);
        FieldInfo fi = lookupFieldInClassHierarchy(env.currentClassName, name);
        if (fi != null) {
            int dst = env.allocLocal();
            if (cur == null) program.mainStmts.add(new IR.HLoadStmt(dst, new IR.TempExp(0), fi.getOffset()));
            else cur.body.add(new IR.HLoadStmt(dst, new IR.TempExp(0), fi.getOffset()));
            return new IR.TempExp(dst);
        }
        return new IR.IntegerExp(0);
    }

    private FieldInfo findFieldInClassChain(ClassInfo ci, String name) {
        if (ci == null) return null;
        for (FieldInfo f : ci.getAllFields()) if (f.getName().equals(name)) return f;
        return null;
    }

    /* ---- Type inference helpers ---- */

    private String inferStaticType(Node exprNode, Env env) {
        if (exprNode == null) return null;
        if (exprNode instanceof PrimaryExpression) return inferStaticType(((PrimaryExpression)exprNode).f0.choice, env);
        if (exprNode instanceof Identifier) {
            String id = ((Identifier)exprNode).f0.toString();
            String t = env.lookupType(id);
            if (t != null) return t;
            FieldInfo fi = lookupFieldInClassHierarchy(env.currentClassName, id);
            if (fi != null) return fi.getTypeName();
            return null;
        }
        if (exprNode instanceof ThisExpression) return env.currentClassName;
        if (exprNode instanceof AllocationExpression) return ((AllocationExpression)exprNode).f1.f0.toString();
        if (exprNode instanceof MessageSend) {
            MessageSend ms = (MessageSend)exprNode;
            String rt = inferStaticType(ms.f0, env);
            if (rt == null) return null;
            ClassInfo rci = classTable.get(rt);
            if (rci == null) return null;
            String mname = ms.f2.f0.toString();
            MethodInfo mi = rci.getMethodSlots().get(mname);
            if (mi == null) {
                for (MethodInfo mm : rci.getMethodSlots().values()) if (mm.getName().equals(mname)) { mi = mm; break; }
            }
            return mi == null ? null : mi.getReturnType();
        }
        if (exprNode instanceof ArrayLookup) return "int";
        if (exprNode instanceof ArrayAllocationExpression) return "int[]";
        if (exprNode instanceof IntegerLiteral) return "int";
        if (exprNode instanceof TrueLiteral || exprNode instanceof FalseLiteral) return "boolean";
        return null;
    }

    // Convert a Minijava 'Type' AST node into a canonical string name used across Env/Class table.
    // - For ordinary class types (Identifier) => the identifier name
    // - For LambdaType => "Function" (we normalize function types to this name)
    // - For ArrayType => "int[]"  (or "int[]" if you prefer; for lookups arrays are not classes)
    // - For IntegerType / BooleanType => "int" / "boolean"
    private String typeToString(Node typeNode) {
        if (typeNode == null) return null;

        // Most generated JTB nodes have concrete classes: ArrayType, IntegerType, BooleanType, Identifier, LambdaType
        if (typeNode instanceof LambdaType) {
            return "Function";
        }
        if (typeNode instanceof ArrayType) {
            return "int[]";
        }
        if (typeNode instanceof IntegerType) {
            return "int";
        }
        if (typeNode instanceof BooleanType) {
            return "boolean";
        }

        // Sometimes Type is a NodeChoice wrapping Identifier
        // If the node *is* an Identifier, return its name
        if (typeNode instanceof Identifier) {
            return ((Identifier) typeNode).f0.toString();
        }

        // Defensive: try to extract identifier token if the node contains one
        try {
            // Many Type nodes have f0 field pointing to an Identifier NodeToken; this is a fallback
            java.lang.reflect.Field f0 = typeNode.getClass().getField("f0");
            Object o = f0.get(typeNode);
            if (o != null) return o.toString();
        } catch (Exception ignore) { }

        // Last resort: use toString() but prefer null-safe
        return typeNode.toString();
    }


    // small helper for debugging
    private String prettyExp(IR.Exp e) {
        if (e instanceof IR.TempExp) return "TEMP " + ((IR.TempExp)e).t;
        if (e instanceof IR.IntegerExp) return Integer.toString(((IR.IntegerExp)e).value);
        if (e instanceof IR.LabelExp) return "LABEL(" + ((IR.LabelExp)e).label + ")";
        if (e instanceof IR.CallExp) return "CALL(...)";
        return e.getClass().getSimpleName();
    }

    // Find a MessageSend node anywhere in the subtree rooted at 'n' (DFS).
    // Returns the first MessageSend encountered, or null if none.
    private MessageSend findMessageSendInSubtree(Node n) {
        if (n == null) return null;
        if (n instanceof MessageSend) return (MessageSend) n;
        // Many AST nodes are NodeSequence/NodeOptional/etc. Recurse into known container fields.
        if (n instanceof NodeSequence) {
            for (Enumeration<Node> e = ((NodeSequence)n).elements(); e.hasMoreElements();) {
                Node ch = e.nextElement();
                MessageSend ms = findMessageSendInSubtree(ch);
                if (ms != null) return ms;
            }
            return null;
        }
        if (n instanceof NodeChoice) {
            try {
                NodeChoice nc = (NodeChoice) n;
                Node ch = (Node) nc.choice;
                return findMessageSendInSubtree(ch);
            } catch (Throwable ignore) {}
        }
        if (n instanceof NodeOptional) {
            NodeOptional no = (NodeOptional) n;
            if (no.present()) return findMessageSendInSubtree(no.node);
            return null;
        }
        if (n instanceof NodeList) {
            for (Enumeration<Node> e = ((NodeList)n).elements(); e.hasMoreElements();) {
                MessageSend ms = findMessageSendInSubtree(e.nextElement());
                if (ms != null) return ms;
            }
            return null;
        }
        if (n instanceof NodeListOptional) {
            for (Enumeration<Node> e = ((NodeListOptional)n).elements(); e.hasMoreElements();) {
                MessageSend ms = findMessageSendInSubtree(e.nextElement());
                if (ms != null) return ms;
            }
            return null;
        }
        // Fallback: try to reflectively inspect common fields (conservative)
        try {
            // some nodes store subnodes in public fields f0..fN — try a few common ones
            for (int i = 0; i < 6; ++i) {
                try {
                    java.lang.reflect.Field f = n.getClass().getField("f" + i);
                    Object v = f.get(n);
                    if (v instanceof Node) {
                        MessageSend ms = findMessageSendInSubtree((Node)v);
                        if (ms != null) return ms;
                    } else if (v instanceof NodeList) {
                        for (Enumeration<Node> e = ((NodeList)v).elements(); e.hasMoreElements();) {
                            MessageSend ms = findMessageSendInSubtree(e.nextElement());
                            if (ms != null) return ms;
                        }
                    } else if (v instanceof NodeOptional) {
                        NodeOptional no = (NodeOptional)v;
                        if (no.present()) {
                            MessageSend ms = findMessageSendInSubtree(no.node);
                            if (ms != null) return ms;
                        }
                    }
                } catch (NoSuchFieldException nsf) {
                    break;
                } catch (Throwable inner) {
                    // ignore and continue
                }
            }
        } catch (Throwable t) {
            // ignore
        }
        return null;
    }

    // Lookup a field named `fieldName` starting at class `startClassName` and moving up
    // the inheritance chain. For each class we inspect its declared fields first so that
    // shadowing works (child fields override parent fields when accessed from inside child).
    private FieldInfo lookupFieldInClassHierarchy(String startClassName, String fieldName) {
        if (startClassName == null) return null;
        String cur = startClassName;
        while (cur != null) {
            ClassInfo ci = classTable.get(cur);
            if (ci == null) break;
            // check declared fields first (to respect shadowing)
            for (FieldInfo f : ci.getDeclaredFields()) {
                if (f.getName().equals(fieldName)) return f;
            }
            // then move to parent
            cur = ci.getParentName();
        }
        return null;
    }

        // add a small counter field in the class (top of MJIRGenerator)
    private int labelCounter = 0;

    // helper to make unique labels
    private String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

        /**
     * Collect free variables (identifiers) referenced in the subtree rooted at `n`,
     * excluding the lambda parameter name and `this`. We return a stable list in
     * deterministic order (in the order we encounter them).
     *
     * This is a conservative collector that assumes the input is type-checked.
     */
    private List<String> collectFreeVariables(Node n, String paramName) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        collectFreeVarsRec(n, paramName, found);
        return new ArrayList<>(found);
    }

    private void collectFreeVarsRec(Node n, String paramName, Set<String> out) {
        if (n == null) return;
        if (n instanceof Identifier) {
            String id = ((Identifier)n).f0.toString();
            // exclude parameter name and "this"
            if (id.equals(paramName) || id.equals("this")) return;
            // If identifier resolves to a known local/param/field in the outer scope, treat as capture
            out.add(id);
            return;
        }
        // Recurse into children for typical container node types
        if (n instanceof NodeSequence) {
            for (Enumeration<Node> e = ((NodeSequence)n).elements(); e.hasMoreElements();) collectFreeVarsRec(e.nextElement(), paramName, out);
            return;
        }
        if (n instanceof NodeChoice) {
            try {
                NodeChoice nc = (NodeChoice) n;
                Node ch = (Node) nc.choice;
                collectFreeVarsRec(ch, paramName, out);
            } catch (Throwable ignore) {}
            return;
        }
        if (n instanceof NodeOptional) {
            NodeOptional no = (NodeOptional) n;
            if (no.present()) collectFreeVarsRec(no.node, paramName, out);
            return;
        }
        if (n instanceof NodeList) {
            for (Enumeration<Node> e = ((NodeList)n).elements(); e.hasMoreElements();) collectFreeVarsRec(e.nextElement(), paramName, out);
            return;
        }
        if (n instanceof NodeListOptional) {
            for (Enumeration<Node> e = ((NodeListOptional)n).elements(); e.hasMoreElements();) collectFreeVarsRec(e.nextElement(), paramName, out);
            return;
        }
        // fallback: reflectively inspect fields f0..fN (conservative)
        try {
            for (int i = 0; i < 6; ++i) {
                try {
                    java.lang.reflect.Field f = n.getClass().getField("f" + i);
                    Object v = f.get(n);
                    if (v instanceof Node) collectFreeVarsRec((Node)v, paramName, out);
                    else if (v instanceof NodeList) {
                        for (Enumeration<Node> e = ((NodeList)v).elements(); e.hasMoreElements();) collectFreeVarsRec(e.nextElement(), paramName, out);
                    } else if (v instanceof NodeOptional) {
                        NodeOptional no = (NodeOptional)v;
                        if (no.present()) collectFreeVarsRec(no.node, paramName, out);
                    }
                } catch (NoSuchFieldException nsf) {
                    break;
                } catch (Throwable inner) { /* ignore */ }
            }
        } catch (Throwable t) { /* ignore */ }
    }

    private void ensureFunctionClass() {
        if (this.classTable.containsKey("Function")) return;

        ClassInfo fci = new ClassInfo("Function", null);

        MethodInfo apply = new MethodInfo("apply", ClassInfo.methodLabel("Function", "apply"), 2, "int");

        // Add declared method properly so slot index gets assigned (slot 0)
        fci.addDeclaredMethod(apply);

        if (fci.getObjectSizeBytes() < 4) {
            try {
                java.lang.reflect.Method m = fci.getClass().getMethod("setObjectSize", int.class);
                m.invoke(fci, 4);
            } catch (Throwable ignore) { /* not critical */ }
        }
        if (fci.getVTableSizeBytes() < 4) {
            try {
                java.lang.reflect.Method m = fci.getClass().getMethod("setVtableSize", int.class);
                m.invoke(fci, 4);
            } catch (Throwable ignore) { /* not critical */ }
        }

        this.classTable.put("Function", fci);

    }


}
