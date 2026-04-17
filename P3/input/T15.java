// yes
// Program type checked successfully
// Function calls in If with *
class T115{
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

        if(((b.bar(a)) * (b.bar(a))) != 6)
            c = 5;

        return 1;
    }
}

class B {
	public int bar(A a){
        return 1;
    }
}
