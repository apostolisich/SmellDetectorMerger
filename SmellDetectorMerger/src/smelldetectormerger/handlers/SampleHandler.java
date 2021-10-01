package smelldetectormerger.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;

import smelldetectormerger.Activator;
import smelldetectormerger.detectors.CheckStyleSmellDetector;
import smelldetectormerger.detectors.DuDeSmellDetector;
import smelldetectormerger.detectors.JDeodorantSmellDetector;
import smelldetectormerger.detectors.JSpIRITSmellDetector;
import smelldetectormerger.detectors.PMDSmellDetector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		IProject selectedProject = (IProject) (((StructuredSelection) selection).getFirstElement());
		IJavaProject javaProject = JavaCore.create(selectedProject);
		Bundle bundle = Activator.getDefault().getBundle();
		
//		PMDSmellDetector.findSmells(bundle, javaProject);
		CheckStyleSmellDetector.findSmells(bundle, javaProject);
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
