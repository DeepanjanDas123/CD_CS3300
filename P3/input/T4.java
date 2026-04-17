// yes
// Program type checked successfully
// Function calls in While
class T104{
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

        while(!b.bar(a))
            c = 5;

        return 1;
    }
}

class B {
	public boolean bar(A a){
        return true;
    }

    public A foobar(int x){
        A a;
        a = new A();

        return a;
    }
}
