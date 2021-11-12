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
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Bundle;

import smelldetectormerger.Activator;
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
	private JavaProject selectedProject;
	private List<SmellDetector> smellDetectors;
	private Map<SmellType, Set<Smell>> detectedSmells;
	private ScopedPreferenceStore scopedPreferenceStore;
	
	public SmellDetectionManager(SmellType smellType, JavaProject selectedProject) {
		this.smellTypeToBeDetected = smellType;
		this.selectedProject = selectedProject;
		this.bundle = Activator.getDefault().getBundle();
		scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "SmellDetectorMerger");
		initialiseSmellDetectors();
	}
	
	private void initialiseSmellDetectors() {
		IProject iProject = selectedProject.getProject();
		IJavaProject iJavaProject = JavaCore.create(iProject);
		
		smellDetectors = new ArrayList<>(5);
		if(scopedPreferenceStore.getString(PreferenceConstants.USE_ALL_DETECTORS).equals("yes")) {
			smellDetectors.add(new PMDSmellDetector(bundle, iJavaProject));
			smellDetectors.add(new CheckStyleSmellDetector(bundle, iJavaProject));
			smellDetectors.add(new DuDeSmellDetector(bundle, iJavaProject));
			smellDetectors.add(new JSpIRITSmellDetector(iProject, iJavaProject));
			smellDetectors.add(new JDeodorantSmellDetector(bundle, iJavaProject));
			smellDetectors.add(new OrganicSmellDetector(iJavaProject));
		} else {
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.PMD_ENABLED))
				smellDetectors.add(new PMDSmellDetector(bundle, iJavaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.CHECKSTYLE_ENABLED))
				smellDetectors.add(new CheckStyleSmellDetector(bundle, iJavaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.DUDE_ENABLED))
				smellDetectors.add(new DuDeSmellDetector(bundle, iJavaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.JSPIRIT_ENABLED))
				smellDetectors.add(new JSpIRITSmellDetector(iProject, iJavaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.JDEODORANT_ENABLED))
				smellDetectors.add(new JDeodorantSmellDetector(bundle, iJavaProject));
			if(scopedPreferenceStore.getBoolean(PreferenceConstants.ORGANIC_ENABLED))
				smellDetectors.add(new OrganicSmellDetector(iJavaProject));
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
						if(progressMonitor.isCanceled()) {
							break;
						}
							
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
			Utils.openNewMessageDialog("An unexpected error occured. Please try again...");
		}
	}
	
	/**
	 * Adds the smells view to the workbench and then fills the view with the detected
	 * smells data.
	 */
	public void displayDetectedSmells() {
		try {
			if(detectedSmells.isEmpty()) {
				Utils.openNewMessageDialog("No smells were detected for the selected project...");
			} else {
				SmellsView smellsView = (SmellsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().
						getActivePage().showView("smelldetectormerger.views.SmellsView");
				
				smellsView.addDetectedSmells(detectedSmells);
			}
		} catch (PartInitException e1) {
			Utils.openNewMessageDialog("An unexpected error occurred during the display of the detected smells. Please try again...");
		}
	}

}
