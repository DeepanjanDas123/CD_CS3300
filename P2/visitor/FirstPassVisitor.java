package visitor;

import syntaxtree.*;
import java.util.*;

public class FirstPassVisitor extends GJDepthFirst<String, Object> {
    private final SymbolTable symtab;

    public FirstPassVisitor(SymbolTable symtab) {
        this.symtab = symtab;
    }

    @Override
    public String visit(ImportFunction n, Object argu) {
        symtab.hasFunctionImport = true;
        return null;
    }

    @Override
    public String visit(MainClass n, Object argu) {
        String className = n.f1.f0.toString();
        symtab.addClass(className, null);
        symtab.currentClass = className;

        MethodInfo mainMethod = new MethodInfo("main", "void");
        mainMethod.addParam("String[]", n.f11.f0.toString());

        symtab.addMethodToCurrentClass(mainMethod);
        return null;
    }

    @Override
    public String visit(ClassDeclaration n, Object argu) {
        String className = n.f1.f0.toString();
        symtab.addClass(className, null);
        symtab.currentClass = className;

        // fields
        for (Enumeration<Node> e = n.f3.elements(); e.hasMoreElements();) {
            VarDeclaration vd = (VarDeclaration) e.nextElement();
            String type = vd.f0.accept(this, null);
            String name = vd.f1.f0.toString();
            symtab.addFieldToCurrentClass(name, type);
        }

        // methods
        for (Enumeration<Node> e = n.f4.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this, className);
        }

        symtab.currentClass = null;

        return null;
    }

    @Override
    public String visit(ClassExtendsDeclaration n, Object argu) {
        String className = n.f1.f0.toString();
        String parentName = n.f3.f0.toString();
        symtab.addClass(className, parentName);
        symtab.currentClass = className;

        for (Enumeration<Node> e = n.f5.elements(); e.hasMoreElements();) {
            VarDeclaration vd = (VarDeclaration) e.nextElement();
            String type = vd.f0.accept(this, null);
            String name = vd.f1.f0.toString();
            symtab.addFieldToCurrentClass(name, type);
            //System.out.println("Adding var " + name + " : " + type + " to " + symtab.currentClass);
        }

        for (Enumeration<Node> e = n.f6.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this, className);
        }

        symtab.currentClass = null;

        return null;
    }

    @Override
    public String visit(VarDeclaration n, Object argu) {
        String type = n.f0.accept(this, null);
        String name = n.f1.f0.toString();
        //System.out.println("Adding var " + name + " : " + type + " to " + symtab.currentClass);
        return type;
    }

    @Override
    public String visit(MethodDeclaration n, Object className) {
        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.f0.toString();
        MethodInfo mi = new MethodInfo(methodName, returnType);

        symtab.currentMethod = methodName;

        if (n.f4.present()) {
            n.f4.accept(this, mi);
        }

        for (Enumeration<Node> e = n.f7.elements(); e.hasMoreElements();) {
            VarDeclaration vd = (VarDeclaration) e.nextElement();
            String type = vd.f0.accept(this, null);
            String name = vd.f1.f0.toString();
            mi.addLocal(name, type);
            //System.out.println("Adding var " + name + " : " + type + " to " + symtab.currentMethod);
        }

        symtab.addMethodToCurrentClass(mi);
        symtab.currentMethod = null;
        return null;
    }

    @Override
    public String visit(FormalParameter n, Object argu) {
        String type = n.f0.accept(this, null);
        String name = n.f1.f0.toString();
        ((MethodInfo)argu).addParam(name, type);
        //System.out.println("Adding param " + name + " : " + type + " to " + ((MethodInfo)argu).name);
        return null;
    }

    @Override
    public String visit(Type n, Object argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(IntegerType n, Object argu) { return "int"; }

    @Override
    public String visit(BooleanType n, Object argu) { return "boolean"; }

    @Override
    public String visit(ArrayType n, Object argu) { return "int[]"; }

    @Override
    public String visit(LambdaType n, Object argu) {
        String t1 = n.f2.f0.toString();
        String t2 = n.f4.f0.toString();
        return "Function<" + t1 + "," + t2 + ">";
    }

    @Override
    public String visit(Identifier n, Object argu) {
        return n.f0.toString();
    }
}
