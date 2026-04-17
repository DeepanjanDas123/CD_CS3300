// yes
// Program type checked successfully
//return type
class T24{
    public static void main(String[] args){
        System.out.println(new A().foo(5));
    }
}

class A{
    public int foo(int p1){
        B b;
        int x;
        boolean y;
	y = true;
        b = new B();
        x = b.bar(y);
        return 1;
    }
}

class B{
    public int bar(boolean p1){
        return 1;
    }
}
