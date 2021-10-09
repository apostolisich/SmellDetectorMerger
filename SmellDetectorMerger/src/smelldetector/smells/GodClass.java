package smelldetector.smells;

import org.eclipse.core.resources.IFile;

public class GodClass extends Smell {

	private String className;
	
	public GodClass(String className,  IFile targetIFile, int targetStartLine) {
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
		return SmellType.GOD_CLASS.getName();
	}

}
