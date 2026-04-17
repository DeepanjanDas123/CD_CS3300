// yes
// Program type checked successfully
//arguments 
class T22{
    public static void main(String[] args){
        System.out.println(new A().foo(1, true));
    }
}

class A{
    public int foo(int p1, boolean p2){
        B b;
        int x;
        int y;
        boolean z;
        x = 0;
        y = 0;
        z = false;
        b = new B();
        p1 = b.bar(x, y, z);
        return p1;
    }
}

class B{
    public int bar(int p1, int p2, boolean p3){
	return p1;
    }
}
