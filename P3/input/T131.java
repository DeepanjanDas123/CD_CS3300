// yes
// Program type checked successfully
// while with extends
class T10{
	public static void main(String []args){
		System.out.println(new A().foo());
	}
}
class B{
	int f2;
}
class A extends B{
	public int foo(){
		int f1;
		f1 = 1;
		while(f1 != 0){
			f1 = f1 + f2; 
            f1 = f1 - 1;
		}
		return f1;
	}
}
