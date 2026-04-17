import java.util.*;

/**
 * ClassInfo - holds declared fields/methods and computes inherited layouts.
 *
 * Important invariants:
 *  - allFields: parent fields first (inherited), then this class's declared fields.
 *  - field offsets start at 4 (vptr at 0), each field occupies 4 bytes.
 *  - methodSlots: LinkedHashMap mapping method name -> MethodInfo. Parent slots preserved,
 *    overridden methods replace the MethodInfo at the same slot index, new methods appended.
 */
public class ClassInfo {
    private final String name;
    private final String parentName; // null if none

    // Declared in this class only:
    private final List<FieldInfo> declaredFields = new ArrayList<>();
    private final List<MethodInfo> declaredMethods = new ArrayList<>();

    // Final combined lists (after inheritance/layout computation):
    private final List<FieldInfo> allFields = new ArrayList<>();
    // methodSlots preserves slot ordering. Key by method name.
    private final LinkedHashMap<String, MethodInfo> methodSlots = new LinkedHashMap<>();

    private int objectSize = 4; // at least vptr
    private int vtableSize = 0;

    public ClassInfo(String name, String parentName) {
        this.name = name;
        this.parentName = parentName;
    }

    public String getName() { return name; }
    public String getParentName() { return parentName; }

    // Add a declared field (used during collection)
    public void addField(FieldInfo f) {
        declaredFields.add(f);
    }

    // Add a declared method (used during collection)
    public void addMethod(MethodInfo m) {
        declaredMethods.add(m);
    }

    /**
     * Add a declared method to this class info.
     * This tries to append to the existing declaredMethods list and methodSlots map
     * without reassigning final fields. If the underlying collections are immutable,
     * it attempts a reflective replacement as a fallback.
     */
    public void addDeclaredMethod(MethodInfo mi) {
        if (mi == null) return;

        // 1) Append to declaredMethods if possible
        try {
            if (this.declaredMethods != null) {
                this.declaredMethods.add(mi); // works when list is mutable
            }
        } catch (UnsupportedOperationException uoe) {
            // declaredMethods is unmodifiable; try reflective replacement of the field
            try {
                java.lang.reflect.Field f = this.getClass().getDeclaredField("declaredMethods");
                f.setAccessible(true);
                java.util.List<MethodInfo> newList = new java.util.ArrayList<MethodInfo>();
                if (this.declaredMethods != null) newList.addAll(this.declaredMethods);
                newList.add(mi);
                // Remove final-check by reflection (still works), then set
                f.set(this, java.util.Collections.unmodifiableList(newList));
            } catch (Throwable ignore) {
                // give up; not fatal
            }
        } catch (Throwable ignore) {
            // ignore other problems, continue
        }

        // 2) Put into methodSlots map if possible
        try {
            if (this.methodSlots != null) {
                // If a method with same name already exists, preserve its slot index
                if (this.methodSlots.containsKey(mi.getName())) {
                    MethodInfo old = this.methodSlots.get(mi.getName());
                    int slotIndex = -1;
                    try { slotIndex = old.getSlotIndex(); } catch (Throwable t) { slotIndex = -1; }
                    if (slotIndex >= 0) {
                        try {
                            mi.setSlotIndex(slotIndex);
                        } catch (Throwable t) {
                            // try reflection fallback for setting slot
                            try {
                                java.lang.reflect.Method sm = mi.getClass().getMethod("setSlotIndex", int.class);
                                sm.invoke(mi, slotIndex);
                            } catch (Throwable tt) { /* ignore */ }
                        }
                    }
                } else {
                    // assign next available slot index = current size
                    int nextSlot = this.methodSlots.size();
                    try {
                        mi.setSlotIndex(nextSlot);
                    } catch (Throwable t) {
                        try {
                            java.lang.reflect.Method sm = mi.getClass().getMethod("setSlotIndex", int.class);
                            sm.invoke(mi, nextSlot);
                        } catch (Throwable tt) { /* ignore */ }
                    }
                }
                this.methodSlots.put(mi.getName(), mi);
            }
        } catch (UnsupportedOperationException uoe) {
            // methodSlots unmodifiable — try reflective replacement
            try {
                java.lang.reflect.Field f = this.getClass().getDeclaredField("methodSlots");
                f.setAccessible(true);
                java.util.Map<String, MethodInfo> newMap = new java.util.LinkedHashMap<>();
                if (this.methodSlots != null) newMap.putAll(this.methodSlots);
                // assign slot if missing
                if (!newMap.containsKey(mi.getName())) {
                    int nextSlot = newMap.size();
                    try { mi.setSlotIndex(nextSlot); } catch (Throwable t) { /* ignore */ }
                } else {
                    MethodInfo old = newMap.get(mi.getName());
                    try { mi.setSlotIndex(old.getSlotIndex()); } catch (Throwable t) { /* ignore */ }
                }
                newMap.put(mi.getName(), mi);
                // set an unmodifiable view to preserve semantics if original was unmodifiable
                f.set(this, java.util.Collections.unmodifiableMap(newMap));
            } catch (Throwable ignore) {
                // give up
            }
        } catch (Throwable ignore) {
            // ignore
        }

        // 3) Update vtableSize — try setter if exists, else set field reflectively if exists
        try {
            int vtBytes = (this.methodSlots == null) ? 0 : (this.methodSlots.size() * 4);
            try {
                java.lang.reflect.Method ms = this.getClass().getMethod("setVtableSize", int.class);
                ms.invoke(this, vtBytes);
            } catch (NoSuchMethodException nsme) {
                try {
                    java.lang.reflect.Field f = this.getClass().getDeclaredField("vtableSize");
                    f.setAccessible(true);
                    f.setInt(this, vtBytes);
                } catch (Throwable ignore) { /* not critical */ }
            }
        } catch (Throwable ignore) { }
    }


    public List<FieldInfo> getDeclaredFields() { return Collections.unmodifiableList(declaredFields); }
    public List<MethodInfo> getDeclaredMethods() { return Collections.unmodifiableList(declaredMethods); }

    // Called for root classes (no inheritance) before computing offsets
    public void setAllFieldsFromDeclared() {
        allFields.clear();
        allFields.addAll(declaredFields);
    }

    // Called when inheriting from a parent: parent's allFields must be appended first.
    // This preserves parent field ordering and ensures offsets are assigned parent-first.
    public void inheritFrom(ClassInfo parent) {
        allFields.clear();
        // copy parent's combined field list first
        if (parent != null) {
            for (FieldInfo pf : parent.getAllFields()) {
                // create a new FieldInfo instance or reuse? reuse is ok because offsets will be set below.
                allFields.add(pf);
            }
            // copy parent's methodSlots first (preserve parent's slot indices/order)
            methodSlots.clear();
            methodSlots.putAll(parent.getMethodSlots());
        } else {
            methodSlots.clear();
        }
        // then append this class's declared fields (child fields come after inherited ones)
        allFields.addAll(declaredFields);
        // note: declaredMethods are not added to methodSlots here; handled in computeMethodSlotsFromDeclared()
    }

    // After allFields is built, compute offsets for each field and objectSize.
    public void computeFieldOffsetsFromAll() {
        int off = 4; // vptr at offset 0; fields start at offset 4
        for (FieldInfo f : allFields) {
            f.setOffset(off);
            off += 4;
        }
        // object size is the next free byte (we use bytes, 4 per field)
        if (off <= 4) off = 4;
        this.objectSize = off;
    }

    // Compute method slots. Should be called after inheritFrom(parent) (so methodSlots initially contains parent's slots if any).
    // This method will:
    //  - For each declared method in this class:
    //      - If same-name method exists in methodSlots -> this is an override: replace MethodInfo but preserve slot index.
    //      - Else -> append new MethodInfo to methodSlots with next slot index.
    public void computeMethodSlotsFromDeclared() {
        // Ensure methodSlots map exists
        if (methodSlots == null) {
            // unlikely, but ensure non-null
            // methodSlots = new LinkedHashMap<>();
        }
        // Determine current slot count (parent slots preserved)
        int nextSlot = methodSlots.size();

        // But we must find the maximum slot index used so far (in case methodInfos already contain setSlotIndex)
        int maxExisting = -1;
        for (MethodInfo mi : methodSlots.values()) {
            if (mi.getSlotIndex() >= 0 && mi.getSlotIndex() > maxExisting) maxExisting = mi.getSlotIndex();
        }
        if (maxExisting >= 0) nextSlot = maxExisting + 1;

        // For each declared method, either override or append
        for (MethodInfo dm : declaredMethods) {
            String mname = dm.getName();
            if (methodSlots.containsKey(mname)) {
                // override: preserve slot index
                MethodInfo old = methodSlots.get(mname);
                int slot = old.getSlotIndex();
                dm.setSlotIndex(slot);
                methodSlots.put(mname, dm);
            } else {
                // new slot
                dm.setSlotIndex(nextSlot);
                methodSlots.put(mname, dm);
                nextSlot++;
            }
        }

        // update vtable size in bytes (4 bytes per slot)
        this.vtableSize = methodSlots.size() * 4;
    }

    // Helpers to access final layout data
    public List<FieldInfo> getAllFields() {
        return Collections.unmodifiableList(allFields);
    }

    public LinkedHashMap<String, MethodInfo> getMethodSlots() {
        return methodSlots;
    }

    public int getObjectSizeBytes() { return objectSize; }
    public int getVTableSizeBytes() { return vtableSize; }

    // Utility method used by existing code for generating labels
    public static String methodLabel(String className, String methodName) {
        return className + "_" + methodName;
    }

    // Pretty-print matching the earlier output
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class ").append(name).append("\n");
        if (parentName != null) sb.append("  extends ").append(parentName).append("\n");
        sb.append("  Fields:\n");
        if (allFields.isEmpty()) {
            // keep empty line (matching earlier)
        } else {
            for (FieldInfo f : allFields) {
                sb.append("    ").append(f.getName()).append(" @").append(f.getOffset()).append("\n");
            }
        }
        sb.append("  Methods:\n");
        if (methodSlots.isEmpty()) {
            // nothing
        } else {
            for (MethodInfo mi : methodSlots.values()) {
                sb.append("    slot ").append(mi.getSlotIndex()).append(": ").append(mi.getName())
                  .append(" -> ").append(mi.getLabel()).append(" (args=").append(mi.getArgCount()).append(")\n");
            }
        }
        sb.append("  objectSize=").append(objectSize).append(" bytes\n");
        sb.append("  vtableSize=").append(vtableSize).append(" bytes\n");
        return sb.toString();
    }
}
