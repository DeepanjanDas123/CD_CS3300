import java.util.function.Function;
class Factorial {
    public static void main(String [] arg) {
        System.out.println(new B().foor());
    }
}

class B {
    public int foor() {
        Function<Integer, Integer> a ;
        a = ((x)-> (this.foo((y) -> (((this.foo((z) -> z + 5)).apply(6)) * 3))).apply(3));
        return a.apply(3);
    }
    public Function<Integer, Integer> foo(Function<Integer, Integer> x){
        return x;
    }
}