class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo {
  public int check() {
    Chain c;
    c = new Chain();
    System.out.println((c.f()).g(2));
    return 67;
  }
}

class Chain {
  public Chain f() { return this; }
  public int g(int x) { return x + 7; }
}
