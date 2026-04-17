// yes
// Program type checked successfully
// Function calls in While <=
class T108{
	public static void main(String []args){
		System.out.println(new A().foo());
	}
}

class A{
	public int foo(){
        B b;
        int c;

        b = new B();

        while((b.bar(b.foobar(5))) <= 5)
            c = 5;

        return 1;
    }
}

class B {
	public int bar(A a){
        return 10;
    }

    public A foobar(int x){
        A a;
        a = new A();

        return a;
    }
}
