class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo()._check());
  }
}

class Dildo {
  public int _check() {
    Child c;
    int recv1;
    int recv2;
    c = new Child();
    //recv1 = c.setParentX(10);
    recv2 = c.setChildX(20);
    System.out.println(c.getParentX()); // parent field
    System.out.println(c.getChildX());  // child field
    return 69;
  }
}

class Parent {
  int x;
  public int getParentX() { return x; }
  public int setParentX(int v) { x = v; return v; }
}

class Child extends Parent {
  int x; // shadowing
  public int getChildX() { return x; }
  public int setChildX(int v) { x = v; return v; }
}
