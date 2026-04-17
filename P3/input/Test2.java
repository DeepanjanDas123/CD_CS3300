import java.util.function.Function;

class Test2 {
    public static void main(String[] args) {
        System.out.println(new T().start(10));
    }
}

class T {
    Function<Integer, Integer> x;

    public int start(int z) {
        x = (y) -> this.foo(y);
        return x.apply(z);
    }

    public int foo(int z) {
        int y;

        if (z != 0) {
            y = z * (x.apply(z - 1));
        } else {
            y = 1;
        }

        return y;
    }
}