import java.util.function.Function;

class Main {
  public static void main(String[] args) {
    System.out.println(new Runner1().run());
  }
}

class Runner1 {
  public int run() {
    Function<Integer,Integer> inc;
    A a;
    a = new B();
    inc = (n) -> n + (a.compute(n));
    return inc.apply(5);   
  }
}

class A {
  public int compute(int x) {
    return x;            
  }
}

class B extends A {
  public int compute(int x) {
    return x * 2;        
  }
}
