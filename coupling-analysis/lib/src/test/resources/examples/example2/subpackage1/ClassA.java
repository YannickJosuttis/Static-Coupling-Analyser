package examples.example2.subpackage1;

import examples.example2.subpackage2.ClassC;

/**
 * Test class to create sample calls for Method-to-Method, Package and Import Coupling
 */
public class ClassA {

	public void method1(ClassB classB) {
		// Methodcall to same packages
		classB.method();
	}
	
	public void method2(examples.example2.subpackage2.ClassB classB) {
		// Methodcall to different package
		classB.method();
	}
	
	public void method3(ClassC classC) {
		// Methodcall with imported class
		classC.method();
	}
	
}
