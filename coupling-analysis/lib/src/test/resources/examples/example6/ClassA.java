package examples.example6;

public class ClassA {
	
	private ClassB cb = new ClassB();
	private Interface i;

	public void methodA() {
		cb.methodB();
		
		i.methodInterface();
	}
}
