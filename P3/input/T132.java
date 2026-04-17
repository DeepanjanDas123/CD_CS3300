// yes
// Program type checked successfully
//object arguments 
class T31{
    public static void main(String[] args){
        System.out.println(new A().foo(1, true));
    }
}

class A{
    public int foo(int p1, boolean p2){
        A a;
        B b;
        a = new A();
        b = new B();
        p1 = b.bar(a, 2);
        return p1;
    }
}

class B{
    public int bar(A p1, int p2){
        return p2;
    }
}
