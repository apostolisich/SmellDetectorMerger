package smelldetectormerger.detectors;

import java.util.Map;
import java.util.Set;

import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;

public abstract class SmellDetector {
	
	protected boolean isEnabled;
	
	/**
	 * A method that returns all the code smell types that can be found from the
	 * detector.
	 * 
	 * @return a {@code Set} of smell types
	 */
	public abstract Set<SmellType> getSupportedSmellTypes();
	
	/**
	 * A method that returns the name of the detector, which is used to differentiate
	 * the different detectors.
	 * 
	 * @return the name of the detector
	 */
	public abstract String getDetectorName();
	
	/**
	 * A method that is responsible for finding the code smells based on the
	 * given smell type.
	 * 
	 * @param smellType the smell type to check for
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 */
	public abstract void findSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception;
	
	/**
	 * A method that returns whether the detector is enabled via the Preferences selected by
	 * the user or not.
	 * 
	 * @return true if the detector is enabled; false otherwise
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

}
