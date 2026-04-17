public class FieldInfo {
    private final String name;
    private final String typeName; // e.g., "int", "boolean", "SomeClass", "int[]"
    private int offset = -1;
    public FieldInfo(String name, String typeName) { this.name = name; this.typeName = typeName; }
    public String getName() { return name; }
    public String getTypeName() { return typeName; }
    public void setOffset(int off) { this.offset = off; }
    public int getOffset() { return offset; }
}
