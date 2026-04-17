import java.util.*;

/**
 * Env maps variable name -> temp number and type name.
 */
public class Env {
    private final Map<String,Integer> varToTemp = new HashMap<>();
    private final Map<String,String> varToType = new HashMap<>();
    private int nextLocalTemp;
    public final String currentClassName;
    public final String currentMethodName;

    public Env(String className, String methodName, int startingTemp) {
        this.currentClassName = className;
        this.currentMethodName = methodName;
        this.nextLocalTemp = startingTemp;
    }

    public int allocLocal() { return nextLocalTemp++; }

    public void addVar(String name, int temp, String typeName) {
        varToTemp.put(name, temp);
        varToType.put(name, typeName);
    }

    public void addVar(String name, int temp) {
        addVar(name, temp, "int"); // default int if not given
    }

    public Integer lookup(String name) { return varToTemp.get(name); }
    public String lookupType(String name) { return varToType.get(name); }
}
