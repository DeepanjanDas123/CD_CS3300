// yes
// Program type checked successfully
//copy
class T3{
	public static void main(String[] args){
        System.out.println(new A().foo());
    }
}

class A{
    public int foo(){
        A a;
        A b;
        a = new A();
        b = a;         
        return 1;
    }
}

class B{

}
