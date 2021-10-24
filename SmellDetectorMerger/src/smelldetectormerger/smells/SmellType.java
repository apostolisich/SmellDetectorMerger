package smelldetectormerger.smells;

public enum SmellType {
	
	//The below is just a SmellType to represent cases that the user wants to detect all smells
	ALL_SMELLS(""),
	GOD_CLASS("God Class"),
	LONG_METHOD("Long Method"),
	LONG_PARAMETER_LIST("Long Parameter List"),
	FEATURE_ENVY("Feature Envy"),
	DUPLICATE_CODE("Duplicate Code"),
	BRAIN_CLASS("Brain Class"),
	BRAIN_METHOD("Brain Method"),
	DATA_CLASS("Data Class"),
	DISPERSE_COUPLING("Disperse Coupling"),
	INTENSIVE_COUPLING("Intensive Coupling"),
	REFUSED_PARENT_BEQUEST("Refused Parent Bequest"),
	SHOTGUN_SURGERY("Shotgun Surgery"),
	TRADITION_BREAKER("Tradition Breaker"),
	TYPE_CHECKING("Type Checking");
	
	private final String smellName;
	
	private SmellType(String smellName) {
		this.smellName = smellName;
	}

	public String getName() {
		return smellName;
	}
}
