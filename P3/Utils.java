import syntaxtree.*;
import java.util.*;

/**
 * Lightweight helpers for JTB AST nodes used by the collector.
 */
public class Utils {
    /** extract identifier text from Identifier node (typical JTB shape) */
    public static String idText(Identifier id) {
        if (id == null) return "<null-identifier>";
        return id.f0.toString();
    }

    /** count parameters in FormalParameterList (if present) */
    public static int countFormalParams(NodeOptional f4) {
        if (f4 == null || !f4.present()) return 0;
        FormalParameterList fpl = (FormalParameterList) f4.node;
        int count = 1; // fpl.f0 is one FormalParameter
        NodeListOptional rest = fpl.f1;
        for (Enumeration<Node> e = rest.elements(); e.hasMoreElements(); ) {
            e.nextElement();
            count++;
        }
        return count;
    }
}
