package smelldetector.smells;

import java.util.Objects;

import org.eclipse.core.resources.IFile;

public class RefusedParentBequest extends Smell {
	
	private String className;
	
	public RefusedParentBequest(String className, IFile targetIFile, int targetStartLine) {
		this.className = className;
		this.targetIFile = targetIFile;
		this.targetStartLine = targetStartLine;
		this.targetEndLine = 0;
	}

	@Override
	public String getAffectedElementName() {
		return className;
	}

	@Override
	public String getSmellTypeName() {
		return SmellType.REFUSED_PARENT_BEQUEST.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(className);
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
		RefusedParentBequest other = (RefusedParentBequest) obj;
		return Objects.equals(className, other.className);
	}
	
}