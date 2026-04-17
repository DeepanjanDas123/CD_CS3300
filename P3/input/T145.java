// yes
// Program type checked successfully
// while
class T9{
	public static void main(String []args){
		System.out.println(new A().foo());
	}
}
class A{
	public int foo(){
		int f1;
		f1 = 0;
		while(f1 != 0){
			f1 = f1 + 1; 
		}
		return f1;
	}
}
