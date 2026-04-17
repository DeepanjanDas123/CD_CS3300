// yes
// Program type checked successfully
// method call error
class T146{
	public static void main(String[] args){
		System.out.println(new A().foo(2));
	}
}

class A{
	public int foo(int p1){
		int b1;
		A a;
		a = new A();
		b1 = (this.bar()).baz(2);
		return b1;
	}
	public A bar(){
		return new A();
	}
    public int baz(int x) {
        return x;
    }
}
