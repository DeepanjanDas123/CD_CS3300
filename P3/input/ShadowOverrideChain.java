class Main {
  public static void main(String[] a) {
    System.out.println(0);
  }
}

class A {
  int x;
  public int a() { return 1; }
}

class B extends A {
  int x;
  public int a() { return 2; } // overrides A.a
  public int b() { return 20; }
}

class C extends B {
  int x;
  public int a() { return 3; } // overrides B.a
  public int c() { return 30; }
}
