package examples.example7;

import examples.example7.ClassB3.LambdaBiConsumer;

/**
 * Example class with a lambda expression
 */
public class ClassB {

	public void method() {
		LambdaBiConsumer c = new LambdaBiConsumer();
		c.consume((x, y) -> x.add(y));
	}

}
