// yes
// Program type checked successfully
// Array allocation
class T121{
	public static void main(String []args){
		System.out.println(new A().foo());
	}
}

class A{
	public int foo(){
        int []x;
        A a;
        
        a = new A();
        x = new int [a.bar()];

        return 1;
    }

    public int bar(){
        return 5;
    }
}
