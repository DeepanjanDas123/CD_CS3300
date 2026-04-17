// yes
// Program type checked successfully
// method call testing
class T216{
	public static void main(String[] args){
		System.out.println(new A().foo());
	}
}

class A{
	public int foo(){
		A a;
		B b;
		int c;
		a = new A();
		b = new B();
		c = (a.foobar(2)).bar(a, b);
		return 1;
	}

	public int bar(A p1, A p2){
		return 2;
	}

	public A foobar(int p1){
		return new A();
	}	
}

class B extends A{}