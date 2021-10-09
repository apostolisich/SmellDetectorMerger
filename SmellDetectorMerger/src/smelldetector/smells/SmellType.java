package smelldetector.smells;

public enum SmellType {
	
	GOD_CLASS("God Class"),
	LONG_METHOD("Long Method"),
	LONG_PARAMETER_LIST("Long Parameter List"),
	FEATURE_ENVY("Feature Envy"),
	DUPLICATE_CODE("Duplicate Code"),
	TYPE_CHECKING("Type Checking");
	
	private final String smellName;
	
	private SmellType(String smellName) {
		this.smellName = smellName;
	}

	public String getName() {
		return smellName;
	}
}
