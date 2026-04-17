package visitor;

import java.util.*;

public class TypeUtils {

    // Normalize a type name to canonical internal form.
    // e.g. "Integer" -> "int", "Boolean" -> "boolean"
    // Leaves class names and array types intact except their base is normalized.
    public static String normalize(String t) {
        if (t == null) return null;
        t = t.trim();

        // function types: Function<X,Y> -> normalize inside
        if (isLambda(t)) {
            LambdaType lt = parseLambdaRaw(t); // raw parse (doesn't normalize)
            String a = normalize(lt.argType);
            String r = normalize(lt.returnType);
            if (a == null) a = "?";
            if (r == null) r = "error"; // leave as-is; error handled elsewhere
            return "Function<" + a + "," + r + ">";
        }

        // arrays: normalize base
        if (t.endsWith("[]")) {
            String base = t.substring(0, t.length() - 2);
            String nbase = normalize(base);
            return (nbase == null) ? null : nbase + "[]";
        }

        // map wrapper names to primitives
        if (t.equals("Integer")) return "int";
        if (t.equals("Boolean")) return "boolean";

        // canonical primitives already
        if (t.equals("int") || t.equals("boolean")) return t;

        // otherwise assume class name, return as-is
        return t;
    }

    // Check if two types are exactly equal (after normalization)
    public static boolean equals(String t1, String t2) {
        String n1 = normalize(t1);
        String n2 = normalize(t2);
        if (n1 == null || n2 == null) return false;
        return n1.equals(n2);
    }

    // Check if rhs can be assigned to lhs (after normalization)
    public static boolean isAssignable(String lhs, String rhs, SymbolTable st) {
        String L = normalize(lhs);
        String R = normalize(rhs);
        if (L == null || R == null) return false;

        // identical types
        if (L.equals(R)) return true;

        // primitive types: must match exactly (we already normalized wrappers)
        if (isPrimitive(L) || isPrimitive(R)) return false;

        // array types
        if (isArray(L) && isArray(R)) {
            return isAssignable(arrayBase(L), arrayBase(R), st);
        } else if (isArray(L) || isArray(R)) {
            return false;
        }

        // lambda types
        if (isLambda(L) && isLambda(R)) {
            LambdaType lhsL = parseLambda(L);
            LambdaType rhsL = parseLambda(R);
            // contravariant param: rhsArg must be assignable to lhsArg after normalization
            String lhsArg = normalize(lhsL.argType);
            String rhsArg = normalize(rhsL.argType);
            String lhsRet = normalize(lhsL.returnType);
            String rhsRet = normalize(rhsL.returnType);

            boolean argsOk;
            if (rhsArg == null || rhsArg.equals("?")) {
                argsOk = true; // unknown RHS param -> accept for now
            } else if (lhsArg == null || lhsArg.equals("?")) {
                argsOk = true;
            } else {
                // contravariance check: lhsArg should be assignable to rhsArg?
                // We want: for an expected lhsArg, rhs must accept it -> rhsParam must be a supertype of lhsParam
                // so check isAssignable(rhsArg, lhsArg)
                argsOk = isAssignable(rhsArg, lhsArg, st);
            }

            boolean retsOk;
            if (rhsRet == null || rhsRet.equals("?")) {
                retsOk = lhsRet == null || lhsRet.equals("?");
            } else if (lhsRet == null || lhsRet.equals("?")) {
                retsOk = true;
            } else {
                // covariant: rhsRet must be assignable to lhsRet
                retsOk = isAssignable(lhsRet, rhsRet, st);
            }

            return argsOk && retsOk;
        } else if (isLambda(L) || isLambda(R)) {
            return false;
        }

        // class inheritance: check if R is subclass of L
        ClassInfo rhsClass = st.getClass(R);
        while (rhsClass != null) {
            if (rhsClass.name.equals(L)) return true;
            rhsClass = rhsClass.parent != null ? st.getClass(rhsClass.parent) : null;
        }

        return false;
    }

    // Helpers
    public static boolean isValidType(String type, SymbolTable st) {
        if (type == null) return false;
        String n = normalize(type);
        if (n == null) return false;
        if (equals(n, "int") || equals(n, "boolean") || equals(n, "int[]")) return true;
        if (isLambda(n)) return true;
        return st.getClass(n) != null;
    }

    public static boolean isPrimitive(String t) {
        String n = normalize(t);
        return "int".equals(n) || "boolean".equals(n);
    }

    public static boolean isArray(String t) {
        if (t == null) return false;
        return normalize(t).endsWith("[]");
    }

    public static String arrayBase(String arrType) {
        String n = normalize(arrType);
        if (n == null || !n.endsWith("[]")) return null;
        return n.substring(0, n.length() - 2);
    }

    public static boolean isLambda(String type) {
        if (type == null) return false;
        return type.trim().startsWith("Function<");
    }

    // Parse lambda returning normalized pieces (argType, returnType)
    public static LambdaType parseLambda(String t) {
        if (t == null || !isLambda(t)) return new LambdaType(null, null);
        // We assume nested Function forms are not used inside lambda params per your grammar.
        // Extract inside of <...>
        String inner = t.substring(t.indexOf('<') + 1, t.lastIndexOf('>')).trim();
        String[] parts = inner.split(",");
        if (parts.length != 2) return new LambdaType(null, null);
        String arg = parts[0].trim();
        String ret = parts[1].trim();
        return new LambdaType(normalize(arg), normalize(ret));
    }

    // Raw parse that doesn't normalize (used internally if needed)
    private static LambdaType parseLambdaRaw(String t) {
        if (t == null || !isLambda(t)) return new LambdaType(null, null);
        String inner = t.substring(t.indexOf('<') + 1, t.lastIndexOf('>')).trim();
        String[] parts = inner.split(",");
        if (parts.length != 2) return new LambdaType(null, null);
        return new LambdaType(parts[0].trim(), parts[1].trim());
    }

    // Represents a lambda type (arg and return). Fields may be normalized already.
    public static class LambdaType {
        public String argType;
        public String returnType;
        public LambdaType(String argType, String returnType) {
            this.argType = argType;
            this.returnType = returnType;
        }
    }
}
