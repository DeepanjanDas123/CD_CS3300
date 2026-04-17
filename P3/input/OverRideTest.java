class Main {
    public static void main(String[] a) {
        System.out.println(0);
    }
}

class A {
    int a;
    public int m1() { return 1; }
    public int m2(int p) { return p; }
}

class B extends A {
    int b;
    public int m1() { return 2; } // overrides A.m1
    public int m3() { return 3; } // new method
}
