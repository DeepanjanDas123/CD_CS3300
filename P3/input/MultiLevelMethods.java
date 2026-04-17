class Main {
  public static void main(String[] a) { System.out.println(0); }
}

class Base {
  public int alpha() { return 1; }
  public int beta()  { return 2; }
}

class Mid extends Base {
  public int alpha() { return 10; } // override
  public int gamma() { return 30; }
}

class Leaf extends Mid {
  public int delta() { return 40; }
}
