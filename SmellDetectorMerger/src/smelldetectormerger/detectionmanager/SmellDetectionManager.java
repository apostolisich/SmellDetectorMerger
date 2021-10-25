package smelldetectormerger.detectionmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;

import smelldetectormerger.detectors.CheckStyleSmellDetector;
import smelldetectormerger.detectors.DuDeSmellDetector;
import smelldetectormerger.detectors.JDeodorantSmellDetector;
import smelldetectormerger.detectors.JSpIRITSmellDetector;
import smelldetectormerger.detectors.PMDSmellDetector;
import smelldetectormerger.detectors.SmellDetector;
import smelldetectormerger.preferences.PreferenceConstants;
import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.views.SmellsView;

public class SmellDetectionManager {
	
	private SmellType smellTypeToBeDetected;
	private Bundle bundle;
	private IProject selectedProject;
	private List<SmellDetector> smellDetectors;
	private Map<SmellType, Set<Smell>> detectedSmells;
	private ScopedPreferenceStore scopedPreferenceStore;
	
	public SmellDetectionManager(SmellType smellType, Bundle bundle, IProject selectedProject) {
		this.smellTypeToBeDetected = smellType;
		this.bundle = bundle;
		this.selectedProject = selectedProject;
		scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "SmellDetectorMerger");
		initialiseSmellDetectors();
	}
	
	private void initialiseSmellDetectors() {
		IJavaProject javaProject = JavaCore.create(selectedProject);
		
		boolean useAllDetectors = scopedPreferenceStore.getString(PreferenceConstants.USE_ALL_DETECTORS).equals("yes");
		
		smellDetectors = new ArrayList<>(5);
		smellDetectors.add(new PMDSmellDetector(bundle, javaProject,
				useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.PMD_ENABLED)));
		smellDetectors.add(new CheckStyleSmellDetector(bundle, javaProject,
				useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.CHECKSTYLE_ENABLED)));
		smellDetectors.add(new DuDeSmellDetector(bundle, javaProject,
				useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.DUDE_ENABLED)));
		smellDetectors.add(new JSpIRITSmellDetector(selectedProject, javaProject,
				useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.JSPIRIT_ENABLED)));
		smellDetectors.add(new JDeodorantSmellDetector(bundle, javaProject,
				useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.JDEODORANT_ENABLED)));
	}
	
	public void detectCodeSmells() {
		detectedSmells = new HashMap<>();
		
		for(SmellDetector detector: smellDetectors) {
			if(detector.isEnabled() && 
					(smellTypeToBeDetected == SmellType.ALL_SMELLS || detector.getSupportedSmellTypes().contains(smellTypeToBeDetected))) {
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
