// yes
// Program type checked successfully
//returning nested call
class T210{
	public static void main(String[] args){
		System.out.println(new A().foo());
	}
}

class A{
	public int foo(){
		boolean b1;
		A a;
		a = new A();
		b1 = a.bar(2);
		return 1;
	}
	public boolean bar(int p){
		return this.baz(p);
	}
    public boolean baz(int p) {
        return true;
    }
}
