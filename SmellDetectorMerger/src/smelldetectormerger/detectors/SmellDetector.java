package smelldetectormerger.detectors;

import java.util.Set;

import smelldetector.smells.Smell;
import smelldetector.smells.SmellType;

public abstract class SmellDetector {
	
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
	 * @return a {@code Set} of detected smells
	 */
	public abstract Set<Smell> findSmells(SmellType smellType) throws Exception;

}
