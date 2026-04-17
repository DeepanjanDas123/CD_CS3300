class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo {
  public int check() {
    A a1;
    int recv1;
    int recv2;
    a1 = new A();
    recv1 = a1.init(4);
    recv2 = a1.setAt(2, 99);
    System.out.println(a1.getAt(1));
    System.out.println(a1.len());
    return 69;
  }
}

class A {
  int[] data;
  public int init(int n) { data = new int[n]; return 0; }
  public int setAt(int i, int v) { data[i] = v; return v; }
  public int getAt(int i) { return data[i]; }
  public int len() { return data.length; }
}
