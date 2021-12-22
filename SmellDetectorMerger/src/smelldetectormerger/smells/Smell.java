package smelldetectormerger.smells;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;

import smelldetectormerger.utilities.Utils;

public class Smell {
	
	//This is only used for DuplicateCode smell type
	private final int duplicationGroupId;
	private final SmellType smellType;
	private final String className;
	private final String methodName;
	private final IFile targetIFile;
	private final int startLine;
	private final int endLine;
	
	private Set<String> detectorNamesSet;
	
	private Smell(Builder builder) {
		this.duplicationGroupId = builder.duplicationGroupId;
		this.smellType = builder.smellType;
		this.className = builder.className;
		this.methodName = builder.methodName;
		this.targetIFile = builder.targetIFile;
		this.startLine = builder.startLine;
		this.endLine = builder.endLine;
		
		detectorNamesSet = new TreeSet<>();
	}
	
	public String getAffectedElementName() {
		if(smellType == SmellType.DUPLICATE_CODE) {
			return String.format("Group %d %s - Start: %s - End: %s", duplicationGroupId, className, startLine, endLine);
		} else if(Utils.isClassSmell(smellType)) {
			return className;
		} else {
			return String.format("%s.%s()", className, methodName);
		}
	}
	
	public SmellType getSmellType() {
		return smellType;
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
	
	public int getDuplicationGroupId() {
		return duplicationGroupId;
	}
	
	public void addDetectorName(String detectorName) {
		detectorNamesSet.add(detectorName);
	}
	
	public String getDetectorNames() {
		StringBuilder builder = new StringBuilder();
		detectorNamesSet.forEach( detectorName -> {
			if(builder.length() != 0)
				builder.append(",");
			
			builder.append(detectorName);
		});
		
		return builder.toString();
	}

	@Override
	public int hashCode() {
		if(smellType == SmellType.DUPLICATE_CODE) {
			return Objects.hash(smellType, className, methodName, startLine, endLine);
		}
		
		return Objects.hash(smellType, className, methodName);
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
		
		if(smellType == SmellType.DUPLICATE_CODE && other.smellType == SmellType.DUPLICATE_CODE) {
			return smellType == other.smellType
					&& Objects.equals(className, other.className)
					&& Objects.equals(methodName, other.methodName)
					&& startLine == other.startLine
					&& endLine == other.endLine;
		}
		
		return smellType == other.smellType
				&& Objects.equals(className, other.className)
				&& Objects.equals(methodName, other.methodName);
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
