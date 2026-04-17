class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check()); // B's own
  }
}

class Dildo {
  public int check() {
    A a1; 
    B b1;
    a1 = new A();
    b1 = new B();
    System.out.println(a1.m1());
    System.out.println(a1.m2());
    System.out.println(b1.m1()); // overridden
    System.out.println(b1.m2()); // inherited
    System.out.println(b1.m3()); // B's own
    return 69;
  }
}

class A {
  public int m1() { return 10; }
  public int m2() { return 20; }
}

class B extends A {
  public int m1() { return 30; } // override slot 0
  public int m3() { return 40; } // new slot
}
