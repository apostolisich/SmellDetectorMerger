package smelldetector.smells;

import org.eclipse.core.resources.IFile;

public class LongParameterList extends Smell {
	
	private String className;
	private String methodName;
	
	public LongParameterList(String className, String methodName, IFile targetIFile, int targetStartLine) {
		this.className = className;
		this.methodName = methodName;
		this.targetIFile = targetIFile;
		this.targetStartLine = targetStartLine;
		this.targetEndLine = 0;
	}
	
	@Override
	public String getAffectedElementName() {
		return String.format("%s.%s", className, methodName);
	}

	@Override
	public String getSmellTypeName() {
		return SmellType.LONG_PARAMETER_LIST.getName();
	}

}
