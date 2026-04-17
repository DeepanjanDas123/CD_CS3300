// yes
// Program type checked successfully
// Lambda definition line 15
import java.util.function.Function;
class T143{
	public static void main(String[] args){
		System.out.println(new A().foo(2));
	}
}

class A{
	public int foo(int p1){
		int b1;
		Function<A, Integer> f1;
        b1 = 0;
		f1 = (x)->b1;
		return b1; 
	}

}

