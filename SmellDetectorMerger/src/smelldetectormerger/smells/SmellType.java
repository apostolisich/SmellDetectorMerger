package smelldetectormerger.smells;

public enum SmellType {
	
	IMPORT_CSV(""),
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
	DISPERSED_COUPLING("Dispersed Coupling"),
	INTENSIVE_COUPLING("Intensive Coupling"),
	REFUSED_PARENT_BEQUEST("Refused Parent Bequest"),
	SHOTGUN_SURGERY("Shotgun Surgery"),
	TRADITION_BREAKER("Tradition Breaker"),
	TYPE_CHECKING("Type Checking"),
	CLASS_DATA_SHOULD_BE_PRIVATE("Class Data Should Be Private"),
	COMPLEX_CLASS("Complex Class"),
	LAZY_CLASS("Lazy Class"),
	MESSAGE_CHAIN("Message Chain"),
	SPECULATIVE_GENERALITY("Speculative Generality"),
	SPAGHETTI_CODE("Spaghetti Code");
	
	private final String smellName;
	
	private SmellType(String smellName) {
		this.smellName = smellName;
	}

	public String getName() {
		return smellName;
	}
}
