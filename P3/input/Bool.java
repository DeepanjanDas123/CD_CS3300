class Bool {
    public static void main(String[] a) {
        System.out.println(new Wrapper().call()); // Initially it was 10. MiniJava/MicroJava code uses the older
                                                  // expression.
    }
}

class Wrapper {
    public int call() {
        boolean t;
        boolean f;
        t = true;
        f = false;

        if (true != (1 <= 3)) {
            System.out.println(0 - 1);
        }
        if (true != (5 <= 5)) {
            System.out.println(0 - 1);
        }
        if (false != (6 <= 5)) {
            System.out.println(0 - 1);
        }
        if (false != ((0 - 1) <= (0 - 2))) {
            System.out.println(0 - 1);
        }

        if (t) {
            System.out.println(1);
        }
        if (f) {
            System.out.println(2);
        }

        if (t && t) {
            System.out.println(3);
        }
        if (t && f) {
            System.out.println(4);
        }
        if (f && t) {
            System.out.println(5);
        }
        if (f && f) {
            System.out.println(6);
        }

        if (t || t) {
            System.out.println(7);
        }
        if (t || f) {
            System.out.println(8);
        }
        if (f || t) {
            System.out.println(9);
        }
        if (f || f) {
            System.out.println(10);
        }

        if (!t) {
            System.out.println(11);
        }
        if (!f) {
            System.out.println(12);
        }
        return 13;
    }
}