class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo{
  public int check() {
    C c;
    c = new C();
    System.out.println(c.run(5));
    System.out.println(c.run(1));
    return 67;
  }
}

class C {
  public int run(int n) {
    int r;
    r = 0;
    while (n != 0) {
      if (n <= 1) {
        r = r + 1;
        n = 0;
      } else {
        r = r + n;
        n = n - 1;
      }
    }
    return r;
  }
}
