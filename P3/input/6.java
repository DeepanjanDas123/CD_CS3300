class Main {
  public static void main(String[] a) {
    System.out.println(new Dildo().check());
  }
}

class Dildo {
  public int check() {
    int[] arr;
    arr = new int[3];
    // arr[0] = 11;
    arr[1] = 22;
    arr[2] = 33;
    System.out.println(arr[0]);
    System.out.println(arr[1]);
    System.out.println(arr[2]);
    System.out.println(arr.length);
    return 420;
  }
}
