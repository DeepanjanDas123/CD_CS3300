import java.util.function.Function;

class Test3 {
    public static void main(String[] args) {
        System.out.println(new T().start(10));
    }
}

class T {
    public int start(int x) {
        return ((new A())._foo()) + ((new A_()).foo());
    }
}

class A {
    public int _foo() {
        return 0;
    }
}

class A_ {
    public int foo() {
        return 1;
    }
}