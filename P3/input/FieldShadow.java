class Main {
    public static void main(String[] a) { System.out.println(0); }
}

class Parent {
    int x;
    int y;
    public int f() { return 0; }
}

class Child extends Parent {
    int x; // hides parent's x (not overriding)
    public int g() { return 1; }
}
