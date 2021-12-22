package smelldetectormerger.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;

import smelldetectormerger.detectionmanager.SmellDetectionManager;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class SmellDetectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		JavaProject selectedProject = getSelectedProject(event);
		if(selectedProject == null)
			return null;
		
		SmellType selectedSmellType = getSelectedSmellType(event);
		if(selectedSmellType == null) {
			Utils.openNewMessageDialog("Unexpected smell type has been selected. Please try again...");
			return null;
		}
		
		SmellDetectionManager smellDetectionManager = new SmellDetectionManager(selectedSmellType, selectedProject);
		smellDetectionManager.extractCodeSmells();
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
	private JavaProject getSelectedProject(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		
		JavaProject selectedProject = null;
		try {
			selectedProject = (JavaProject) ((TreeSelection) selection).getFirstElement();
		} catch(ClassCastException ex) {
			Utils.openNewMessageDialog("Please right click on the root folder of a Java project and try again...");
			return null;
		}
		
		return selectedProject;
	}
	
	/**
	 * Extracts and returns the {@code SmellType} based on the selected option by the user.
	 * 
	 * @param event the event that triggered the tool
	 * @return the {@code SmellType} selected by the user
	 */
	private SmellType getSelectedSmellType(ExecutionEvent event) {
		try {
			String smellName = event.getCommand().getName();
			SmellType selectedSmellType = Utils.getSmellTypeFromName(smellName);
			
			return selectedSmellType;
		} catch (NotDefinedException e) {
			//If an error is returned, null is returned here and then a message dialog will be displayed later
			return null;
		}
	}
	
}
