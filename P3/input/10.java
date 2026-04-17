import java.util.function.Function;

class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo{
  public int check() {
    Holder h;
    int recv1;
    Num recv2;
    Num n;
    Function <Integer, Integer> f;
    h = new Holder();
    n = new Num();
    f = (x) -> (x + 1);
    recv1 = n.setVal((1+2)*3);
    recv2 = h.set(n);
    System.out.println((h.get()).getVal());
    System.out.println(f.apply(3));
    System.out.println(f.apply(4));
    return 69;
  }
}

class Num {
  int val;
  public int getVal() { return val; }
  public int setVal(int v) { val = v; return v; }
  //public Num(int dummy) { val = 0; } // NOTE: grammar doesn't support constructor initializers — but we can provide method to set
  // We'll instead use set above; leave this as inert
}

class Holder {
  Num x;
  public Num set(Num n) { x = n; return x; }
  public Num get() { return x; }
}
