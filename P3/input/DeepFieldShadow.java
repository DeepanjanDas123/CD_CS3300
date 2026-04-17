class Main {
  public static void main(String[] a) { System.out.println(new Y().getV()); }
}

class X {
  int v;
  public int getV() { return v; }
}

class Y extends X {
  int v;
  public int getV() { return 10; } // overrides
}

class Z extends Y {
  int v;
  public int getV() { return 20; } // overrides again
}
