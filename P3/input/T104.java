// yes
// Program type checked successfully
// Lambda in return expr
import java.util.function.Function;
class T143{
	public static void main(String[] args){
		System.out.println(new A().foo(2));
	}
}

class A{
	public int foo(int p1){
		int b1;
		Function<Integer, Boolean> f1;
        b1 = 0;
		f1 = new B().getLambda();
		return b1; 
	}

}

class B{
	public Function<Integer, Boolean> getLambda(){
		return (x)->x!=(new A().foo(x));
	}
}
