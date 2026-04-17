class Main {
    public static void main(String[] a) { System.out.println(0); }
}

class A {
    public int foo() { return 1; }
}

class B extends A {
    public int foo() { return 2; }    // override A.foo
    public int bar() { return 10; }   // new
}

class C extends B {
    public int bar() { return 20; }   // override B.bar
    public int baz() { return 30; }   // new
}
