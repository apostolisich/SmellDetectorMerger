package smelldetectormerger.utilities;

/**
 * A class that represents code duplications detected by the tools.
 */
public class Duplication {
	
	private String className;
	private int startLine;
	private int endLine;
	
	public Duplication(String className, int startLine, int endLine) {
		this.className = className;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public String getClassName() {
		return className;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}
	
}
