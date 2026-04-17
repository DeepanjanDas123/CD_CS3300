package visitor;
import syntaxtree.*;
import java.util.*;

public class FirstPassVisitor<R,A> extends GJDepthFirst<R,A> {

   /**
    * f0 -> "MAIN"
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    * f12 -> ( SpillInfo() )?
    * f13 -> ( Procedure() )*
    * f14 -> <EOF>
    */
 @Override
 public R visit(Goal n, A argu) {
    R _ret = null;
    int vars = Integer.parseInt(n.f5.f0.toString());
    int stack_size = (vars + 2) * 4;
    String pre = ".text\n.globl main\nmain:\n" + "sw $fp, -8($sp)\n" + "move $fp, $sp\n" + "sw $ra, -4($fp)\n" + "subu $sp, $sp, " + stack_size + "\n";
    System.out.print(pre);
    n.f10.accept(this, argu);
    String suff = "addu $sp, $sp, " + stack_size + "\n" + "lw $ra, -4($fp)\n" + "lw $fp, -8($sp)\n" + "j $ra\n";
    System.out.print(suff);
    if (n.f13.present()) {
        for (Node procedure : n.f13.nodes) {
            procedure.accept(this, argu);
        }
    }
    System.out.println(".text\n.globl _halloc\n_halloc:");
    System.out.println("li $v0, 9");
    System.out.println("syscall");
    System.out.println("j $ra");
    System.out.println(".text\n.globl _print\n_print:");
    System.out.println("li $v0, 1");
    System.out.println("syscall");
    System.out.println("la $a0, newl");
    System.out.println("li $v0, 4");
    System.out.println("syscall");
    System.out.println("j $ra");
    System.out.println(".data\n.align 0\nnewl: .asciiz \"\\n\"\n.data\n.align 0\nstr_er: .asciiz \"ERROR: abnormal termination\\n\"");
    return _ret;
}

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
  @Override
  public R visit(StmtList n, A argu) {
    R _ret = null;
    if (n.f0.present()) {
        for (Node node : n.f0.nodes) {
            NodeSequence  stmt_with_label = (NodeSequence) node;
            NodeOptional opt_lbl = (NodeOptional)  stmt_with_label.nodes.elementAt(0);
            Node stmt = (Node)  stmt_with_label.nodes.elementAt(1);
            if (opt_lbl.present()) {
                Label lbl = (Label) opt_lbl.node;
                System.out.println(lbl.f0.toString() + ":\n");
            }
            stmt.accept(this, argu);
        }
    }
    return _ret;
}
   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    * f12 -> ( SpillInfo() )?
    */
   @Override
  public R visit(Procedure n, A argu) {
    R _ret = null;
    String nm = n.f0.f0.toString();
    int stack_size = (Integer.parseInt(n.f5.f0.toString()) + 2) * 4;
    String pre = ".text\n" + ".globl " + nm + "\n" + nm + ":\n" + "sw $fp, -8($sp)\n" + "move $fp, $sp\n" + "sw $ra, -4($fp)\n" + "subu $sp, $sp, " + stack_size + "\n";
    System.out.print(pre);
    n.f10.accept(this, argu);
    String suff = "addu $sp, $sp, " + stack_size + "\n" + "lw $ra, -4($fp)\n" + "lw $fp, -8($sp)\n" + "j $ra\n";
    System.out.print(suff);
    return _ret;
}

   /**
    * f0 -> "NOOP"
    */
   @Override
   public R visit(NoOpStmt n, A argu) {
      R _ret=null;
     System.out.println("nop");
      return _ret;
   }

   /**
    * f0 -> "ERROR"
    */
   @Override
  public R visit(ErrorStmt n, A argu) {
    R _ret = null;
    System.out.println("la $a0, str_er");
    System.out.println("li $v0, 4");
    System.out.println("syscall");
    System.out.println("li $v0, 10");
    System.out.println("syscall");
    return _ret;
}

   /**
    * f0 -> "CJUMP"
    * f1 -> Reg()
    * f2 -> Label()
    */
   @Override
  public R visit(CJumpStmt n, A argu) {
    R _ret = null;
    String reg = (String) n.f1.accept(this, argu);
    String label = n.f2.f0.toString();
    System.out.println("beqz " + reg + ", " + label);
    return _ret;
}

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   @Override
  public R visit(JumpStmt n, A argu) {
    R _ret = null;
    String lbl = n.f1.f0.toString();
    System.out.println("j " + lbl);
    return _ret;
}
   /**
    * f0 -> "HSTORE"
    * f1 -> Reg()
    * f2 -> IntegerLiteral()
    * f3 -> Reg()
    */
   @Override
   public R visit(HStoreStmt n, A argu) {
    R _ret = null;
    String base = (String) n.f1.accept(this, argu);
    String off = n.f2.f0.toString();
    String src = (String) n.f3.accept(this, argu);
    System.out.println("sw " + src + ", " + off + "(" + base + ")");
    return _ret;
}

   /**
    * f0 -> "HLOAD"
    * f1 -> Reg()
    * f2 -> Reg()
    * f3 -> IntegerLiteral()
    */
   @Override
 public R visit(HLoadStmt n, A argu) {
    R _ret = null;
    String dest = (String) n.f1.accept(this, argu);
    String base = (String) n.f2.accept(this, argu);
    String off = n.f3.f0.toString();
    System.out.println("lw " + dest + ", " + off + "(" + base + ")");
    return _ret;
}

   /**
    * f0 -> "MOVE"
    * f1 -> Reg()
    * f2 -> Exp()
    */
   @Override
  public R visit(MoveStmt n, A argu) {
    R _ret = null;
    String dest = (String) n.f1.accept(this, argu);
    Node exp = n.f2.f0.choice;
    if(exp instanceof SimpleExp){
        SimpleExp simple = (SimpleExp) exp;
        if (simple.f0.choice instanceof IntegerLiteral) {
            String val = ((IntegerLiteral) simple.f0.choice).f0.toString();
            System.out.println("li " + dest + ", " + val);
        } else if (simple.f0.choice instanceof Label) {
            String lbl = ((Label) simple.f0.choice).f0.toString();
            System.out.println("la " + dest + ", " + lbl);
        } else if (simple.f0.choice instanceof Reg) {
            String src = (String) ((Reg) simple.f0.choice).accept(this, argu);
            System.out.println("move " + dest + ", " + src);
        }
    }
    else if (exp instanceof BinOp) {
        BinOp binop = (BinOp) exp;
        binop.accept(this, (A) dest);
    } else {
        HAllocate halloc = (HAllocate) exp;
        halloc.accept(this, (A) dest);
    }
    return _ret;
}

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   @Override
  public R visit(PrintStmt n, A argu) {
    R _ret = null;
    Node exp = n.f1.f0.choice;
    if (exp instanceof Reg) {
        String reg = (String) ((Reg) exp).accept(this, argu);
        System.out.println("move $a0, " + reg);
    } else if (exp instanceof IntegerLiteral) {
        String val = ((IntegerLiteral) exp).f0.toString();
        System.out.println("li $a0, " + val);
    } else if (exp instanceof Label) {
        String lbl = ((Label) exp).f0.toString();
        System.out.println("la $a0, " + lbl);
    }
    System.out.println("jal _print");
    return _ret;
}

   /**
    * f0 -> "ALOAD"
    * f1 -> Reg()
    * f2 -> SpilledArg()
    */
   @Override
  public R visit(ALoadStmt n, A argu) {
    R _ret = null;
    String dest = (String) n.f1.accept(this, argu);
    String src = (String) n.f2.accept(this, argu);
    System.out.println("lw " + dest + ", " + src);
    return _ret;
}

   /**
    * f0 -> "ASTORE"
    * f1 -> SpilledArg()
    * f2 -> Reg()
    */
   @Override
  public R visit(AStoreStmt n, A argu) {
    R _ret = null;
    String dest = (String) n.f1.accept(this, argu);
    String src = (String) n.f2.accept(this, argu);
    System.out.println("sw " + src + ", " + dest);
    return _ret;
}

   /**
    * f0 -> "PASSARG"
    * f1 -> IntegerLiteral()
    * f2 -> Reg()
    */
   @Override
  public R visit(PassArgStmt n, A argu) {
    R _ret = null;
    String src = (String) n.f2.accept(this, argu);
    int num = Integer.parseInt(n.f1.f0.toString());
    int idx = num - 1;
    int off = (idx + 3) * 4;
    System.out.println("sw " + src + ", -" + off + "($sp)");
    return _ret;
}

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    */
   @Override
  public R visit(CallStmt n, A argu) {
    R _ret = null;
    Node tgt = n.f1.f0.choice;
    if (tgt instanceof Label) {
        String nm = ((Label) tgt).f0.toString();
        System.out.println("jal " + nm);
    } else if (tgt instanceof Reg) {
        String reg = (String) ((Reg) tgt).accept(this, argu);
        System.out.println("jalr " + reg);
    }
    return _ret;
}


   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   @Override
  public R visit(HAllocate n, A argu) {
    String dest = (String) argu;
    R _ret = null;
    Node size = n.f1.f0.choice;
    if (size instanceof Reg) {
        String src = (String) ((Reg) size).accept(this, argu);
        System.out.println("move $a0, " + src);
    }else if (size instanceof IntegerLiteral) {
        String val = ((IntegerLiteral) size).f0.toString();
        System.out.println("li $a0, " + val);
    } else if (size instanceof Label) {
        String lbl = ((Label) size).f0.toString();
        System.out.println("la $a0, " + lbl);
    }
    System.out.println("jal _halloc");
    System.out.println("move " + dest + ", $v0");
    return _ret;
}
   /**
    * f0 -> Operator()
    * f1 -> Reg()
    * f2 -> SimpleExp()
    */
   @Override
  public R visit(BinOp n, A argu) {
    R _ret = null;
    String dest = (String) argu;
    String reg1 = (String) n.f1.accept(this, argu);
    String reg2 = (String) n.f2.accept(this, argu);
    String op = n.f0.f0.choice.toString();
    String mipsOp;
    if(op == "PLUS"){
        mipsOp = "addu";
    } else if(op == "MINUS"){
        mipsOp = "subu";
    } else if(op == "TIMES"){
        mipsOp = "mul";
    } else if(op == "DIV"){
        mipsOp = "div";
    } else if(op == "LE"){
        mipsOp = "sle";
    } else if(op == "NE"){
        mipsOp = "sne";
    } else{     //invalid
        mipsOp = op;
    }
    
    System.out.println(mipsOp + " " + dest + ", " + reg1 + ", " + reg2);
    return _ret;
}


   /**
    * f0 -> "SPILLEDARG"
    * f1 -> IntegerLiteral()
    */
   @Override
  public R visit(SpilledArg n, A argu) {
    int slot = Integer.parseInt(n.f1.f0.toString());
    int off = (slot + 3) * 4;
    String loc = "-" + off + "($fp)";
    return (R) loc;
}
   /**
    * f0 -> Reg()
    *       | IntegerLiteral()
    *       | Label()
    */
   @Override
   public R visit(SimpleExp n, A argu) {
    R res = n.f0.accept(this, argu);
    return res;
}

   /**
    * f0 -> "a0"
    *       | "a1"
    *       | "a2"
    *       | "a3"
    *       | "t0"
    *       | "t1"
    *       | "t2"
    *       | "t3"
    *       | "t4"
    *       | "t5"
    *       | "t6"
    *       | "t7"
    *       | "s0"
    *       | "s1"
    *       | "s2"
    *       | "s3"
    *       | "s4"
    *       | "s5"
    *       | "s6"
    *       | "s7"
    *       | "t8"
    *       | "t9"
    *       | "v0"
    *       | "v1"
    */
   @Override
  public R visit(Reg n, A argu) {
    String nm = n.f0.choice.toString();
    String reg = "$" + nm;
    return (R) reg;
}

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   @Override
   public R visit(IntegerLiteral n, A argu) {
    String val = n.f0.toString();
    return (R) val;
}

   /**
    * f0 -> <IDENTIFIER>
    */
   @Override
  public R visit(Label n, A argu) {
    String lbl = n.f0.toString();
    return (R) lbl;
}


}
