// yes
// Program type checked successfully
// Function calls in If <=
class T102{
	public static void main(String []args){
		System.out.println(new A().foo());
	}
}

class A{
	public int foo(){
        B b;
        int c;

        b = new B();

        if((b.bar(b.foobar(5))) <= 5)
            c = 5;
        else
            c = 3;

        return c;
    }
}

class B {
	public int bar(A a){
        return 1;
    }

    public A foobar(int x){
        A a;
        a = new A();

        return a;
    }
}
