package smelldetectormerger.detectionmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
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
	private Map<SmellType, Integer> mapFromSmellNameToMaxToolCount;
	private ScopedPreferenceStore scopedPreferenceStore;
	
	public SmellDetectionManager(SmellType smellType, JavaProject selectedProject) {
		this.smellTypeToBeDetected = smellType;
		this.selectedProject = selectedProject;
		initialiseNecessaryClassFields();
	}
	
	private void initialiseNecessaryClassFields() {
		bundle = Activator.getDefault().getBundle();
		scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "SmellDetectorMerger");
		detectedSmells = new HashMap<>();
		mapFromSmellNameToMaxToolCount = new HashMap<>();
		
		IProject iProject = selectedProject.getProject();
		IJavaProject iJavaProject = JavaCore.create(iProject);
		
		smellDetectors = new ArrayList<>(6);
		boolean useAllDetectors = scopedPreferenceStore.getString(PreferenceConstants.USE_ALL_DETECTORS).equals("yes");
		if(useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.PMD_ENABLED)) {
			PMDSmellDetector pmdDetector = new PMDSmellDetector(bundle, iJavaProject);
			smellDetectors.add(pmdDetector);
			updateMaxToolCountMap(pmdDetector);
		}
		if(useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.CHECKSTYLE_ENABLED)) {
			CheckStyleSmellDetector checkStyleDetector = new CheckStyleSmellDetector(bundle, iJavaProject);
			smellDetectors.add(checkStyleDetector);
			updateMaxToolCountMap(checkStyleDetector);
		}
		if(useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.DUDE_ENABLED)) {
			DuDeSmellDetector dudeDetector = new DuDeSmellDetector(bundle, iJavaProject);
			smellDetectors.add(dudeDetector);
			updateMaxToolCountMap(dudeDetector);
		}
		if(useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.JSPIRIT_ENABLED)) {
			JSpIRITSmellDetector jspiritDetector = new JSpIRITSmellDetector(iProject, iJavaProject);
			smellDetectors.add(jspiritDetector);
			updateMaxToolCountMap(jspiritDetector);
		}
		if(useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.JDEODORANT_ENABLED)) {
			JDeodorantSmellDetector jdeodorantDetector = new JDeodorantSmellDetector(bundle, iJavaProject);
			smellDetectors.add(jdeodorantDetector);
			updateMaxToolCountMap(jdeodorantDetector);
		}
		if(useAllDetectors || scopedPreferenceStore.getBoolean(PreferenceConstants.ORGANIC_ENABLED)) {
			OrganicSmellDetector organicDetector = new OrganicSmellDetector(iJavaProject);
			smellDetectors.add(organicDetector);
			updateMaxToolCountMap(organicDetector);
		}
	}
	
	/**
	 * Parses the supported smell types for the given detector and updates the max count of
	 * the enabled tools which can detect each smell type.
	 * 
	 * @param detector the {@code SmellDetector} from which the smell types will be parsed
	 */
	private void updateMaxToolCountMap(SmellDetector detector) {
		detector.getSupportedSmellTypes().forEach( smellType -> {
			mapFromSmellNameToMaxToolCount.merge(smellType, 1, (oldValue, newValue) -> oldValue + newValue);
		});
	}
	
	public void extractCodeSmells() {
		if(smellTypeToBeDetected == SmellType.IMPORT_CSV) {
			extractCodeSmellsFromCsv();
		} else {
			detectCodeSmellsInSelectedProject();
		}
	}
	
	private void extractCodeSmellsFromCsv() {
		FileDialog fileExplorerDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
		fileExplorerDialog.setFilterExtensions(new String[] { "*.csv" });
		String selectedFilePath = fileExplorerDialog.open();
		
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(selectedFilePath)))) {
			String line;
			while((line = reader.readLine()) != null) {
				String[] smellData = line.split(",");
				SmellType smellType = Utils.getSmellTypeFromName(smellData[0]);
				String affectedElement = smellData[1];
				
				Smell newSmell;
				if(smellType == SmellType.DUPLICATE_CODE) {
					int groupId = Integer.parseInt(affectedElement.substring(6, affectedElement.indexOf(' ', 6)));
					String className = affectedElement.substring(affectedElement.indexOf(' ', 6) + 1, affectedElement.indexOf(" -"));
					int startLineIndex = affectedElement.indexOf("Start: ") + 7;
					int startLine = Integer.parseInt(affectedElement.substring(startLineIndex, affectedElement.indexOf(' ', startLineIndex)));
					int endLineIndex = affectedElement.indexOf("End: ") + 5;
					int endLine = Integer.parseInt(affectedElement.substring(endLineIndex));
					
					newSmell = Utils.createSmellObject(smellType, groupId, className, null, startLine, endLine);
				} else if(Utils.isClassSmell(smellType)) {
					newSmell = Utils.createSmellObject(smellType, affectedElement, null, 0);
				} else {
					String[] affectedElementParts = affectedElement.split("\\.");
					newSmell = Utils.createSmellObject(smellType, affectedElementParts[0], affectedElementParts[1].replace("()", ""), null, 0);
				}
				
				for(int i = 2; i < smellData.length; i++) {
					newSmell.addDetectorName(smellData[i].trim());
				}
				
				if(!detectedSmells.containsKey(smellType))
					detectedSmells.put(smellType, new LinkedHashSet<Smell>());
				
				detectedSmells.get(smellType).add(newSmell);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Utils.openNewMessageDialog("An error occured while reading the csv file. Please try again...");
		} catch (Exception e) {
			e.printStackTrace();
			Utils.openNewMessageDialog("An error occured while importing data. Please try again...");
		}
	}
	
	private void detectCodeSmellsInSelectedProject() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
					progressMonitor.beginTask("Detecting code smells...", smellDetectors.size());
					
					for(SmellDetector detector: smellDetectors) {
						if(smellTypeToBeDetected == SmellType.ALL_SMELLS || detector.getSupportedSmellTypes().contains(smellTypeToBeDetected)) {
							executeDetector(detector, progressMonitor);
						}
					}
					
					progressMonitor.done();
				}
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			Utils.openNewMessageDialog("An unexpected error occured. Please try again...");
		}
	}
	
	/**
	 * Executes the smell detection for the given detector and updates the progress
	 * in the progress bar after the detection is finished.
	 * 
	 * @param detector the smell detector that will check for smells
	 * @param progressMonitor a progress bar dialog that reports the progress of detection
	 */
	private void executeDetector(SmellDetector detector, IProgressMonitor progressMonitor) {
			if(progressMonitor.isCanceled()) {
				return;
			}
			
			try {
				progressMonitor.subTask("Current Detector: " + detector.getDetectorName());
				detector.findSmells(smellTypeToBeDetected, detectedSmells);
			} catch (Exception e) {
				//Ignore if an error is thrown. The flow should continue with the rest of the tools.
			}
			
			progressMonitor.worked(1);
	}
	
	/**
	 * Adds the smells view to the workbench and then fills the view with the detected
	 * smells data.
	 */
	public void displayDetectedSmells() {
		try {
			if(detectedSmells.isEmpty()) {
				Utils.openNewMessageDialog("No smells were detected for the selected project!");
			} else {
				SmellsView smellsView = (SmellsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().
						getActivePage().showView("smelldetectormerger.views.SmellsView");
				
				smellsView.addDetectedSmells(detectedSmells);
				smellsView.setMaxToolCountMap(mapFromSmellNameToMaxToolCount);
			}
		} catch (PartInitException e1) {
			Utils.openNewMessageDialog("An unexpected error occurred during the display of the detected smells. Please try again...");
		}
	}

}
