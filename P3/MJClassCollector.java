import java.util.*;
import syntaxtree.*;
import visitor.*;

/**
 * Updated collector stores Field types and Method return types.
 * Uses the same traversal strategy as earlier, but when encountering VarDeclaration at class scope
 * we store the type name as well.
 */
public class MJClassCollector extends GJDepthFirst<Void, Void> {
    private final LinkedHashMap<String, ClassInfo> classTable = new LinkedHashMap<>();
    private ClassInfo currentClass = null;
    private boolean insideMethod = false;

    public MJClassCollector() {}

    public Map<String, ClassInfo> getClassTable() { return Collections.unmodifiableMap(classTable); }

    @Override
    public Void visit(Goal n, Void argu) {
        super.visit(n, argu);
        computeLayouts();
        return null;
    }

    @Override
    public Void visit(MainClass n, Void argu) {
        String name = n.f1.f0.toString();
        currentClass = new ClassInfo(name, null);
        classTable.put(name, currentClass);
        MethodInfo mainMethod = new MethodInfo("main", ClassInfo.methodLabel(name, "main"), 1, "void");
        currentClass.addMethod(mainMethod);
        super.visit(n, argu);
        currentClass = null;
        return null;
    }

    @Override
    public Void visit(ClassDeclaration n, Void argu) {
        String name = n.f1.f0.toString();
        currentClass = new ClassInfo(name, null);
        classTable.put(name, currentClass);
        super.visit(n, argu);
        currentClass = null;
        return null;
    }

    @Override
    public Void visit(ClassExtendsDeclaration n, Void argu) {
        String name = n.f1.f0.toString();
        String parent = n.f3.f0.toString();
        currentClass = new ClassInfo(name, parent);
        classTable.put(name, currentClass);
        super.visit(n, argu);
        currentClass = null;
        return null;
    }

    // VarDeclaration: Type Identifier ;
    @Override
    public Void visit(VarDeclaration n, Void argu) {
        if (currentClass != null && !insideMethod) {
            // class-level field
            String typeName = typeToString(n.f0);
            String fname = n.f1.f0.toString();
            currentClass.addField(new FieldInfo(fname, typeName));
        }
        return null;
    }

    @Override
    public Void visit(MethodDeclaration n, Void argu) {
        if (currentClass == null) return null;
        String methodName = n.f2.f0.toString();
        String returnType = typeToString(n.f1);
        int paramCount = 0;
        if (n.f4.present()) {
            FormalParameterList fpl = (FormalParameterList) n.f4.node;
            paramCount = 1; // at least one
            NodeListOptional rest = fpl.f1;
            for (Enumeration<Node> e = rest.elements(); e.hasMoreElements();) { e.nextElement(); paramCount++; }
        } else {
            paramCount = 0;
        }
        int irArgCount = 1 + paramCount;
        MethodInfo mi = new MethodInfo(methodName, ClassInfo.methodLabel(currentClass.getName(), methodName), irArgCount, returnType);
        currentClass.addMethod(mi);

        boolean prev = insideMethod;
        insideMethod = true;
        super.visit(n, argu);
        insideMethod = prev;
        return null;
    }

    private String typeToString(Node tnode) {
        if (tnode == null) return "void";
        if (tnode instanceof ArrayType) return "int[]";
        if (tnode instanceof IntegerType) return "int";
        if (tnode instanceof BooleanType) return "boolean";
        if (tnode instanceof LambdaType) return "Function";
        if (tnode instanceof Identifier) return ((Identifier)tnode).f0.toString();
        // some types appear wrapped as nodes with f0 child (e.g., in FormalParameterList)
        try {
            // fallback to toString
            return tnode.toString().trim();
        } catch (Exception e) { return "unknown"; }
    }

    // After traversal compute layouts
    private void computeLayouts() {
        List<String> topo = topoSortClasses();
        for (String cname : topo) {
            ClassInfo ci = classTable.get(cname);
            if (ci.getParentName() == null) {
                ci.setAllFieldsFromDeclared();
                ci.computeFieldOffsetsFromAll();
                ci.computeMethodSlotsFromDeclared();
            } else {
                ClassInfo parent = classTable.get(ci.getParentName());
                if (parent == null) {
                    ci.setAllFieldsFromDeclared();
                    ci.computeFieldOffsetsFromAll();
                    ci.computeMethodSlotsFromDeclared();
                } else {
                    ci.inheritFrom(parent);
                    ci.computeFieldOffsetsFromAll();
                    ci.computeMethodSlotsFromDeclared();
                }
            }
        }
    }

    private List<String> topoSortClasses() {
        List<String> res = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String c : classTable.keySet()) dfsTopo(c, seen, res);
        return res;
    }

    private void dfsTopo(String c, Set<String> seen, List<String> out) {
        if (seen.contains(c)) return;
        seen.add(c);
        ClassInfo ci = classTable.get(c);
        if (ci != null && ci.getParentName() != null && classTable.containsKey(ci.getParentName())) {
            dfsTopo(ci.getParentName(), seen, out);
        }
        out.add(c);
    }
}
