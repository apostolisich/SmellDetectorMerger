package smelldetectormerger.detectors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import br.pucrio.opus.smells.Organic;
import br.pucrio.opus.smells.collector.SmellName;
import br.pucrio.opus.smells.resources.Method;
import br.pucrio.opus.smells.resources.Type;
import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class OrganicSmellDetector extends SmellDetector {

	private IJavaProject javaProject;
	
	public OrganicSmellDetector(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}
	
	private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
			new HashSet<SmellType>(Arrays.asList(SmellType.CLASS_DATA_SHOULD_BE_PRIVATE,
												 SmellType.COMPLEX_CLASS,
												 SmellType.FEATURE_ENVY,
												 SmellType.GOD_CLASS,
												 SmellType.LAZY_CLASS,
												 SmellType.LONG_METHOD,
												 SmellType.LONG_PARAMETER_LIST,
												 SmellType.MESSAGE_CHAIN,
												 SmellType.REFUSED_PARENT_BEQUEST,
												 SmellType.SPECULATIVE_GENERALITY,
												 SmellType.SPAGHETTI_CODE,
												 SmellType.DISPERSED_COUPLING,
												 SmellType.INTENSIVE_COUPLING,
												 SmellType.BRAIN_CLASS,
												 SmellType.SHOTGUN_SURGERY,
												 SmellType.BRAIN_METHOD,
												 SmellType.DATA_CLASS)));
	@Override
	public Set<SmellType> getSupportedSmellTypes() {
		return SUPPORTED_SMELL_TYPES;
	}

	@Override
	public String getDetectorName() {
		return "Organic";
	}
	
	private static final Map<SmellName, SmellType> MAP_FROM_SMELLNAME_TO_SMELLTYPE;
	static {
		MAP_FROM_SMELLNAME_TO_SMELLTYPE = new HashMap<>(17);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.ClassDataShouldBePrivate, SmellType.CLASS_DATA_SHOULD_BE_PRIVATE);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.ComplexClass, SmellType.COMPLEX_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.FeatureEnvy, SmellType.FEATURE_ENVY);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.GodClass, SmellType.GOD_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.LazyClass, SmellType.LAZY_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.LongMethod, SmellType.LONG_METHOD);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.LongParameterList, SmellType.LONG_PARAMETER_LIST);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.MessageChain, SmellType.MESSAGE_CHAIN);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.RefusedBequest, SmellType.REFUSED_PARENT_BEQUEST);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.SpeculativeGenerality, SmellType.SPECULATIVE_GENERALITY);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.SpaghettiCode, SmellType.SPAGHETTI_CODE);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.DispersedCoupling, SmellType.DISPERSED_COUPLING);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.IntensiveCoupling, SmellType.INTENSIVE_COUPLING);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.BrainClass, SmellType.BRAIN_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.ShotgunSurgery, SmellType.SHOTGUN_SURGERY);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.BrainMethod, SmellType.BRAIN_METHOD);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.DataClass, SmellType.DATA_CLASS);
	}

	@Override
	public void findSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		Organic organicPlugin = new Organic();
		String sourcePath = javaProject.getCorrespondingResource().getLocation().toString();
		
		List<Type> classTypeDeclarations = organicPlugin.loadAllTypes(Collections.singletonList(sourcePath));
		organicPlugin.collectTypeMetrics(classTypeDeclarations);
		organicPlugin.detectSmells(classTypeDeclarations);
		classTypeDeclarations = organicPlugin.onlySmelly(classTypeDeclarations);
		
		for(Type classTypeDeclaration: classTypeDeclarations) {
			String fullClassPath = classTypeDeclaration.getFullyQualifiedName();
			String className = fullClassPath.substring(fullClassPath.lastIndexOf('.') + 1);
			
			String sourceFilePath = classTypeDeclaration.getSourceFile().getFile().getPath();
			String iFilePath = sourceFilePath.substring(sourceFilePath.indexOf("\\", sourceFilePath.indexOf(javaProject.getPath().makeRelative().toString())) + 1);
			
			IFile targetIFile = javaProject.getProject().getFile(iFilePath);
			
			extractSmells(smellType, detectedSmells, classTypeDeclaration.getSmells(), className, targetIFile);
			
			for(Method methodTypeDeclaration: classTypeDeclaration.getMethods())
				extractSmells(smellType, detectedSmells, methodTypeDeclaration.getSmells(), className, targetIFile);
		}
	}
	
	private void extractSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells, List<br.pucrio.opus.smells.collector.Smell> toolSmells,
							   String className, IFile targetIFile) throws Exception {
		for(br.pucrio.opus.smells.collector.Smell smell: toolSmells) {
			SmellType detectedSmellType = MAP_FROM_SMELLNAME_TO_SMELLTYPE.get(smell.getName());
			if(smellType != SmellType.ALL_SMELLS && smellType != detectedSmellType)
				continue;

			int startingLine = smell.getStartingLine();
			
			if(Utils.isClassSmell(detectedSmellType)) {
				Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
						Utils.createSmellObject(detectedSmellType, className, targetIFile, startingLine));
			} else {
				String methodName = (String) Utils.extractMethodNameAndCorrectLineFromFile(targetIFile, startingLine)[0];
				Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
						Utils.createSmellObject(detectedSmellType, className, methodName, targetIFile, startingLine));
			}
		}
	}

}
