package examples.example7;

public class ClassB3 {
	
	public static int add( int x, int y) {
		return x+y;
	}

	public static class LambdaBiConsumer {
		
		public Lambda consume(Lambda lambda) {
			return (x,y) -> {
				return lambda.lambda(x, y);
			};
		}
	}
	
	@FunctionalInterface
	public interface Lambda{
		public int lambda(ClassB4 x, ClassB4 y);
	}
}
