// yes
// Program type checked successfully
//class extends
//allocate
class T3{
	public static void main(String[] args){
        System.out.println(new A().foo());
    }
}

class A{
    public int foo(){
        A a;
        a = new B();        
        return 1;
    }
}

class B extends A{

}
