// yes
// Program type checked successfully
// Function calls in If
class T106{
	public static void main(String []args){
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

        if(!b.bar(a))
            c = 5;

        return 5;
    }
}

class B {
	public boolean bar(A a){
        return false;
    }
}
