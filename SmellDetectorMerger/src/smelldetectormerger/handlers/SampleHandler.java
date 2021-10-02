package smelldetectormerger.handlers;

import java.util.List;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;

import smelldetector.smells.SmellType;
import smelldetector.smells.Smellable;
import smelldetectormerger.Activator;
import smelldetectormerger.detectors.PMDSmellDetector;

public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		IProject selectedProject = (IProject) (((StructuredSelection) selection).getFirstElement());
		IJavaProject javaProject = JavaCore.create(selectedProject);
		Bundle bundle = Activator.getDefault().getBundle();
		
		PMDSmellDetector pmdDetector = new PMDSmellDetector(bundle, javaProject);
		List<Smellable> detectedSmells = null;
		try {
			detectedSmells = pmdDetector.findSmells(SmellType.DUPLICATE_CODE);
		} catch(Exception e) {
			//Ignore if an error is thrown. The flow should continue with the rest of the tools.
		}
		
//		CheckStyleSmellDetector.findSmells(bundle, javaProject);
//		JSpIRITSmellDetector.findSmells(selectedProject);
//		JDeodorantSmellDetector.findSmells(bundle, javaProject);
//		DuDeSmellDetector.findSmells(bundle, javaProject);
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(
				window.getShell(),
				"SmellDetectorMerger",
				"Hello, Eclipse world");
		return null;
	}
}
