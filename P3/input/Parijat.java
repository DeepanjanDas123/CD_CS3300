import java.util.function.Function;

class LambdaEverything {
    public static void main(String[] args) {
        System.out.println(new Wrapper().call());
    }
}

class Wrapper {
    int a;
    A objA1;
    B objB2;
    int trash;

    public int call() {
        int b;
        B objB1;
        A objA2;
        int ans1;
        int ans2;
        int ans3;
        int ans4;
        Function<Integer, Integer> lam;
        a = 5;
        b = 12;
        lam = (x) -> (this.identity_int((y) -> (this.identity_int((z) -> (x + y) +
                ((z + b) * a))).apply(y * 100)))
                .apply(x * 10);
        a = 1;
        ans1 = lam.apply(1);
        System.out.println(ans1);
        ans2 = this.nest(
                (p) -> (lam.apply(p)) * ((this.identity_int(
                        (x) -> (this.identity_int((y) -> (this.identity_int((z) ->
                                (x + y) + ((z + b) * a))).apply(y * 100)))
                                .apply(x * 10)))
                        .apply(p)),
                (q) -> (lam.apply(q)) + ((this.identity_int(
                        (x) -> (this.identity_int((y) -> (this.identity_int((z) ->
                                (x + y) + ((z + b) * a))).apply(y * 100)))
                                .apply(x * 10)))
                        .apply(q)),
                1);
        System.out.println(ans2);
        ans3 = this.nest(
                (q) -> (lam.apply(q)) + ((this.identity_int(
                        (x) -> (this.identity_int((y) -> (this.identity_int((z) ->
                                (x + y) + ((z + b) * a))).apply(y * 100)))
                                .apply(x * 10)))
                        .apply(q)),
                (p) -> (lam.apply(p)) * ((this.identity_int(
                        (x) -> (this.identity_int((y) -> (this.identity_int((z) ->
                                (x + y) + ((z + b) * a))).apply(y * 100)))
                                .apply(x * 10)))
                        .apply(p)),
                1);
        System.out.println(ans3);
        objA1 = new A();
        trash = objA1.init(8);
        objB1 = new B();
        trash = objB1.init(9);
        objB2 = (this.identity_class1((x) -> objB1)).apply(objA1);
        objA2 = (this.identity_class2((x) -> objA1)).apply(objB1);
        ans4 = ((objA2.get()) * 10) + (objB2.get());
        System.out.println(ans4);
        return ans1 + ((ans3 / ans2) * ans4);
    }

    public Function<Integer, Integer> identity_int(Function<Integer, Integer> lam) {
        return lam;
    }

    public int nest(Function<Integer, Integer> lam1, Function<Integer, Integer> lam2,
                    int arg) {
        return lam1.apply(lam2.apply(arg));
    }

    public Function<A, B> identity_class1(Function<A, B> lam) {
        return lam;
    }

    public Function<B, A> identity_class2(Function<B, A> lam) {
        return lam;
    }
}

class A {
    int a;

    public int init(int k) {
        a = k;
        return a;
    }

    public int get() {
        return a;
    }
}

class B {
    int b;

    public int init(int k) {
        b = k;
        return b;
    }

    public int get() {
        return b;
    }
}