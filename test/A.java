package test;

import test.depth1.B;
import test.depth1.depth2.C;

class A {
	public B doSomething(String irrelevant) {
		C c = new C();
		Neighbor flenders = new Neighbor();
		flenders.hiDiddlyHo();
		System.out.println("Nothing has been achieved!");
	}
	
	public static void staticMethod() {
		// Here be dragons!
	}
}