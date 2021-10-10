package smelldetector.smells;

import java.util.Objects;

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
	}

	@Override
	public int hashCode() {
		return Objects.hash(targetEndLine, targetIFile, targetStartLine);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Smell other = (Smell) obj;
		return targetEndLine == other.targetEndLine && targetStartLine == other.targetStartLine;
	};
	
}
