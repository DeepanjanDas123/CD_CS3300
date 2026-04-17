// yes
// Program type checked successfully
class T1{
	public static void main(String[] args){
        System.out.println(new A().foo());
    }
}

class A{
    public int foo(){
        A a;
        a = new A();
        return 1;
    }
}

class B{

}
