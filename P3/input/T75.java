// yes
// Program type checked successfully
//multi-level inheritence
class T256{
	public static void main(String[] args){
		System.out.println(new A().foo());
	}
}

class A{
	public int foo(){
		C c;
		boolean b;
        c = new C();
		b = c.bar();
		return 1;
	}
	public boolean bar(){
		return true;
	} 
}

class B extends A{}
class C extends B{}
