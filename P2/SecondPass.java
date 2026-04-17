package visitor;
import syntaxtree.*;
import java.util.*;
import helperclasses.*;

public class SecondPass<R,A> extends GJDepthFirst<R,A>{
    private Symbol_table st;
    private Class_ currclass = null;
    private Method currmethod = null;
    public SecondPass(Symbol_table st){this.st=st;}
    private ArrayList<String> args;
    private boolean use_function=false;
    private String expfunc=null;         
    private Map<String,String> lams=new HashMap<>();

    void SymbolError(){
        System.out.println("Symbol not found"); 
        System.exit(0);
    }
    void TypeError(){
        System.out.println("Type error"); 
        System.exit(0);
    }

    private String lookupVar(String id){
        if (currmethod!=null){
            String t=currmethod.find_var(id);
            if (t!=null) return t;
        }
        String ta=lams.get(id);    
        if (ta!=null) return ta;
        String t=st.findvar(currclass.name,id);
        if (t!=null) return t;
        SymbolError();
        return "";
    }
    private String bs(String t) {
        if (t == null) return null;
        if ("Integer".equals(t)) return "int";
        if ("Boolean".equals(t)) return "bool";
        return t;
    }
    private boolean isvalidtype(String t) {
      if (t == null) return false;
      if ("int".equals(t) || "bool".equals(t) || "int_array".equals(t)) return true;
      if (t.startsWith("Function<") && t.endsWith(">")) {
         int lt=t.indexOf('<');
         int gt=t.lastIndexOf('>');
         if (lt<0 || gt<0 || lt>=gt) return false;
         String i=t.substring(lt+1,gt);
         int comma=i.indexOf(',');
         if (comma<0) return false;
         String A=i.substring(0, comma).trim();
         String B=i.substring(comma + 1).trim();
         A=bs(A);
         B=bs(B);
         boolean okA = "int".equals(A) || "bool".equals(A) || st.classes.containsKey(A);
         boolean okB = "int".equals(B) || "bool".equals(B) || st.classes.containsKey(B);
         return okA && okB;
      }
      return st.classes.containsKey(t);
    }

    private boolean isAssignable(String want,String got){
        want=bs(want);
        got=bs(got);
        if (want == null || got == null) return false;
        if (want.equals(got)) return true;
        if (want.endsWith("_array") || got.endsWith("_array")) return false;
        return st.isSubtype(got,want);
    }
    private boolean islamb(String t){ 
        return t.startsWith("Function<") && t.endsWith(">"); 
    }

   public R visit(NodeToken n, A argu) { return null; }

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> ( ImportFunction() )?
    * f1 -> MainClass()
    * f2 -> ( TypeDeclaration() )*
    * f3 -> <EOF>
    */
   public R visit(Goal n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "import"
    * f1 -> "java.util.function.Function"
    * f2 -> ";"
    */
   public R visit(ImportFunction n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      use_function=true;
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> PrintStatement()
    * f15 -> "}"
    * f16 -> "}"
    */
   public R visit(MainClass n, A argu) { 
      R _ret=null;
      currclass = st.getclass(n.f1.f0.toString());
      n.f0.accept(this, argu);
    //   n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      n.f9.accept(this, argu);
      n.f10.accept(this, argu);
    //   n.f11.accept(this, argu);
      n.f12.accept(this, argu);
      n.f13.accept(this, argu);
      n.f14.accept(this, argu);
      n.f15.accept(this, argu);
      n.f16.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public R visit(TypeDeclaration n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public R visit(ClassDeclaration n, A argu) { 
      R _ret=null;
      currclass =st.getclass(n.f1.f0.toString());
      for (Map.Entry<String,String> e:currclass.locals.entrySet()){
         if (!isvalidtype(e.getValue())) SymbolError();
      }
      n.f0.accept(this, argu);
    //   n.f1.accept(this, argu);
      n.f2.accept(this, argu);
    //   n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public R visit(ClassExtendsDeclaration n, A argu) { 
      R _ret=null;  
      currclass =st.getclass(n.f1.f0.toString());
      for (Map.Entry<String,String> e:currclass.locals.entrySet()){
         if (!isvalidtype(e.getValue())) SymbolError();
      }
      String parent = n.f3.f0.toString();
      if (st.getclass(parent) == null) SymbolError();
      n.f0.accept(this, argu);
    //   n.f1.accept(this, argu);
      n.f2.accept(this, argu);
    //   n.f3.accept(this, argu);
      n.f4.accept(this, argu);
    //   n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public R visit(VarDeclaration n, A argu) { 
      R _ret=null;
      String type = bs((String) n.f0.accept(this, argu));
      if (!isvalidtype(type)) SymbolError();
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   public R visit(MethodDeclaration n, A argu) { //ooof
      R _ret=null;
      String mname= n.f2.f0.toString();
      n.f0.accept(this, argu);
    //   n.f1.accept(this, argu);
    //   n.f2.accept(this, argu);
      n.f3.accept(this, argu);
    //   n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      Method found=currclass.methods.get(mname);
      if (found==null){
        for (Class_ c=st.findpar(currclass);c!=null && found==null;c=st.findpar(c)) {
            found = c.methods.get(mname);
        }
      }
      if (found==null) SymbolError();
         currmethod=found;
      if (!isvalidtype(currmethod.ret_type)) SymbolError();

      for (Map.Entry<String,String> e:currmethod.params.entrySet()) {
         if (!isvalidtype(e.getValue())) SymbolError();
      }
      for (Map.Entry<String,String> e:currmethod.localvars.entrySet()) {
         if (!isvalidtype(e.getValue())) SymbolError();
      }
    //   n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      n.f9.accept(this, argu);
      String previ=expfunc;                       
      if (currmethod.ret_type.startsWith("Function<"))                
        expfunc=currmethod.ret_type;                  
      String rt=(String)n.f10.accept(this, argu);
      expfunc=previ;                        
      rt = bs(rt);      
      if (!( "int_array".equals(rt) ||"int".equals(rt) || "bool".equals(rt) || rt.startsWith("Function<")|| st.classes.containsKey(rt))) {
         TypeError();
      }
      if (!isAssignable(currmethod.ret_type, rt)) TypeError();
      
      n.f11.accept(this, argu);
      n.f12.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public R visit(FormalParameterList n, A argu) { //work
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public R visit(FormalParameter n, A argu) { //work
      R _ret=null;
      
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public R visit(FormalParameterRest n, A argu) { //work
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    *       | LambdaType()
    */
   public R visit(Type n, A argu) {
      R _ret=n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public R visit(ArrayType n, A argu) {
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return (R)"int_array";
   }

   /**
    * f0 -> "boolean"
    */
   public R visit(BooleanType n, A argu) {
      n.f0.accept(this, argu);
      return (R)"bool";
   }

   /**
    * f0 -> "int"
    */
   public R visit(IntegerType n, A argu) {
      n.f0.accept(this, argu);
      return (R)"int";
   }

   /**
    * f0 -> "Function"
    * f1 -> "<"
    * f2 -> Identifier()
    * f3 -> ","
    * f4 -> Identifier()
    * f5 -> ">"
    */
   public R visit(LambdaType n, A argu) {    
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String A1 = n.f2.f0.toString();
      n.f3.accept(this, argu);
      String A2 = n.f4.f0.toString();
      n.f5.accept(this, argu);
      if (!isvalidtype(A1) || !isvalidtype(A2)) SymbolError();
      return (R)("Function<" + A1 + "," + A2 + ">");      
   }

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
   public R visit(Statement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public R visit(Block n, A argu) {   
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public R visit(AssignmentStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      String lhs=lookupVar(n.f0.f0.toString());
      if (lhs==null) SymbolError();
      n.f1.accept(this, argu);
      String prev=expfunc;      
      if (lhs.startsWith("Function<")) expfunc=lhs;
      String rhs=(String)n.f2.accept(this,argu);
      expfunc=prev;
      if (!isAssignable(lhs,rhs)) TypeError();
      n.f3.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public R visit(ArrayAssignmentStatement n, A argu) {     
      R _ret=null;
      n.f0.accept(this, argu);

      String arr=lookupVar(n.f0.f0.toString()); 
      if (arr==null) SymbolError();
      if (!"int_array".equals(arr)) TypeError();

      n.f1.accept(this, argu);
      String int1=(String)n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      String int2=(String)n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      if (!"int".equals(int1) || !"int".equals(int2)) TypeError();
      return _ret;
   }

   /**
    * f0 -> IfthenElseStatement()
    *       | IfthenStatement()
    */
   public R visit(IfStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public R visit(IfthenStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String bool=(String)n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      if (!bool.equals("bool")) TypeError(); 
      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public R visit(IfthenElseStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String bool=(String)n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      if (!bool.equals("bool")) TypeError(); 
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public R visit(WhileStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String bool=(String) n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      if (!"bool".equals(bool)) TypeError();
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public R visit(PrintStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String t=(String)n.f2.accept(this, argu);
      if (!"int".equals(t)) TypeError();
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> OrExpression()
    *       | AndExpression()
    *       | CompareExpression()
    *       | neqExpression()
    *       | AddExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | DivExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | LambdaExpression()
    *       | PrimaryExpression()
    */
   public R visit(Expression n, A argu) {
      R _ret=n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "("
    * f1 -> Identifier()
    * f2 -> ")"
    * f3 -> "->"
    * f4 -> Expression()
    */
   public R visit(LambdaExpression n, A argu) {  
      n.f0.accept(this, argu);
    //   n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
    //   n.f4.accept(this, argu); //vvese
      if (expfunc==null || !islamb(expfunc)) TypeError();
      int lt=expfunc.indexOf('<');
      int gt=expfunc.indexOf('>');
      String inside=expfunc.substring(lt+1, gt);
      int comma=inside.indexOf(',');
      String A=inside.substring(0, comma).trim();
      String B=inside.substring(comma+1).trim();

      String pname=n.f1.f0.toString();
      String old=lams.put(pname,A);
      String bodyt=(String)n.f4.accept(this, argu);
      if (old == null) lams.remove(pname); else lams.put(pname, old);

      if (!isAssignable(B,bodyt)) TypeError();
      return (R)expfunc;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
   public R visit(AndExpression n, A argu) {
      String bool1=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String bool2=(String)n.f2.accept(this, argu);
      if (!"bool".equals(bool1) || !"bool".equals(bool2)) TypeError();
      return (R)"bool";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "||"
    * f2 -> PrimaryExpression()
    */
   public R visit(OrExpression n, A argu) {
      String bool1=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String bool2=(String)n.f2.accept(this, argu);
      if (!"bool".equals(bool1) || !"bool".equals(bool2)) TypeError();
      return (R)"bool";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<="
    * f2 -> PrimaryExpression()
    */
   public R visit(CompareExpression n, A argu) {
      String int1=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String int2=(String)n.f2.accept(this, argu);
      if (!"int".equals(int1) || !"int".equals(int2)) TypeError();
      return (R)"bool";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "!="
    * f2 -> PrimaryExpression()
    */
   public R visit(neqExpression n, A argu) { //both types should be same and if identifier ,one should be parent of other
      String x=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String y=(String)n.f2.accept(this, argu);
      boolean primX = "int".equals(x) || "bool".equals(x) || "int_array".equals(x);
      boolean primY = "int".equals(y) || "bool".equals(y) || "int_array".equals(y);
      if (primX || primY){
          if (!x.equals(y)) TypeError();
      } else {
          if (!(x.equals(y) || st.isSubtype(x,y) || st.isSubtype(y,x))) TypeError();
      }
      return (R)"bool";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public R visit(AddExpression n, A argu) {
      String int1=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String int2=(String)n.f2.accept(this, argu);
      if (!"int".equals(int1) || !"int".equals(int2)) TypeError();
      return (R)"int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public R visit(MinusExpression n, A argu) {
      String int1=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String int2=(String)n.f2.accept(this, argu);
      if (!"int".equals(int1) || !"int".equals(int2)) TypeError();
      return (R)"int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public R visit(TimesExpression n, A argu) {
      String int1=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String int2=(String)n.f2.accept(this, argu);
      if (!"int".equals(int1) || !"int".equals(int2)) TypeError();
      return (R)"int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "/"
    * f2 -> PrimaryExpression()
    */
   public R visit(DivExpression n, A argu) {
      String int1=(String)n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String int2=(String)n.f2.accept(this, argu);
      if (!"int".equals(int1) || !"int".equals(int2)) TypeError();
      return (R)"int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public R visit(ArrayLookup n, A argu) { //work same as below
      String arr=(String) n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String intype=(String) n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      if (!"int_array".equals(arr) || !"int".equals(intype)) TypeError();
      return (R)"int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public R visit(ArrayLength n, A argu) { //work a bit
      R _ret=null;
      String arr=(String)n.f0.accept(this, argu);  
      n.f1.accept(this, argu);
      if (!"int_array".equals(arr)) TypeError();
      n.f2.accept(this, argu);
      return (R)"int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public R visit(MessageSend n, A argu) {
      String smthg = (String) n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String mname = n.f2.f0.toString();
      if (islamb(smthg) && !use_function) SymbolError();
      if ("int".equals(smthg) || "bool".equals(smthg) ) TypeError();
      if (islamb(smthg)) {
         if (!"apply".equals(mname)) SymbolError();
         String t = smthg;
         int lt = t.indexOf('<'), gt = t.lastIndexOf('>');
         if (lt < 0 || gt <= lt + 1) SymbolError();
         String inside = t.substring(lt + 1, gt);
         int k = inside.indexOf(',');
         if (k < 0) SymbolError();
         String A = inside.substring(0, k).trim();
         String B = inside.substring(k + 1).trim();

         ArrayList<String> prevargs = args;
         String prevExp = expfunc;
         args = new ArrayList<>();
         expfunc = A;                 
         n.f3.accept(this, argu);
         n.f4.accept(this, argu);
         n.f5.accept(this, argu);
         expfunc = prevExp;
         ArrayList<String> argTypes = new ArrayList<>(args);
         args = prevargs;

         if (argTypes.size() != 1) TypeError();
         if (!isAssignable(A, argTypes.get(0))) TypeError();
         return (R) B;
      }
      if (!st.classes.containsKey(smthg)) SymbolError();

      Method decl = null;
      for (Class_ c = st.getclass(smthg); c != null && decl == null; c = st.findpar(c)) {
         decl = c.methods.get(mname);
      }
      if (decl == null) SymbolError();
      java.util.List<String> ps = decl.paramlst();

      ArrayList<String> argTypes = new ArrayList<>();
      String saveExp = expfunc;

      n.f3.accept(this, argu);
      if (n.f4.present()) {
         syntaxtree.ExpressionList el = (syntaxtree.ExpressionList) n.f4.node;
         if (!ps.isEmpty() && islamb(ps.get(0))) expfunc = ps.get(0);
         argTypes.add((String) el.f0.accept(this, argu));
         int idx = 1;
         for (java.util.Enumeration<syntaxtree.Node> e = el.f1.elements(); e.hasMoreElements(); idx++) {
               syntaxtree.ExpressionRest er = (syntaxtree.ExpressionRest) e.nextElement();
               if (idx < ps.size() && islamb(ps.get(idx))) expfunc = ps.get(idx);
               else expfunc = null;
               argTypes.add((String) er.f1.accept(this, argu));
         }
      }
      expfunc = saveExp;
      n.f5.accept(this, argu);

      Method m = st.getfun(smthg, mname, argTypes);
      if (m == null) SymbolError();
      java.util.List<String> ps2 = m.paramlst();
      if (ps2.size() != argTypes.size()) TypeError();
      for (int i = 0; i < ps2.size(); i++)
         if (!isAssignable(ps2.get(i), argTypes.get(i))) TypeError();
      return (R) m.ret_type;
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public R visit(ExpressionList n, A argu) { //work
      R _ret=null;
      args.add(bs((String)n.f0.accept(this, argu)));
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public R visit(ExpressionRest n, A argu) {  //work
      R _ret=null;
      n.f0.accept(this, argu);
      args.add((String)n.f1.accept(this, argu));
      return _ret;
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
   public R visit(PrimaryExpression n, A argu) {
      R _ret=n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public R visit(IntegerLiteral n, A argu) {
      n.f0.accept(this, argu);
      return (R)"int";
   }

   /**
    * f0 -> "true"
    */
   public R visit(TrueLiteral n, A argu) {
      n.f0.accept(this, argu);
      return (R)"bool";
   }

   /**
    * f0 -> "false"
    */
   public R visit(FalseLiteral n, A argu) {
      n.f0.accept(this, argu);
      return (R)"bool";
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public R visit(Identifier n, A argu) {  //work-ARGUEMENT USED
      n.f0.accept(this, argu);
      String t=lookupVar(n.f0.toString());
      if (t==null) SymbolError();
      return (R)bs(t);
   }

   /**
    * f0 -> "this"
    */
   public R visit(ThisExpression n, A argu) {  //WORK
      R _ret=null;
      n.f0.accept(this, argu);
      return (R)currclass.name;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public R visit(ArrayAllocationExpression n, A argu) {
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      String intype=bs((String)n.f3.accept(this, argu));
      if (!"int".equals(intype)) TypeError();
      n.f4.accept(this, argu);
      return (R)"int_array";
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public R visit(AllocationExpression n, A argu) {     //WORK
      R _ret=null;
      n.f0.accept(this, argu);
    //   n.f1.accept(this, argu);
      String cname=n.f1.f0.toString();
      if ("Function".equals(cname) && !use_function) SymbolError(); 
      // cname=bs(cname);
      if (!cname.equals("Integer") && !cname.equals("Boolean") && st.getclass(cname)==null) SymbolError();
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      return (R)cname;
   }

   /** 
    * f0 -> "!"
    * f1 -> Expression()
    */
   public R visit(NotExpression n, A argu) {
      n.f0.accept(this, argu);
      String type=bs((String)n.f1.accept(this, argu));
      if (!"bool".equals(type))  TypeError();
      return ((R)"bool");
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public R visit(BracketExpression n, A argu) {
      n.f0.accept(this, argu);
      R _ret=n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }
}