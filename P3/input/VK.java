import java.util.function.Function;

class Test {
    public static void main(String[] args) {
        System.out.println(new T().start());
    }
}

class T {
    A c;

    public int start() {
        Function<A, B> y;
        y = this.init();
        return ((this.init()).apply((new A()).init(5, (a) -> a))).getX();
    }

    public Function<A, B> init() {
        Function<A, B> x;
        int b;
        b = 10;
        c = new A();
        x = (a) -> (new B()).init((a.getX()) + (c.getX()));
        return x;
    }
}

class A {
    int x;

    public A init(int y, Function<A, A> z) {
        x = 10;
        x = (y) + ((z.apply(this)).getX());
        return this;
    }

    public int getX() {
        return x;
    }
}

class B {
    int x;

    public B init(int y) {
        B z;
        A w;

        x = y * y;
        z = new B();
        w = new A();

        return this;
    }

    public int getX() {
        return x;
    }
}