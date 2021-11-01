package smelldetectormerger.detectionmanager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Bundle;

import smelldetectormerger.detectors.CheckStyleSmellDetector;
import smelldetectormerger.detectors.DuDeSmellDetector;
import smelldetectormerger.detectors.JDeodorantSmellDetector;
import smelldetectormerger.detectors.JSpIRITSmellDetector;
import smelldetectormerger.detectors.OrganicSmellDetector;
import smelldetectormerger.detectors.PMDSmellDetector;
import smelldetectormerger.detectors.SmellDetector;
import smelldetectormerger.preferences.PreferenceConstants;
import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.utilities.Utils;
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
		
		smellDetectors = new ArrayList<>(5);
		if(scopedPreferenceStore.getString(PreferenceConstants.USE_ALL_DETECTORS).equals("yes")) {
			smellDetectors.add(new PMDSmellDetector(bundle, javaProject));
			smellDetectors.add(new CheckStyleSmellDetector(bundle, javaProject));
			smellDetectors.add(new DuDeSmellDetector(bundle, javaProject));
			smellDetectors.add(new JSpIRITSmellDetector(selectedProject, javaProject));
			smellDetectors.add(new JDeodorantSmellDetector(bundle, javaProject));
			smellDetectors.add(new OrganicSmellDetector(javaProject));
		} else {
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.PMD_ENABLED))
				smellDetectors.add(new PMDSmellDetector(bundle, javaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.CHECKSTYLE_ENABLED))
				smellDetectors.add(new CheckStyleSmellDetector(bundle, javaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.DUDE_ENABLED))
				smellDetectors.add(new DuDeSmellDetector(bundle, javaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.JSPIRIT_ENABLED))
				smellDetectors.add(new JSpIRITSmellDetector(selectedProject, javaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.JDEODORANT_ENABLED))
				smellDetectors.add(new JDeodorantSmellDetector(bundle, javaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.ORGANIC_ENABLED))
				smellDetectors.add(new OrganicSmellDetector(javaProject));
		}
	}
	
	public void detectCodeSmells() {
		detectedSmells = new HashMap<>();
		
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
					progressMonitor.beginTask("Detecting code smells...", smellDetectors.size());
					
					for(SmellDetector detector: smellDetectors) {
						if(smellTypeToBeDetected == SmellType.ALL_SMELLS || detector.getSupportedSmellTypes().contains(smellTypeToBeDetected)) {
							try {
								detector.findSmells(smellTypeToBeDetected, detectedSmells);
							} catch (Exception e) {
								//Ignore if an error is thrown. The flow should continue with the rest of the tools.
							}
						}
						progressMonitor.worked(1);
					}
					progressMonitor.done();
				}
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			Utils.openErrorMessageDialog("An unexpected error occured. Please try again...");
		}
	}
	
	/**
	 * Adds the smells view to the workbench and then fills the view with the detected
	 * smells data.
	 */
	public void displayDetectedSmells() {
		try {
			SmellsView smellsView = (SmellsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().
					getActivePage().showView("smelldetectormerger.views.SmellsView");
			
			smellsView.addDetectedSmells(detectedSmells);
		} catch (PartInitException e1) {
			Utils.openErrorMessageDialog("An unexpected error occurred during the display of the detected smells. Please try again...");
		}
	}

}
