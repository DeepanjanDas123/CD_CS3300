import java.util.function.Function;

class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class Test1 {
    public int[] check() {
        int[] arr;
        arr = new int[10];
        arr[0] = 5;
        System.out.println(arr.length);
        System.out.println(arr[0]);
        return arr;
    }
}

class Base {
    int x;
    public int getX() { return x; }
}

class Derived extends Base {
    int y;
    public int sum() { return (x + y); }
}

class Test2 {
    public int check() {
        Base d;
        d = new Derived();
        System.out.println(d.getX());
        return 0;
    }
}

class Shadow {
    public int f(int x) {
        int x1;
        x1 = x + 1;
        return x1;
    }
}

class impl{
    public int hello(){
        Shadow s;
        System.out.println(s.f(10));
        return 0;
    }
}

// class LambdaTest {
//     public int check() {
//         Function<Integer,Integer> f;
//         // f = (x) -> x + 1;
//         System.out.println(f.apply(5));
//         return 0;
//     }
// }

class Bad9 {
    int x;
    public int f() {
        boolean x;     // redeclare with different type
        int[] y;
        int z;
        z = y.find(something);
        return 0;
    }
}

class Bad8 {
    public int min(int a) {
        if (1) System.out.println(5);  // condition not boolean
        return a;
    }
}

class Bad7 {
    public int min(int a) {
        int[] arr;
        arr = new int[10];
        System.out.println(arr[true]);  // index must be int
        return a;
    }
}

class Bad6 {
    public int man(int a) {
        Foo f;
        f = new Foo();
        System.out.println(f.bar(true));  // bar expects int
        return a;
    }
}

class Bad5 {
    public int man(int a) {
        Foo f;
        f = new Foo();
        System.out.println(f.bar(1,2));  // bar expects 1 arg
        return a;
    }
}
class Foo {
    public int bar(int x) { return x; }
}

class Bad4 {
    public int mai() {
        Base b;
        b = new Base();
        System.out.println(b.g()); // g() doesn’t exist
        return 0;
    }
}

class Bad3 {
    public int min() {
        int[] arr;
        arr = 42;   // int assigned to int[]
        return 1;
    }
}


class Bad1 {
    public int cake() {
        int x;
        x = y + 1;   // y undeclared
        return 0;
    }
}

class Bad2 {
    public int f() {
        return true;  // expected int, got boolean
    }
}
