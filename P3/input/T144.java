// yes
// Program type checked successfully
// if-else with bracket expr
class T8{
	public static void main(String []args){
		System.out.println(new A().foo());
	}
}
class A{
	public int foo(){
		int f1;
		if(((2<=3))){
			f1 = 1;
		}else{
			f1 = 2;
		}
		return f1;
	}
}
