package smelldetectormerger.detectors;

import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IProject;

import spirit.changes.manager.CodeChanges;
import spirit.core.design.AgglomerationManager;
import spirit.core.design.CodeSmellsManager;
import spirit.core.design.CodeSmellsManagerFactory;
import spirit.core.smells.CodeSmell;
import spirit.dependencies.DependencyVisitor;
import spirit.dependencies.Graph;
import spirit.metrics.storage.InvokingCache;

public class JSpIRITSmellDetector {

	public static void findSmells(IProject selectedProject) {
		CodeSmellsManagerFactory.getInstance().setCurrentProject(selectedProject);
		CodeSmellsManager codeSmellManager = CodeSmellsManagerFactory.getInstance().getCurrentProjectManager();
		codeSmellManager.initialize();
		InvokingCache.getInstance().initialize();
		AgglomerationManager agglomerationManager = AgglomerationManager.getInstance();
		agglomerationManager.setCurrentProject(selectedProject.getFullPath().toString());

		try {
			codeSmellManager.setVisitOnlyModified(false);
			Graph graph = DependencyVisitor.getInstance()
					.getGraph(CodeSmellsManagerFactory.getInstance().getCurrentProject().getName());
			if (!CodeChanges.getInstance().isNeedReScan() && graph.countVertexes() > 0) {
				CodeChanges changes = CodeChanges.getInstance();
				Set<String> modClasses = null;
				if (changes.anyChanges())
					modClasses = graph.getTouchedClasses(changes);
				if (modClasses != null) {
					codeSmellManager.setVisitOnlyModified(true);
					codeSmellManager.setModifiedClasses(modClasses);
				}
			} else {
				codeSmellManager.setVisitDependencies(true);
			}

			codeSmellManager.calculateMetricsCode();
			codeSmellManager.calculateAditionalMetrics();
			codeSmellManager.detectCodeSmells();

			Vector<CodeSmell> codeSmells = codeSmellManager.getSmells();
			int counter = 0;
			for (CodeSmell smell : codeSmells) {
				counter++;
				System.out.println("----------------" + "Smell: " + counter + " -------------");
				System.out.println("********* Affected Classes ************");
				System.out.println(smell.getAffectedClasses());
				System.out.println("********* Class ************");
				System.out.println(smell.getClass());
				System.out.println("********* Element ************");
				System.out.println(smell.getElement());
				System.out.println("********* Element name ************");
				System.out.println(smell.getElementName());
				System.out.println("********* Kind of smell name ************");
				System.out.println(smell.getKindOfSmellName());
				System.out.println("********* Line ************");
				System.out.println(smell.getLine());
				System.out.println("********* Main Class ************");
				System.out.println(smell.getMainClass());
				System.out.println("********* Main Class Name ************");
				System.out.println(smell.getMainClassName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
