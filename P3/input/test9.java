import java.util.function.Function;

class Main {
    public static void main(String[] args) {
        System.out.println(
            new C().compute(
                new B().combine(
                    5 + (new A().inc(2)),  
                    false               
                ),
                new A().applyFunc(
                    (x) -> x              
                ),
                new D()                 
            )
        );
    }
}

class A {
    public int inc(int n) {
        return n + 1;
    }

    public int applyFunc(Function<A, A> f) {
        return (f.apply(this)).inc(2);
    }
}

class B {
    public int combine(int val, boolean flag) {
        return val + 2;
    }
}

class D {
    public int times(int a, int b) {
        return a * b;
    }
}

class C {
    public int compute(int p, int q, D d) {
        return (p * q) + (d.times(p, q));
    }
}
