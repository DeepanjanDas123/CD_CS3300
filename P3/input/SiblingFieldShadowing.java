class Main {
  public static void main(String[] a) { System.out.println(0); }
}

class S {
  int x;
  public int m() { return 1; }
}

class L extends S {
  int x;
  public int m() { return 2; }
}

class R extends S {
  int x;
  public int m2() { return 3; }
}
