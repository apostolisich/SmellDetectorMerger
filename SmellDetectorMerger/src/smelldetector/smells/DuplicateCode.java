package smelldetector.smells;

import java.util.Objects;

import org.eclipse.core.resources.IFile;

/**
 * A class that represents code duplications detected by the tools.
 */
public class DuplicateCode extends Smell {
	
	/** Each duplication belongs to a group of duplicates (same code in different places). This is recognised by the group id */
	private int duplicationGroupId;
	private String className;
	
	public DuplicateCode(int duplicationGroupId, String className, IFile targetIFile, int targetStartLine, int targetEndLine) {
		this.duplicationGroupId = duplicationGroupId;
		this.className = className;
		this.targetIFile = targetIFile;
		this.targetStartLine = targetStartLine;
		this.targetEndLine = targetEndLine;
	}
	
	@Override
	public String getAffectedElementName() {
		return String.format("Group %d, %s - Start: %s - End: %s", duplicationGroupId, className, targetStartLine, targetEndLine);
	}

	@Override
	public String getSmellTypeName() {
		return SmellType.DUPLICATE_CODE.getName();
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
		DuplicateCode other = (DuplicateCode) obj;
		return Objects.equals(className, other.className);
	}
	
}
