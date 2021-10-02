package smelldetector.smells;

import java.util.List;

import smelldetectormerger.utilities.Duplication;

public class DuplicateCode implements Smellable {

	private List<Duplication> duplications;
	
	public DuplicateCode(List<Duplication> duplications) {
		this.duplications = duplications;
	}

	public List<Duplication> getDuplications() {
		return duplications;
	}
	
}
