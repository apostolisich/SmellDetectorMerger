package smelldetectormerger.detectionmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import smelldetectormerger.detectors.CheckStyleSmellDetector;
import smelldetectormerger.detectors.DuDeSmellDetector;
import smelldetectormerger.detectors.JDeodorantSmellDetector;
import smelldetectormerger.detectors.JSpIRITSmellDetector;
import smelldetectormerger.detectors.PMDSmellDetector;
import smelldetectormerger.detectors.SmellDetector;
import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.views.SmellsView;

public class SmellDetectionManager {
	
	private SmellType smellTypeToBeDetected;
	private Bundle bundle;
	private IProject selectedProject;
	private List<SmellDetector> smellDetectors;
	private Map<SmellType, Set<Smell>> detectedSmells;
	
	public SmellDetectionManager(SmellType smellType, Bundle bundle, IProject selectedProject) {
		this.smellTypeToBeDetected = smellType;
		this.bundle = bundle;
		this.selectedProject = selectedProject;
		initialiseSmellDetectors();
	}
	
	private void initialiseSmellDetectors() {
		IJavaProject javaProject = JavaCore.create(selectedProject);
		
		smellDetectors = new ArrayList<>(5);
		smellDetectors.add(new PMDSmellDetector(bundle, javaProject));
		smellDetectors.add(new CheckStyleSmellDetector(bundle, javaProject));
		smellDetectors.add(new DuDeSmellDetector(bundle, javaProject));
		smellDetectors.add(new JSpIRITSmellDetector(selectedProject, javaProject));
		smellDetectors.add(new JDeodorantSmellDetector(bundle, javaProject));
	}
	
	public void detectCodeSmells() {
		detectedSmells = new HashMap<>();
		
		for(SmellDetector detector: smellDetectors) {
			if(smellTypeToBeDetected == SmellType.ALL_SMELLS || detector.getSupportedSmellTypes().contains(smellTypeToBeDetected)) {
				try {
					detector.findSmells(smellTypeToBeDetected, detectedSmells);
				} catch (Exception e) {
					//Ignore if an error is thrown. The flow should continue with the rest of the tools.
				}
			}
		}
	}
	
	public void displayDetectedSmells() {
		try {
			SmellsView smellsView = (SmellsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().
					getActivePage().showView("smelldetectormerger.views.SmellsView");
			
			smellsView.addDetectedSmells(detectedSmells);
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}
	}

}
