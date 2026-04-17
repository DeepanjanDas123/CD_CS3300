// yes
// Program type checked successfully
// array allocation
class T151{
	public static void main(String[] args){
		System.out.println(new A().foo(2));
	}
}

class A{
	public int foo(int p1){
		int[] a;
		a = new int[this.bar(2)];
		return 2;
	}

    public int bar(int p1) {
        return 2;
    }
}

