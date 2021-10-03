package smelldetectormerger.handlers;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;

import smelldetector.smells.SmellType;
import smelldetector.smells.Smellable;
import smelldetectormerger.Activator;
import smelldetectormerger.detectors.PMDSmellDetector;
import smelldetectormerger.views.SmellsView;

public class SmellDetectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject selectedProject = getSelectedProject(event);
		if(selectedProject == null)
			return null;
		
		IJavaProject javaProject = JavaCore.create(selectedProject);
		Bundle bundle = Activator.getDefault().getBundle();
		
		PMDSmellDetector pmdDetector = new PMDSmellDetector(bundle, javaProject);
		Set<Smellable> detectedSmells = null;
		try {
			detectedSmells = pmdDetector.findSmells(SmellType.DUPLICATE_CODE);
		} catch(Exception e) {
			//Ignore if an error is thrown. The flow should continue with the rest of the tools.
		}
		
//		CheckStyleSmellDetector.findSmells(bundle, javaProject);
//		JSpIRITSmellDetector.findSmells(selectedProject);
//		JDeodorantSmellDetector.findSmells(bundle, javaProject);
//		DuDeSmellDetector.findSmells(bundle, javaProject);
		
		try {
			SmellsView smellsView = (SmellsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("smelldetectormerger.views.SmellsView");
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Gets the selected project and displays an error message in case the user didn't trigger the tool via the
	 * selected project's root folder.
	 * 
	 * @param event the event that triggered the tool
	 * @return the project selected by the user for smell detection
	 * @throws ExecutionException
	 */
	private IProject getSelectedProject(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		
		IProject selectedProject = null;
		try {
			selectedProject = (IProject) (((StructuredSelection) selection).getFirstElement());
		} catch(ClassCastException ex) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(
					window.getShell(),
					"SmellDetectorMerger",
					"Please right click on the project's root folder and try again...");
			
			return null;
		}
		
		return selectedProject;
	}
}
