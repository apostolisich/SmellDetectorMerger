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
import gr.uom.java.distance.ExtractClassCandidateRefactoring;
import gr.uom.java.distance.MoveMethodCandidateRefactoring;
import gr.uom.java.jdeodorant.refactoring.manipulators.ASTSlice;
import gr.uom.java.jdeodorant.refactoring.manipulators.ASTSliceGroup;
import gr.uom.java.jdeodorant.refactoring.manipulators.TypeCheckElimination;
import gr.uom.java.jdeodorant.refactoring.manipulators.TypeCheckEliminationGroup;
import gr.uom.java.jdeodorant.refactoring.views.FeatureEnvy;
import gr.uom.java.jdeodorant.refactoring.views.GodClass;
import gr.uom.java.jdeodorant.refactoring.views.LongMethod;
import gr.uom.java.jdeodorant.refactoring.views.TypeChecking;
import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;
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
		if(smellType == SmellType.ALL_SMELLS) {
			extractGodClassSmells(SmellType.GOD_CLASS, detectedSmells);
			extractLongMethodSmells(SmellType.LONG_METHOD, detectedSmells);
			extractFeatureEnvySmells(SmellType.FEATURE_ENVY, detectedSmells);
			extractTypeCheckingSmells(SmellType.TYPE_CHECKING, detectedSmells);
		} else if(smellType == SmellType.GOD_CLASS) {
			extractGodClassSmells(smellType, detectedSmells);
		} else if(smellType == SmellType.LONG_METHOD) {
			extractLongMethodSmells(smellType, detectedSmells);
		} else if(smellType == SmellType.FEATURE_ENVY) {
			extractFeatureEnvySmells(smellType, detectedSmells);
		} else {
			extractTypeCheckingSmells(smellType, detectedSmells);
		}
	}
	
	private void extractGodClassSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		GodClass godClass = new GodClass();
		godClass.setActiveProject(javaProject);
		TopicFinder.setStopWordsFileUrl(bundle.getEntry("essentials/glasgowstoplist.txt"));
		ExtractClassCandidateGroup[] godClassSmells = godClass.getTable();
		
		for(ExtractClassCandidateGroup group: godClassSmells) {
			//All candidates in a group refer to the same smell, so we only get the first one here
			ExtractClassCandidateRefactoring candidate = group.getCandidates().get(0);
			
			String source = candidate.getSource();
			String className = source.substring(source.lastIndexOf('.') + 1);
			IFile targetIFile = candidate.getSourceIFile();
			
			IJavaElement javaElement = candidate.getSourceClassTypeDeclaration().resolveBinding().getJavaElement();
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
			int startLine = Utils.getLineNumFromOffset(cu, candidate.getSourceClassTypeDeclaration().getStartPosition());
			
			Utils.addSmell(smellType, detectedSmells, getDetectorName(),
					Utils.createSmellObject(smellType, className, targetIFile, startLine));
		}
	}
	
	private void extractLongMethodSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		LongMethod longMethod = new LongMethod();
		longMethod.setActiveProject(javaProject);
		ASTSliceGroup[] longMethods = longMethod.getTable();
		
		for(ASTSliceGroup group: longMethods) {
			//All candidates in a group refer to the same smell, so we only get the first one here
			ASTSlice candidate = group.getCandidates().iterator().next();
				
			String className = candidate.getSourceTypeDeclaration().getName().getIdentifier();
			String methodName = candidate.getSourceMethodDeclaration().getName().getIdentifier();
			IFile targetIFile = candidate.getIFile();
			
			IJavaElement javaElement = candidate.getSourceMethodDeclaration().resolveBinding().getJavaElement();
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
			int startLine = Utils.getLineNumFromOffset(cu, candidate.getSourceMethodDeclaration().getStartPosition());
			
			Utils.addSmell(smellType, detectedSmells, getDetectorName(),
					Utils.createSmellObject(smellType, className, methodName, targetIFile, startLine));
		}
	}
	
	private void extractFeatureEnvySmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		FeatureEnvy featureEnvy = new FeatureEnvy();
		featureEnvy.setActiveProject(javaProject);
		CandidateRefactoring[] featureEnvySmells = featureEnvy.getTable();
		
		for(CandidateRefactoring candidateRefactoring: featureEnvySmells) {
			MoveMethodCandidateRefactoring moveMethodCandidate = (MoveMethodCandidateRefactoring) candidateRefactoring;
			
			String sourceClass = moveMethodCandidate.getSourceClass().getName();
			String className = sourceClass.substring(sourceClass.lastIndexOf('.') + 1);
			String methodName = moveMethodCandidate.getMovedMethodName();
			
			IFile targetIFile = moveMethodCandidate.getSourceIFile();
			
			IJavaElement javaElement = candidateRefactoring.getSourceClassTypeDeclaration().resolveBinding().getJavaElement();
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
			int startLine = Utils.getLineNumFromOffset(cu, candidateRefactoring.getPositions().get(0).getOffset());

			Utils.addSmell(smellType, detectedSmells, getDetectorName(),
					Utils.createSmellObject(smellType, className, methodName, targetIFile, startLine));
		}
	}
	
	private void extractTypeCheckingSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		TypeChecking typeChecking = new TypeChecking();
		typeChecking.setActiveProject(javaProject);
		TypeCheckEliminationGroup[] typeCheckingSmells = typeChecking.getTable();
		
		for(TypeCheckEliminationGroup group: typeCheckingSmells) {
			//All candidates in a group refer to the same smell, so we only get the first one here
			TypeCheckElimination candidate = group.getCandidates().get(0);
			
			String className = candidate.getTypeCheckClass().getName().getIdentifier();
			String methodName = candidate.getTypeCheckMethod().getName().getIdentifier();
			IFile targetIFile = candidate.getTypeCheckIFile();
			
			IJavaElement javaElement = candidate.getTypeCheckClass().resolveBinding().getJavaElement();
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
			int startLine = Utils.getLineNumFromOffset(cu, candidate.getTypeCheckMethod().getStartPosition());
			
			Utils.addSmell(smellType, detectedSmells, getDetectorName(),
					Utils.createSmellObject(smellType, className, methodName, targetIFile, startLine));
		}
	}
	
}
