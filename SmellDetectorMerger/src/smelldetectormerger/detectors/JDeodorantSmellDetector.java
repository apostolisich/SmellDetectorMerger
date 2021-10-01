package smelldetectormerger.detectors;

import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;

import gr.uom.java.ast.util.TopicFinder;
import gr.uom.java.distance.CandidateRefactoring;
import gr.uom.java.distance.ExtractClassCandidateGroup;
import gr.uom.java.jdeodorant.refactoring.manipulators.ASTSliceGroup;
import gr.uom.java.jdeodorant.refactoring.manipulators.TypeCheckEliminationGroup;
import gr.uom.java.jdeodorant.refactoring.views.FeatureEnvy;
import gr.uom.java.jdeodorant.refactoring.views.GodClass;
import gr.uom.java.jdeodorant.refactoring.views.LongMethod;
import gr.uom.java.jdeodorant.refactoring.views.TypeChecking;

public class JDeodorantSmellDetector {
	
	public static void findSmells(Bundle bundle, IJavaProject javaProject) {
		GodClass gd = new GodClass();
		gd.setActiveProject(javaProject);
		TopicFinder.setStopWordsFileUrl(bundle.getEntry("essentials/glasgowstoplist.txt"));
		ExtractClassCandidateGroup[] godClasses = gd.getTable();
		
		LongMethod lm = new LongMethod();
		lm.setActiveProject(javaProject);
		ASTSliceGroup[] sliceGroup = lm.getTable();
		
		TypeChecking tc = new TypeChecking();
		tc.setActiveProject(javaProject);
		TypeCheckEliminationGroup[] typeCheckings = tc.getTable();
		
		FeatureEnvy fd = new FeatureEnvy();
		fd.setActiveProject(javaProject);
		CandidateRefactoring[] featureEnvies = fd.getTable();
	}

}
