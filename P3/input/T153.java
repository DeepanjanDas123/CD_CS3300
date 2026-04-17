// yes
// Program type checked successfully
// nested call with same method

import java.util.function.Function;

class Main{
    public static void main(String[] a){
        System.out.println((1 + 2) * 3);
	}
}

class Testcase {
    public int increment(int num, int factor){
		num = num + factor;
		return num;
    }

	public int test(){
		int a;
		a = 1;
		return this.increment(this.increment(this.increment(a, 1), 2), 3);
	}
}
