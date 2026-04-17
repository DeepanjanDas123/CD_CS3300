// yes
// Program type checked successfully
//returning with method call
class T201{
	public static void main(String[] args){
		System.out.println(new A().foo());
	}
}

class A{
	public int foo(){
		return new A().bar();
	}
    public int bar() { return 0; }
}