package examples.example4.subpackage2;

import examples.example4.subpackage1.ClassA;

/**
 * Example Test class with an Outer and Nested class
 */
public class OuterClass {
	
	public void methodOuter() {
		ClassA cA = new ClassA();
		
		// call to nested class, to check that this is not counted
		InnerClass ic = new InnerClass();
		ic.methodInner();
	}

	public class InnerClass {

		private ClassA cA2;
		
		public void methodInner() {
			ClassA cA = new ClassA();
		}
	}
}
