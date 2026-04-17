// yes
// Program type checked successfully
// Function calls in While with /
class T112{
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

        while(((b.bar(a)) / (b.bar(a))) != 1)
            c = 5;

        return 1;
    }
}

class B {
	public int bar(A a){
        return 1;
    }
}
