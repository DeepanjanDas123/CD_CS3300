import java.util.function.Function;

class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo{
  public int check() {
    Function <Integer, Integer> f;
    f = (x) -> (x + 1);
    System.out.println(f.apply(3));
    System.out.println(f.apply(4));
    return 69;
  }
}
