import syntaxtree.*;
import java.util.*;
import visitor.*;

public class MiniToMicroVisitor extends GJDepthFirst<String, Object> {
    int label_ind = 1000;       //a heuristic high new label number

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */

    @Override
    public String visit(Goal n, Object argu){
        String _ret=null;
        System.out.println("MAIN");
        n.f1.accept(this, argu);
        System.out.println("END");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ( ( Label() )? Stmt() )*
     */

    @Override
    public String visit(StmtList n, Object argu) {
        if (n.f0.present()) {
            for (Node node : n.f0.nodes) {
                NodeSequence labelStmt = (NodeSequence) node;
                NodeOptional labelOpt = (NodeOptional) labelStmt.nodes.elementAt(0);
                Node stmtNode = (Node) labelStmt.nodes.elementAt(1);
                if (labelOpt.present()) {
                    Node label = labelOpt.node;
                    System.out.println(label.accept(this, argu));
                }
                stmtNode.accept(this, argu);
            }
        }
        return null;
    }

    /**
     * f0 -> Label()
     * f1 -> "["
     * f2 -> IntegerLiteral()
     * f3 -> "]"
     * f4 -> StmtExp()
     */

    @Override
    public String visit(Procedure n, Object argu){
        String _ret = null;

        System.out.format("%s [ %s ]\n", n.f0.accept(this, argu), n.f2.accept(this, argu));

        n.f3.accept(this, argu);
        n.f4.accept(this, (Object) "Fn_start");

        return _ret;
    }

    /**
     * f0 -> "NOOP"
     */

    @Override
    public String visit(NoOpStmt n, Object argu){
        System.out.println("NOOP");
        return null;
    }

    /**
     * f0 -> "ERROR"
     */
    @Override
    public String visit(ErrorStmt n, Object argu) {
        System.out.println("ERROR");
        return null;
    }

    /**
    * f0 -> "CJUMP"
    * f1 -> Exp()
    * f2 -> Label()
    */
   public String visit(CJumpStmt n, Object argu) {
        String temp = "TEMP " + (label_ind++);
        System.out.format(
            "MOVE %s %s\nCJUMP %s %s\n",
            temp,
            n.f1.accept(this, argu),
            temp,
            n.f2.accept(this, argu)
        );
        
        return null;
    }
    /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public String visit(JumpStmt n, Object argu) {
        System.out.printf("JUMP %s\n", n.f1.accept(this, argu));
         return null;
    }

    /**
     * f0 -> "HSTORE"
     * f1 -> Exp()
     * f2 -> IntegerLiteral()
     * f3 -> Exp()
     */
   public String visit(HStoreStmt n, Object argu) {
        String temp1 = "TEMP " + (label_ind++);
        String temp2 = "TEMP " + (label_ind++);
        System.out.format(
            "MOVE %s %s\nMOVE %s %s\nHSTORE %s %s %s\n",
            temp1,
            n.f1.accept(this, argu), 
            temp2,
            n.f3.accept(this, argu), 
            temp1,
            n.f2.accept(this, argu),
            temp2
        );
        
        return null;
    }
    /**
     * f0 -> "HLOAD"
     * f1 -> Temp()
     * f2 -> Exp()
     * f3 -> IntegerLiteral()
     */
    public String visit(HLoadStmt n, Object argu) {
        String temp = "TEMP " + (label_ind++);
        System.out.format(
            "MOVE %s %s\nHLOAD %s %s %s\n",
            temp,
            n.f2.accept(this, argu),
            n.f1.accept(this, argu),
            temp,
            n.f3.accept(this, argu)
        );
        
        return null;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
   public String visit(MoveStmt n, Object argu) {
        System.out.format(
            "MOVE %s %s\n",
            n.f1.accept(this, argu),
            n.f2.accept(this, argu)
        );
        return null;
    }
    /**
     * f0 -> "PRINT"
     * f1 -> Exp()
     */
    public String visit(PrintStmt n, Object argu) {
        String temp = "TEMP " + (label_ind++);
        System.out.format(
            "MOVE %s %s\nPRINT %s\n",
            temp,
            n.f1.accept(this, argu),
            temp
        );
        
        return null;
    }

    /**
     * f0 -> StmtExp()
     *       | Call()
     *       | HAllocate()
     *       | BinOp()
     *       | Temp()
     *       | IntegerLiteral()
     *       | Label()
     */
    public String visit(Exp n, Object argu) {
        return (String) n.f0.accept(this, argu);
    }

    /**
     * f0 -> "BEGIN"
     * f1 -> StmtList()
     * f2 -> "RETURN"
     * f3 -> Exp()
     * f4 -> "END"
     */
    public String visit(StmtExp n, Object argu) {
        String temp = "TEMP " + (label_ind++);
        String argString = (String) argu;

        if ("Fn_start".equals(argString)) {
            System.out.println("BEGIN");
        }

        n.f1.accept(this, null);
        String exp = (String) n.f3.accept(this, null);

        if ("Fn_start".equals(argString)) {
            System.out.println("RETURN " + exp);
            System.out.println("END");
        } else {
            System.out.println("MOVE " + temp + " " + exp);
        }

        return (String) temp;
    }

    /**
     * f0 -> "CALL"
     * f1 -> Exp()
     * f2 -> "("
     * f3 -> ( Exp() )*
     * f4 -> ")"
     */
    public String visit(Call n, Object argu) {
        String res = "TEMP " + (label_ind++);
        String fp = "TEMP " + (label_ind++);

        String func_name = (String) n.f1.accept(this, argu);
        System.out.println("MOVE " + fp + " " + func_name);

        StringBuilder build = new StringBuilder("CALL ").append(fp).append(" ( ");

        if (n.f3.present()) {
            for (Node exp : n.f3.nodes) {
                String arg = "TEMP " + (label_ind++);
                System.out.println("MOVE " + arg + " " + exp.accept(this, argu));
                build.append(arg).append(" ");
            }
        }

        build.append(")\n");
        System.out.println("MOVE " + res + " " + build.toString());

        return (String) res;
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> Exp()
     */
   public String visit(HAllocate n, Object argu) {
        String res = "TEMP " + (label_ind++);
        String size = "TEMP " + (label_ind++);
        System.out.format(
            "MOVE %s %s\nMOVE %s HALLOCATE %s\n",
            size,
            n.f1.accept(this, argu),
            res,
            size
        );
        
        return (String) res;
    }

    /**
     * f0 -> Operator()
     * f1 -> Exp()
     * f2 -> Exp()
     */
    public String visit(BinOp n, Object argu) {
        String res = "TEMP " + (label_ind++);
        String leftTemp = "TEMP " + (label_ind++);
        String rightTemp = "TEMP " + (label_ind++);
        String op = (String) n.f0.accept(this, argu);
        String left = (String) n.f1.accept(this, argu);
        String right = (String) n.f2.accept(this, argu);
        System.out.format(
            "MOVE %s %s\nMOVE %s %s\nMOVE %s %s %s %s\n",
            leftTemp,
            left,
            rightTemp,
            right,
            res,
            op,
            leftTemp,
            rightTemp
        );
        
        return (String) res;
    }
    /**
     * f0 -> "LE"
     *       | "NE"
     *       | "PLUS"
     *       | "MINUS"
     *       | "TIMES"
     *       | "DIV"
     */
   public String visit(Operator n, Object argu) {
        return (String) n.f0.choice.toString();
    }
    
    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
   public String visit(Temp n, Object argu) {
        return (String) ("TEMP " + n.f1.accept(this, argu));
    }
    
    /**
     * f0 -> <INTEGER_LITERAL>
     */
   public String visit(IntegerLiteral n, Object argu) {
        return (String) n.f0.toString();
    }
    
    /**
     * f0 -> <IDENTIFIER>
     */
   public String visit(Label n, Object argu) {
        return (String) n.f0.toString();
    }

}