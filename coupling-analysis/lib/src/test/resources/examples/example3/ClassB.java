package examples.example3;

/**
 * A Test class with a nested class.
 */
public class ClassB extends ClassC implements AnotherInterface{
	
	private String str;
	
	public void methodB1(String s) {
		str = s;		
	}
	public void methodB2() {
		System.out.println(str);
	}
	
	public void methodInterface() {
	}
	
	public void methodAnotherInterface() {
		
	}
	
	public class InnerClassB implements AnotherInterface{

		public void methodAnotherInterface() {
		}
		
	}

}
