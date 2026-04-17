import java.util.function.Function;

class LambdaBruh {
    public static void main(String[] args) {
        System.out.println(new test().Less_Equal(5, 10));
    }
}

class test {
    public int Less_Equal(int a, int b) {
        Function<Integer, Integer> f;
        int t;
        test ram;
        t = 0;
        ram = new test();
        f = ((x) -> ((ram.dos((y) -> ((y + x) + t))) + x));
        return f.apply(2);
    }

    public int dos(Function<Integer, Integer> g) {
        return g.apply(1);
    }
}