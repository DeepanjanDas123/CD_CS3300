// yes
// Program type checked successfully
//inheritance
class T27{
    public static void main(String[] args){
        System.out.println(new A().foo(5));
    }
}

class A{
    public int foo(int p1){
    	B b;
        int x;
        b = new B();
        x = b.bar(true);
        return 1;
    }
}

class B extends A{
    public int bar(boolean p1){
        A a;
        B b;
        a = new A();
        a = new B();
        b = new B();
        return 1;
    }
}
