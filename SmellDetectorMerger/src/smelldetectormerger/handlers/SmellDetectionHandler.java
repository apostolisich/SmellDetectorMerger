package smelldetectormerger.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import smelldetectormerger.detectionmanager.SmellDetectionManager;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class SmellDetectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject selectedProject = getSelectedProject(event);
		if(selectedProject == null)
			return null;
		
		SmellType selectedSmellType = Utils.getSmellTypeFromName(event);
		if(selectedSmellType == null) {
			Utils.openErrorMessageDialog("Unexpected smell type has been selected. Please try again...");
			return null;
		}
		
		SmellDetectionManager smellDetectionManager = new SmellDetectionManager(selectedSmellType, selectedProject);
		smellDetectionManager.detectCodeSmells();
		smellDetectionManager.displayDetectedSmells();

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
			Utils.openErrorMessageDialog("Please right click on the project's root folder and try again...");
			return null;
		}
		
		return selectedProject;
	}
	
}
