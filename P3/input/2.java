class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo {
  public int check() {
    D d;
    int recv;
    d = new D();
    recv = d.setX(7);
    System.out.println(d.getX());
    return 0;
  }
}

class D {
  int x;
  public int getX() { return x; }
  public int setX(int v) { x = v; return x; }
}
