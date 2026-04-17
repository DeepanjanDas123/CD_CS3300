public class MethodInfo {
    private final String name;
    private final String label;
    private final int argCount;
    private final String returnType; // e.g., "int", "boolean", "SomeClass", "int[]", "void"
    private int slotIndex = -1;

    public MethodInfo(String name, String label, int argCount, String returnType) {
        this.name = name; this.label = label; this.argCount = argCount; this.returnType = returnType;
    }

    public String getName() { return name; }
    public String getLabel() { return label; }
    public int getArgCount() { return argCount; }
    public String getReturnType() { return returnType; }

    public void setSlotIndex(int i) { this.slotIndex = i; }
    public int getSlotIndex() { return slotIndex; }
}
