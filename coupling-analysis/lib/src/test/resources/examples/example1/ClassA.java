package examples.example1;

/**
 * Test class to create sample calls for Method-to-Method Coupling
 */
public class ClassA {

	ClassD cD;
	
	public void methodA() {
		ClassB classB = new ClassB();
		// Methodcall from Object
		classB.methodB();
		
		// static Methodcall
		ClassC.staticMethod();
		
		// Methodcall from Field
		cD.methodD();
	}
	
}
