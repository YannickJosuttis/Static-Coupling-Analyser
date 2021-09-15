package examples.example7;

import examples.example7.ClassB3.Lambda;
import examples.example7.ClassB3.LambdaBiConsumer;

/**
 * Example class with lambda expression and anonymous class.
 */
public class ClassB2 {

	public void method() {
		LambdaBiConsumer c = new LambdaBiConsumer();
		c.consume(new Lambda() {

			@Override
			public int lambda(ClassB4 x, ClassB4 y) {
				return x.add(y);
			}

		});
	}
}
