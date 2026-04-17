// yes
// Program type checked successfully
//multi-level inheritence
class T257{
	public static void main(String[] args){
		System.out.println(new A().foo());
	}
}

class A{
	boolean f;
	public int foo(){
		C c;
		boolean b;
        c = new C();
		b = c.bar();
		return 1;
	} 
}

class B extends A{}
class C extends B{
	public boolean bar(){
		return f;
	}
}
