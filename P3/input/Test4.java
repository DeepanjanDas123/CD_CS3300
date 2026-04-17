import java.util.function.Function;

class Test4 {
    public static void main(String[] args) {
        System.out.println(new T().start(10));
    }
}

class T {
    public int start(int x) {
        if (true || ((new A()).foo(1))) {
            System.out.println(5);
        } else {
            System.out.println(9);
        }

        if (false && ((new A()).foo(2))) {
            System.out.println(6);
        } else {
            System.out.println(10);
        }

        if (true && ((new A()).foo(3))) {
            System.out.println(7);
        } else {
            System.out.println(11);
        }

        if (false || ((new A()).foo(4))) {
            System.out.println(8);
        } else {
            System.out.println(12);
        }

        return x * 2;
    }
}

class A {
    public boolean foo(int x) {
        System.out.println(x);
        return false;
    }
}