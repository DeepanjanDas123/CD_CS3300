import java.util.function.Function;

class Test5 {
    public static void main(String[] args) {
        System.out.println(new T().start());
    }
}

class T {
    public int start() {
        return ((new C()).c(
                (x)
                        -> (x + (((new C()).c(
                        (y)
                                -> (y + (((new C()).c(
                                (z)
                                        -> (z * y) +
                                        (x +
                                                (((new C())
                                                        .c((u)
                                                                        -> (u +
                                                                        z),
                                                                (v) -> (v * x)))
                                                        .apply(
                                                                10))),
                                (w) -> w + y))
                                .apply(3))),
                        (y) -> (y + x)))
                        .apply(2))),
                (x) -> (x + 3)))
                .apply(5);
    }
}

class C {
    public Function<Integer, Integer> c(Function<Integer, Integer> x,
                                        Function<Integer, Integer> y) {
        return (a) -> x.apply(y.apply(a));
    }
}
