import java.util.*;

/**
 * Small IR model for MiniIR generation.
 *
 * Extended for Milestone 3: added HAllocateExp to represent HALLOCATE <Exp>.
 */
public class IR {

    public static class Program {
        public final List<Stmt> mainStmts = new ArrayList<>();
        public final List<Procedure> procedures = new ArrayList<>();
    }

    public static class Procedure {
        public final String label;
        public final int argCount;
        public final List<Stmt> body = new ArrayList<>();
        public Procedure(String label, int argCount) { this.label = label; this.argCount = argCount; }
    }

    /* Statements */
    public static abstract class Stmt {}

    public static class NoOpStmt extends Stmt { @Override public String toString(){ return "NOOP"; } }
    public static class ErrorStmt extends Stmt { @Override public String toString(){ return "ERROR"; } }

    public static class CJumpStmt extends Stmt {
        public final Exp cond; public final String label;
        public CJumpStmt(Exp cond, String label){ this.cond = cond; this.label = label; }
    }

    public static class JumpStmt extends Stmt { public final String label; public JumpStmt(String label){ this.label = label; } }

    public static class HStoreStmt extends Stmt {
        public final Exp addr; public final int offset; public final Exp value;
        public HStoreStmt(Exp addr, int offset, Exp value){ this.addr = addr; this.offset = offset; this.value = value; }
    }

    public static class HLoadStmt extends Stmt {
        public final int targetTemp; public final Exp addr; public final int offset;
        public HLoadStmt(int targetTemp, Exp addr, int offset){ this.targetTemp = targetTemp; this.addr = addr; this.offset = offset; }
    }

    public static class MoveStmt extends Stmt {
        public final int targetTemp; public final Exp src;
        public MoveStmt(int targetTemp, Exp src){ this.targetTemp = targetTemp; this.src = src; }
    }

    public static class PrintStmt extends Stmt {
        public final Exp exp; public PrintStmt(Exp e){ this.exp = e; }
    }

    public static class LabelStmt extends Stmt { public final String label; public LabelStmt(String l){ this.label = l; } }

    /* Expressions */
    public static abstract class Exp {}

    public static class TempExp extends Exp { public final int t; public TempExp(int t){ this.t = t; } }
    public static class IntegerExp extends Exp { public final int value; public IntegerExp(int v){ this.value = v; } }
    public static class LabelExp extends Exp { public final String label; public LabelExp(String l){ this.label = l; } }
    public static class BinOpExp extends Exp { public final String op; public final Exp left, right; public BinOpExp(String op, Exp left, Exp right){ this.op=op; this.left=left; this.right=right; } }
    public static class CallExp extends Exp { public final Exp target; public final List<Exp> args; public CallExp(Exp target, List<Exp> args){ this.target=target; this.args=args; } }
    public static class StmtExp extends Exp { public final List<Stmt> stmts; public final Exp ret; public StmtExp(List<Stmt> stmts, Exp ret){ this.stmts = stmts; this.ret = ret; } }

    // HALLOCATE <Exp>
    public static class HAllocateExp extends Exp {
        public final Exp sizeExp;
        public HAllocateExp(Exp sizeExp){ this.sizeExp = sizeExp; }
    }
}
