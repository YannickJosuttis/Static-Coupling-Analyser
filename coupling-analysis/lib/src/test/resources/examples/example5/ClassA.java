package examples.example5;

/**
 * Example Test class for a method call to a nested class, to check if this is counted to its outer class.
 */
public class ClassA {

	OuterClass outerClass = new OuterClass();
	OuterClass.InnerClass innerClass = outerClass.new InnerClass();
	
	public void method() {
		innerClass.methodInner(8);
	}
	
}
