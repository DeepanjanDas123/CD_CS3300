import java.util.*;

/**
 * IRPrinter: emits textual MiniIR for the IR model.
 *
 * Important: labels are printed as bare identifiers on their own line (no colon),
 * because MiniIR grammar expects a Label token (an identifier), not "label:".
 */
public class IRPrinter {

    private final StringBuilder sb = new StringBuilder();

    public void printProgram(IR.Program prog) {
        sb.setLength(0);
        sb.append("MAIN\n");

        // print main statements, ensuring labels are followed by a statement (insert NOOP in output if needed)
        for (int i = 0; i < prog.mainStmts.size(); ++i) {
            IR.Stmt s = prog.mainStmts.get(i);
            printStmt(s);
            sb.append("\n");
            if (s instanceof IR.LabelStmt) {
                boolean nextIsLabelOrEnd = (i + 1 >= prog.mainStmts.size()) || (prog.mainStmts.get(i + 1) instanceof IR.LabelStmt);
                if (nextIsLabelOrEnd) {
                    sb.append("NOOP\n");
                }
            }
        }

        sb.append("END\n");

        for (IR.Procedure p : prog.procedures) {
            sb.append(p.label).append(" [").append(p.argCount).append("] \n");
            sb.append("BEGIN\n");

            // print procedure body with the same label-followed-by-stmt guard
            for (int i = 0; i < p.body.size(); ++i) {
                IR.Stmt s = p.body.get(i);
                printStmt(s);
                sb.append("\n");
                if (s instanceof IR.LabelStmt) {
                    boolean nextIsLabelOrEnd = (i + 1 >= p.body.size()) || (p.body.get(i + 1) instanceof IR.LabelStmt);
                    if (nextIsLabelOrEnd) {
                        sb.append("NOOP\n");
                    }
                }
            }

            // print RETURN expression same as before (derive from last MoveStmt if present)
            sb.append("RETURN ");
            if (!p.body.isEmpty()) {
                IR.Stmt last = p.body.get(p.body.size()-1);
                if (last instanceof IR.MoveStmt) {
                    IR.MoveStmt ms = (IR.MoveStmt) last;
                    printExp(ms.src);
                    sb.append("\n");
                } else {
                    sb.append("0\n");
                }
            } else {
                sb.append("0\n");
            }
            sb.append("END\n");
        }
        System.out.print(sb.toString());
    }


    private void printStmt(IR.Stmt s) {
        if (s instanceof IR.NoOpStmt) {
            sb.append("NOOP");
        } else if (s instanceof IR.ErrorStmt) {
            sb.append("ERROR");
        } else if (s instanceof IR.CJumpStmt) {
            IR.CJumpStmt cs = (IR.CJumpStmt) s;
            sb.append("CJUMP ");
            printExp(cs.cond);
            sb.append(" ").append(cs.label);
        } else if (s instanceof IR.JumpStmt) {
            IR.JumpStmt js = (IR.JumpStmt) s;
            sb.append("JUMP ").append(js.label);
        } else if (s instanceof IR.HStoreStmt) {
            IR.HStoreStmt hs = (IR.HStoreStmt) s;
            sb.append("HSTORE ");
            printExp(hs.addr);
            sb.append(" ").append(hs.offset).append(" ");
            printExp(hs.value);
        } else if (s instanceof IR.HLoadStmt) {
            IR.HLoadStmt hl = (IR.HLoadStmt) s;
            sb.append("HLOAD TEMP ").append(hl.targetTemp).append(" ");
            printExp(hl.addr);
            sb.append(" ").append(hl.offset);
        } else if (s instanceof IR.MoveStmt) {
            IR.MoveStmt ms = (IR.MoveStmt) s;
            sb.append("MOVE TEMP ").append(ms.targetTemp).append(" ");
            printExp(ms.src);
        } else if (s instanceof IR.PrintStmt) {
            IR.PrintStmt ps = (IR.PrintStmt) s;
            sb.append("PRINT ");
            printExp(ps.exp);
        } else if (s instanceof IR.LabelStmt) {
            IR.LabelStmt ls = (IR.LabelStmt) s;
            // IMPORTANT: print the label as a bare identifier on its own line (no colon)
            sb.append(ls.label);
        } else {
            sb.append("#UNKNOWN_STMT");
        }
    }

    private void printExp(IR.Exp e) {
        if (e instanceof IR.TempExp) {
            IR.TempExp te = (IR.TempExp) e;
            sb.append("TEMP ").append(te.t);
        } else if (e instanceof IR.IntegerExp) {
            sb.append(((IR.IntegerExp)e).value);
        } else if (e instanceof IR.LabelExp) {
            sb.append(((IR.LabelExp)e).label);
        } else if (e instanceof IR.BinOpExp) {
            IR.BinOpExp bo = (IR.BinOpExp) e;
            sb.append(bo.op).append(" ");
            printExp(bo.left);
            sb.append(" ");
            printExp(bo.right);
        } else if (e instanceof IR.CallExp) {
            IR.CallExp c = (IR.CallExp) e;
            sb.append("CALL ");
            printExp(c.target);
            sb.append(" (");
            for (int i=0;i<c.args.size();++i) {
                printExp(c.args.get(i));
                if (i+1<c.args.size()) sb.append(" ");
            }
            sb.append(")");
        } else if (e instanceof IR.StmtExp) {
            IR.StmtExp se = (IR.StmtExp) e;
            sb.append("BEGIN ");
            sb.append("... ");
            sb.append("RETURN ");
            printExp(se.ret);
            sb.append(" END");
        } else if (e instanceof IR.HAllocateExp) {
            IR.HAllocateExp ha = (IR.HAllocateExp) e;
            sb.append("HALLOCATE ");
            printExp(ha.sizeExp);
        } else {
            sb.append("0");
        }
    }
}
