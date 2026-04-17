// yes
// Program type checked successfully
// apply call
import java.util.function.Function;
class T143{
	public static void main(String[] args){
		System.out.println(new A().foo(2));
	}
}

class A{
	public int foo(int p1){
		int b1;
		Function<Integer, A> f1;
		B b2;
		A b3;
		b1 = 20;
		b2 = new B();
		f1 = (x)->b2;
		b3 = f1.apply(b1);		
		return b1; 
	}

}

class B extends A{
	public int apply(int p1){
		int b1;
		b1 = 10;
		return b1;
	}
}



