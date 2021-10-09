package smelldetector.smells;

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

}
