package smelldetector.smells;

public class LongMethod implements Smellable {

	private String className;
	private String methodName;
	
	public LongMethod(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
}
