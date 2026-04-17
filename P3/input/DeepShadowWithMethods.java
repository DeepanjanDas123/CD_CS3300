class Main {
  public static void main(String[] a) { System.out.println(0); }
}

class P {
  int a;
  public int ma() { return 1; }
}

class Q extends P {
  int a;
  public int mb() { return 2; }
}

class R extends Q {
  int a;
  public int ma() { return 3; } // override P.ma (via inheritance through Q)
  public int mc() { return 4; }
}
