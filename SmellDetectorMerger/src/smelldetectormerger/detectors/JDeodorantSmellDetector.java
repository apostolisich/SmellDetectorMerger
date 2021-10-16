package smelldetectormerger.detectors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;

import gr.uom.java.ast.util.TopicFinder;
import gr.uom.java.distance.CandidateRefactoring;
import gr.uom.java.distance.ExtractClassCandidateGroup;
import gr.uom.java.distance.MoveMethodCandidateRefactoring;
import gr.uom.java.jdeodorant.refactoring.manipulators.ASTSliceGroup;
import gr.uom.java.jdeodorant.refactoring.manipulators.TypeCheckEliminationGroup;
import gr.uom.java.jdeodorant.refactoring.views.FeatureEnvy;
import gr.uom.java.jdeodorant.refactoring.views.GodClass;
import gr.uom.java.jdeodorant.refactoring.views.LongMethod;
import gr.uom.java.jdeodorant.refactoring.views.TypeChecking;
import smelldetector.smells.Smell;
import smelldetector.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class JDeodorantSmellDetector extends SmellDetector {
	
	private Bundle bundle;
	private IJavaProject javaProject;
	
	public JDeodorantSmellDetector(Bundle bundle, IJavaProject javaProject) {
		this.bundle = bundle;
		this.javaProject = javaProject;
	}
	
	private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
			new HashSet<SmellType>(Arrays.asList(SmellType.GOD_CLASS,
												SmellType.LONG_METHOD,
												SmellType.FEATURE_ENVY,
												SmellType.TYPE_CHECKING)));
	
	@Override
	public Set<SmellType> getSupportedSmellTypes() {
		return SUPPORTED_SMELL_TYPES;
	}

	@Override
	public String getDetectorName() {
		return "JDeodorant";
	}

	@Override
	public void findSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		GodClass gd = new GodClass();
		gd.setActiveProject(javaProject);
		TopicFinder.setStopWordsFileUrl(bundle.getEntry("essentials/glasgowstoplist.txt"));
		ExtractClassCandidateGroup[] godClasses = gd.getTable();
		extractGodClassSmells(godClasses, detectedSmells);
		
		LongMethod lm = new LongMethod();
		lm.setActiveProject(javaProject);
		ASTSliceGroup[] longMethods = lm.getTable();
		extractLongMethodSmells(longMethods, detectedSmells);
		
		FeatureEnvy featureEnvy = new FeatureEnvy();
		featureEnvy.setActiveProject(javaProject);
		CandidateRefactoring[] featureEnvies = featureEnvy.getTable();
		extractFeatureEnvySmells(featureEnvies, detectedSmells);
		
		TypeChecking tc = new TypeChecking();
		tc.setActiveProject(javaProject);
		TypeCheckEliminationGroup[] typeCheckings = tc.getTable();
		extractTypeCheckingSmells(typeCheckings, detectedSmells);
	}
	
	private void extractGodClassSmells(ExtractClassCandidateGroup[] godClasses, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		SmellType godClassSmellType = SmellType.GOD_CLASS;
		
		for(ExtractClassCandidateGroup group: godClasses) {
			group.getCandidates().forEach( candidate -> {
				String source = candidate.getSource();
				String className = source.substring(source.lastIndexOf('.') + 1);
				IFile targetIFile = candidate.getSourceIFile();
				
				IJavaElement javaElement = candidate.getSourceClassTypeDeclaration().resolveBinding().getJavaElement();
				ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
				int startLine = Utils.getLineNumFromOffset(cu, candidate.getSourceClassTypeDeclaration().getStartPosition());
				
				try {
					Utils.addSmell(godClassSmellType, detectedSmells, getDetectorName(),
							Utils.createSmellObject(godClassSmellType, className, targetIFile, startLine));
				} catch (Exception e) {
					//Ignore
				}
			});
		}
	}
	
	private void extractLongMethodSmells(ASTSliceGroup[] longMethods, Map<SmellType, Set<Smell>> detectedSmells) {
		SmellType longMethodSmellType = SmellType.LONG_METHOD;
		
		for(ASTSliceGroup group: longMethods) {
			group.getCandidates().forEach( candidate -> {
				String className = candidate.getSourceTypeDeclaration().getName().getIdentifier();
				String methodName = candidate.getSourceMethodDeclaration().getName().getIdentifier();
				IFile targetIFile = candidate.getIFile();
				
				IJavaElement javaElement = candidate.getSourceMethodDeclaration().resolveBinding().getJavaElement();
				ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
				int startLine = Utils.getLineNumFromOffset(cu, candidate.getSourceMethodDeclaration().getStartPosition());
				
				try {
					Utils.addSmell(longMethodSmellType, detectedSmells, getDetectorName(),
							Utils.createSmellObject(longMethodSmellType, className, methodName, targetIFile, startLine));
				} catch (Exception e) {
					//Ignore
				}
			});
		}
	}
	
	private void extractFeatureEnvySmells(CandidateRefactoring[] featureEnvies, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		SmellType featureEnvySmellType = SmellType.FEATURE_ENVY;
		
		for(CandidateRefactoring candidateRefactoring: featureEnvies) {
			MoveMethodCandidateRefactoring moveMethodCandidate = (MoveMethodCandidateRefactoring) candidateRefactoring;
			
			String sourceClass = moveMethodCandidate.getSourceClass().getName();
			String className = sourceClass.substring(sourceClass.lastIndexOf('.') + 1);
			String methodName = moveMethodCandidate.getMovedMethodName();
			
			IFile targetIFile = moveMethodCandidate.getSourceIFile();
			
			IJavaElement javaElement = candidateRefactoring.getSourceClassTypeDeclaration().resolveBinding().getJavaElement();
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
			int startLine = Utils.getLineNumFromOffset(cu, candidateRefactoring.getPositions().get(0).getOffset());

			Utils.addSmell(featureEnvySmellType, detectedSmells, getDetectorName(),
					Utils.createSmellObject(featureEnvySmellType, className, methodName, targetIFile, startLine));
		}
	}
	
	private void extractTypeCheckingSmells(TypeCheckEliminationGroup[] typeCheckings, Map<SmellType, Set<Smell>> detectedSmells) {
		SmellType typeCheckingSmellType = SmellType.TYPE_CHECKING;
		
		for(TypeCheckEliminationGroup group: typeCheckings) {
			group.getCandidates().forEach( candidate -> {
				String className = candidate.getTypeCheckClass().getName().getIdentifier();
				String methodName = candidate.getTypeCheckMethod().getName().getIdentifier();
				IFile targetIFile = candidate.getTypeCheckIFile();
				
				IJavaElement javaElement = candidate.getTypeCheckClass().resolveBinding().getJavaElement();
				ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
				int startLine = Utils.getLineNumFromOffset(cu, candidate.getTypeCheckMethod().getStartPosition());
				
				try {
					Utils.addSmell(typeCheckingSmellType, detectedSmells, getDetectorName(),
							Utils.createSmellObject(typeCheckingSmellType, className, methodName, targetIFile, startLine));
				} catch (Exception e) {
					//Ignore
				}
			});
		}
	}
	
}
