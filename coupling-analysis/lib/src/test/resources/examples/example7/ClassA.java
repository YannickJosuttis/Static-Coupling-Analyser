package examples.example7;

/**
 * Example class with a try-catch-finally block and a method chain.
 */
public class ClassA {

	public void method(int i) {
		try {
			i = 0;
		} catch (final IllegalStateException e) {
			i = 1;
		} finally {
			ClassA2.method2(i);
		}
	}

	public void method3() {
		int var = 42;
		ClassA3.funcA(ClassA3.funcB(ClassA3.funcC(var).funcD()), ClassA3.funcE());
	}

}
