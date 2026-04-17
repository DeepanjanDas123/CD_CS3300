import java.io.*;
import java.lang.reflect.*;
import syntaxtree.*;
import visitor.*;


public class P6 {
    public static void main(String[] args) throws Exception {
        Node root = new MiniRAParser(System.in).Goal();
        FirstPassVisitor<Object,Object> v = new FirstPassVisitor<>();
        root.accept(v, null);
    }
}