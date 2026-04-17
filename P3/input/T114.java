// yes
// Program type checked successfully
// method call with lambdas as parameter
import java.util.function.Function;
class T143{
	public static void main(String[] args){
		System.out.println(new A().foo(2));
	}
}

class A{
	public int foo(int p1){
		int b1;
		B b2;
		b2 = new B();
		b1 = 20;
		b1 = this.bar(b1, (x)->b2);
		return b1; 
	}
	
	public int bar(int p1, Function<Integer, A> p2){
		int b1;
		b1 = 10;
		return b1;
	}

}

class B extends A{
}


