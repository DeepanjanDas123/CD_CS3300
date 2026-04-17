package visitor;

import java.util.*;

public class ClassInfo {
    public final String name;
    public final String parent;
    public final HashMap<String, String> fields = new HashMap<>();
    public final HashMap<String, MethodInfo> methods = new HashMap<>();

    public ClassInfo(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }

    public void addField(String field, String type) {
        if (fields.containsKey(field)) {
            ErrorHandler.report("Type error");
        }

        fields.put(field, type);
    }

    public void addMethod(MethodInfo method, SymbolTable table) {
        // Check duplicate in current class
        if (methods.containsKey(method.name)) {
            ErrorHandler.report("Type error");
        }

        methods.put(method.name, method);
    }

    public void checkMethod(MethodInfo method, SymbolTable table) {
        // Check overriding rules
        ClassInfo ancestor = parent != null ? table.getClass(parent) : null;
        while (ancestor != null) {
            MethodInfo m = ancestor.methods.get(method.name);
            if (m != null) {
                // signature check
                if(!sameParamTypes(m.getParamTypes(), method.getParamTypes())){
                    ErrorHandler.report("Type error");
                }
                else{       //same signature, now check for illegal overriding
                    if(!TypeUtils.isAssignable(method.returnType, m.returnType, table)){
                        ErrorHandler.report("Type error");
                    }
                }
            }
            ancestor = ancestor.parent != null ? table.getClass(ancestor.parent) : null;
        }
    }

    public String lookupField(String fieldName, SymbolTable table) {
        if (fields.containsKey(fieldName)) return fields.get(fieldName);
        // lookup in parent
        if (parent != null) {
            ClassInfo p = table.getClass(parent);
            if (p != null) return p.lookupField(fieldName, table);
        }
        return null;
    }

    public MethodInfo lookupMethod(String methodName, SymbolTable table) {
        if (methods.containsKey(methodName)) return methods.get(methodName);
        if (parent != null) {
            ClassInfo p = table.getClass(parent);
            if (p != null) return p.lookupMethod(methodName, table);
        }
        return null;
    }

    private boolean sameParamTypes(List<String> p1, List<String> p2) {
        if (p1.size() != p2.size()) return false;
        for (int i = 0; i < p1.size(); i++)
            if (!p1.get(i).equals(p2.get(i))) return false;
        return true;
    }
}
