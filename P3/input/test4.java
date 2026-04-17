import java.util.function.Function;

class Main {
  public static void main(String[] args) {
    System.out.println(new RunnerA().run());
  }
}

class RunnerA {
  public int run() {
    int[] arr;
    int a;
    int b;
    int c;
    int d;
    Function<Integer,Integer> add3;

    arr = new int[4];
    arr[0] = 20;
    arr[1] = 5;
    a = arr[0];               
    b = arr[1];               
    c = a - b;
    d = (a * (b + 2)) / (b - 1); 

    add3 = (x) -> x + 3;
    arr[2] = add3.apply(d);  
    arr[3] = ((arr[0]) / (arr[1])) + ((arr[2]) - c);

    if ((a <= (b * 5))) {            
      a = a + 1;                     
    } else {
      a = a - 1;
    }

    if ((!(a != 21)) || (b <= 0)) {  
      b = b + 0;
    }

    return (((arr[3]) + c) + (arr[2])) - a;
  }
}
