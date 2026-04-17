package visitor;

import java.util.*;

public class MethodInfo {
    public final String name;
    public final String returnType;
    public final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
    public final HashMap<String, String> locals = new HashMap<>();

    public MethodInfo(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public void addParam(String param, String type) {
        if (parameters.containsKey(param)) {
            ErrorHandler.report("Type error");
        } else {
            parameters.put(param, type);
        }
    }

    public void addLocal(String local, String type) {
        if (locals.containsKey(local) || parameters.containsKey(local)) {
            ErrorHandler.report("Type error");
        } else {
            locals.put(local, type);
        }
    }

    public String lookupVar(String name) {
        if (locals.containsKey(name)) return locals.get(name);
        if (parameters.containsKey(name)) return parameters.get(name);
        return null; // Not found
    }

    public List<String> getParamTypes() {
        return new ArrayList<>(parameters.values());
    }
}
