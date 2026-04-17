// yes
// Program type checked successfully
//class field vs local var
class T5{
	public static void main(String[] args){
		System.out.println(new A().foo());
	}
}
class A{
	int f1;
	public int foo(){
		boolean f1;
		f1 = true;
		return 1;
	}
}
