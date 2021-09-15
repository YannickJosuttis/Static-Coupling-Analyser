package examples.example4.subpackage1;

import examples.example4.subpackage2.OuterClass;

/**
 * Example Test class for imports and calls in a class with nested class.
 */
public class ClassA {

	OuterClass outerClass = new OuterClass();
	OuterClass.InnerClass innerClass = outerClass.new InnerClass();
	
	public void methodA1() {		
		innerClass.methodInner();
		outerClass.methodOuter();
	}
	
}
