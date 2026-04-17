class Main {
  public static void main(String[] a) { System.out.println(0); }
}

class Root {
  int r;
  public int m() { return 1; }
}

class Left extends Root {
  int x;
  public int m() { return 2; } // overrides Root.m
  public int l() { return 10; }
}

class Right extends Root {
  int x;
  public int rmethod() { return 20; }
}

class Leaf extends Left {
  int x;
  public int leaf() { return 30; }
}
