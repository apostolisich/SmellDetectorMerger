package smelldetector.smells;

import java.util.Objects;

import org.eclipse.core.resources.IFile;

public class BrainMethod extends Smell {

	private String className;
	private String methodName;
	
	public BrainMethod(String className, String methodName, IFile targetIFile, int targetStartLine) {
		this.className = className;
		this.methodName = methodName;
		this.targetIFile = targetIFile;
		this.targetStartLine = targetStartLine;
		this.targetEndLine = 0;
	}
	
	@Override
	public String getAffectedElementName() {
		return String.format("%s.%s()", className, methodName);
	}

	@Override
	public String getSmellTypeName() {
		return SmellType.BRAIN_METHOD.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(className, methodName);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BrainMethod other = (BrainMethod) obj;
		return Objects.equals(className, other.className) && Objects.equals(methodName, other.methodName);
	}
	
}