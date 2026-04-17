class Arrays {
    public static void main(String[] a) {
        System.out.println(new Wrapper().call());
    }
}

class Wrapper {
    int[] a;

    public int call() {
        int[] b;
        int i;
        int j;
        a = new int[10];
        b = new int[5];
        i = 0;
        while (i <= ((b.length) - 1)) {
            b[i] = (i + 1) * (i + 1);
            i = i + 1;
        }
        i = 0;
        while (i <= ((a.length) - 1)) {
            j = i;
            while (!(j <= ((b.length) - 1))) {
                j = j - (b.length);
            }
            a[i] = b[j];
            i = i + 1;
        }
        j = this.modify();
        i = 0;
        while (i <= ((a.length) - 1)) {
            System.out.println(a[i]);
            i = i + 1;
        }
        j = 0;
        while (j <= ((b.length) - 1)) {
            System.out.println(b[j]);
            j = j + 1;
        }
        return 0;
    }
    
    public int modify() {
        int i;
        i = 0;
        while (i <= ((a.length) - 1)) {
            a[i] = (a[i]) * (a[i]);
            i = i + 1;
        }
        return 0;
    }
}