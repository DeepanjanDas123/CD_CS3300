import syntaxtree.*;
import visitor.*;
import java.util.*;

public class P4 {
    public static void main(String[] args) throws Exception {
        Node root = new MiniIRParser(System.in).Goal();
        MiniToMicroVisitor vs = new MiniToMicroVisitor();
        root.accept(vs, null);
    }
}
