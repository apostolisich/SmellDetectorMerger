package smelldetector.smells;

import java.util.Objects;

import org.eclipse.core.resources.IFile;

import smelldetectormerger.utilities.Utils;

public class Smell {
	
	//This is only used for DuplicateCode smell type
	private int duplicationGroupId;
	private final SmellType smellType;
	private final String className;
	private final String methodName;
	private final IFile targetIFile;
	private final int startLine;
	private final int endLine;
	
	private Smell(Builder builder) {
		this.duplicationGroupId = builder.duplicationGroupId;
		this.smellType = builder.smellType;
		this.className = builder.className;
		this.methodName = builder.methodName;
		this.targetIFile = builder.targetIFile;
		this.startLine = builder.startLine;
		this.endLine = builder.endLine;
	}
	
	public String getAffectedElementName() {
		if(smellType == SmellType.DUPLICATE_CODE) {
			return String.format("Group %d, %s - Start: %s - End: %s", duplicationGroupId, className, startLine, endLine);
		} else if(Utils.isClassSmell(smellType)) {
			return className;
		} else {
			return String.format("%s.%s()", className, methodName);
		}
	}
	
	public String getSmellTypeName() {
		return smellType.getName();
	}
	
	public IFile getTargetIFile() {
		return targetIFile;
	}
	
	public int getTargetStartLine() {
		return startLine;
	}
	
	public int getTargetEndLine() {
		return endLine;
	}

	public int hashCode() {
		return Objects.hash(className, endLine, methodName, smellType, startLine, targetIFile);
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
		return Objects.equals(className, other.className) && endLine == other.endLine
				&& Objects.equals(methodName, other.methodName) && smellType == other.smellType
				&& startLine == other.startLine;
	}
	
	public static class Builder {
		//This is only used for DuplicateCode smell type
		private int duplicationGroupId;
		private final SmellType smellType;
		private String className;
		private String methodName;
		private IFile targetIFile;
		private int startLine;
		private int endLine;
		
		public Builder(SmellType smellType) {
			this.smellType = smellType;
		}
		
		public Builder setDuplicationGroupId(int duplicationGroupId) {
			this.duplicationGroupId = duplicationGroupId;
			return this;
		}
		
		public Builder setClassName(String className) {
			this.className = className;
			return this;
		}
		
		public Builder setMethodName(String methodName) {
			this.methodName = methodName;
			return this;
		}
		
		public Builder setTargetIFile(IFile targetIFile) {
			this.targetIFile = targetIFile;
			return this;
		}
		
		public Builder setStartLine(int startLine) {
			this.startLine = startLine;
			return this;
		}
		
		public Builder setEndLine(int endLine) {
			this.endLine = endLine;
			return this;
		}
		
		public Smell build() {
			return new Smell(this);
		}
	}

}
