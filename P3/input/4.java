class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo {
  public int check() {
    Root r; 
    Mid m; 
    Leaf l;
    r = new Root();
    m = new Mid();
    l = new Leaf();
    System.out.println(r.ping()); // Root_ping
    System.out.println(m.ping()); // overridden in Mid? depends - we'll design so Leaf overrides
    System.out.println(l.ping()); // Leaf_ping
    System.out.println(m.pong()); // Mid_pong
    System.out.println(l.extra()); // Leaf_extra
    return 1;
  }
}

class Root {
  public int ping() { return 1; }
}

class Mid extends Root {
  public int ping() { return 2; }   // override
  public int pong() { return 3; }
}

class Leaf extends Mid {
  public int ping() { return 4; }   // override again
  public int extra() { return 5; }
}
