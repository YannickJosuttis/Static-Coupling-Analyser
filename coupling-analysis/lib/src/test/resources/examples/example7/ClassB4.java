package examples.example7;

public class ClassB4 {

	public int integer;
	
	public ClassB4(int integer) {
		this.integer = integer;
	}
	
	public int add (ClassB4 y) {
		return integer + y.integer;
	}
	
}
