package smelldetector.smells;

import org.eclipse.core.resources.IFile;

/**
 * Marker interface to indicate classes that represent smells
 */
public abstract class Smell {
	
	protected IFile targetIFile;
	protected int targetStartLine;
	protected int targetEndLine;
	
	public abstract String getAffectedElementName();
	
	public abstract String getSmellTypeName();
	
	public IFile getTargetIFile() {
		return targetIFile;
	};
	
	public int getTargetStartLine() {
		return targetStartLine;
	};
	
	public int getTargetEndLine() {
		return targetEndLine;
	};
	
}
