import syntaxtree.*;
import visitor.*;

public class P2 {
    public static void main(String[] args) {
        try {
            MiniJavaParser parser = new MiniJavaParser(System.in);
            Node root = parser.Goal();

            // First pass: build symbol tables
            SymbolTable symtab = new SymbolTable();
            FirstPassVisitor fp = new FirstPassVisitor(symtab);
            root.accept(fp, null);

            //Second pass: type check
            SecondPassVisitor sp = new SecondPassVisitor(symtab);
            root.accept(sp, null);

            System.out.println("Program type checked successfully");

        } 
        catch (RuntimeException e) {
            // if (e.getMessage().contains("Symbol not found")) {
            //     System.out.println("Symbol not found");
            // } else if (e.getMessage().contains("Type error")) {
            //     System.out.println("Type error");
            // } else {
            //     System.out.println("Type error"); // fallback
            // }
            System.out.println(e.getMessage());
        } catch (Exception e) {
            //System.out.println("Type error");
            System.out.println(e.getMessage());
        }
    }
}
