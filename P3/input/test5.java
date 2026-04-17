import java.util.function.Function;

class Main {
  public static void main(String[] args) {
    System.out.println(new RunnerB().run());
  }
}


class RunnerB {
  public int run() {
    TopBase tb;
    Middle md;
    Bottom bt;
    int r1;
    int r2;
    int r3;

    tb = new Bottom();          
    md = new Bottom();          
    bt = new Bottom();          

    r1 = tb.alpha(4);           
    r2 = md.gamma(5);           
    r3 = bt.beta(6);            

    return (r1 + r2) + r3;        
  }
}

class TopBase {
  public int alpha(int x) {
    Function<Integer,Integer> f;
    f = (y) -> y + 1;
    return f.apply(x);  
  }
  public int beta(int x) {
    return x + 2;      
  }
}

class Middle extends TopBase {
  public int alpha(int x) {   
    Function<Integer,Integer> f;
    f = (z) -> z * 2;
    return f.apply(x); 
  }
  public int gamma(int x) {   
    return x + 10;
  }
}

class Bottom extends Middle {
  public int beta(int x) {    
    return x * 3;
  }
}
