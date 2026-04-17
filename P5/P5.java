import syntaxtree.*;
import visitor.*;
public class P5 {
   public static void main(String [] args) {
      try {
         Node root = new microIRParser(System.in).Goal();
         FirstPassVisitor pass1 = new FirstPassVisitor();
         root.accept(pass1);
         SecondPassVisitor<Void, Void> pass2 = new SecondPassVisitor<>(pass1.lblMap);
         root.accept(pass2, null);
         ThirdPassVisitor<Void, Void> pass3 = new ThirdPassVisitor<>(pass2.cfgs);
         root.accept(pass3, null);

      }
      catch (ParseException e) {
         System.err.println("Error generated while parsing input microIR: " + e.toString());
      }
      catch (Exception e) {
         System.err.println("ERROR:");
      }
   }
}