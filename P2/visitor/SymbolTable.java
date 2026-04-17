package visitor;

import java.util.*;

public class SymbolTable {
    private final HashMap<String, ClassInfo> classes = new HashMap<>();

    public String currentClass = null;
    public String currentMethod = null;

    public boolean hasFunctionImport = false;

    public void addClass(String name, String parent) {
        if (classes.containsKey(name)) {
            ErrorHandler.report("Type error");       //first pass error, TYPE
        }
        classes.put(name, new ClassInfo(name, parent));
    }

    public ClassInfo getClass(String name) {
        return classes.get(name);
    }

    public boolean classExists(String name) {
        return classes.containsKey(name);
    }

    public void addFieldToCurrentClass(String field, String type) {
        ClassInfo c = classes.get(currentClass);
        if (c == null) ErrorHandler.report("Symbol not found");
        c.addField(field, type);
    }

    public void addMethodToCurrentClass(MethodInfo method) {
        ClassInfo c = classes.get(currentClass);
        if (c == null) ErrorHandler.report("Symbol not found");
        c.addMethod(method, this);
    }

    public String lookupVar(String name) {
        if (currentMethod != null) {
            MethodInfo m = classes.get(currentClass).methods.get(currentMethod);
            String t = m.lookupVar(name);
            if (t != null) return t;
        }
        // Check class fields and parent fields
        return classes.get(currentClass).lookupField(name, this);
    }

    public MethodInfo lookupMethod(String methodName) {
        return classes.get(currentClass).lookupMethod(methodName, this);
    }

    public void validateInheritance() {
        // 1. Check parent existence + detect cycles
        for (ClassInfo c : classes.values()) {
            if (c.parent != null && !classExists(c.parent)) {
                ErrorHandler.report("Symbol not found");
            }
            detectCycle(c);
        }

        // 2. Method overriding/overloading checks
        for (ClassInfo c : classes.values()) {
            for (MethodInfo m : c.methods.values()) {
                c.checkMethod(m, this);
            }
        }
    }

    // cycle detection helper
    private void detectCycle(ClassInfo c) {
        Set<String> visited = new HashSet<>();
        String cur = c.name;
        while (cur != null) {
            if (visited.contains(cur)) {
                ErrorHandler.report("Type error");
                return;
            }
            visited.add(cur);
            ClassInfo next = classes.get(cur);
            cur = (next != null) ? next.parent : null;
        }
    }
}
