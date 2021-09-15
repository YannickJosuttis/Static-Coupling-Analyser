package examples.example3;

/**
 * Example Test class for Inheritance and special Method-to-Method Coupling.
 */
public class ClassA extends ClassB implements Interface, AnotherInterface {

	public String a = "aaaaa";

	/**
	 * Creates a Testcase with speziell Method-to-Method Coupling
	 */
	public void methodA1() {
		ClassB classB = new ClassB();
		// count as M2M Coupling to ClassB by Source- and Bytecode
		classB.methodB1("ClassB");

		// count as M2M Coupling to ClassB by Source- and Bytecode analysis
		super.methodB2();

		// Bytecode count this not as Coupling, because it's an invokevirtuel without 'super'
		methodC(); 
		
		// Bytecode count this not as Coupling to ClassC but to ClassB 
		// because ClassC is SuperSuperClass from ClassA
		super.methodC();
	}

	/**
	 * Creates a Testcase with an Interface call with an casted Object from Interface
	 */
	public void methodA2() {
		ClassA cAInterfacer = new ClassA();
		Interface intf = (Interface) cAInterfacer;
		intf.methodInterface();
	}

	public void methodInterface() {
	}

	public void methodAnotherInterface() {
		super.methodB2();
	}

}
