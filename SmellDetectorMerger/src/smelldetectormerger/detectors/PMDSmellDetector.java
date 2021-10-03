package smelldetectormerger.detectors;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import smelldetector.smells.DuplicateCode;
import smelldetector.smells.SmellType;
import smelldetector.smells.Smellable;
import smelldetectormerger.utilities.Duplication;
import smelldetectormerger.utilities.Utils;

public class PMDSmellDetector extends SmellDetector {
	
	private Bundle bundle;
	private IJavaProject javaProject;
	
	public PMDSmellDetector(Bundle bundle, IJavaProject javaProject) {
		this.bundle = bundle;
		this.javaProject = javaProject;
	}
	
	/**
	 * A map that contains the code smells detected from the tool as the key, and their
	 * corresponding {@code SmellType} as the value.
	 */
	private static final Map<String, SmellType> MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE;
	static {
		Map<String, SmellType> tempMap = new HashMap<String, SmellType>(4);
		tempMap.put("GodClass", SmellType.GOD_CLASS);
		tempMap.put("ExcessiveMethodLength", SmellType.LONG_METHOD);
		tempMap.put("ExcessiveParameterList", SmellType.LONG_PARAMETER_LIST);
		MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE = Collections.unmodifiableMap(tempMap);
	}
	
	@Override
	public Set<Smellable> findSmells(SmellType smellType) throws Exception {
		File pmdBatFile = Utils.createFile(bundle, "pmd-bin-6.37.0/bin/pmd.bat");
		File pmdConfigFile = Utils.createFile(bundle, "pmd-bin-6.37.0/bin/pmd-config.xml");
		File pmdCacheFile = Utils.createFile(bundle, "pmd-bin-6.37.0/pmd-cache.txt");
		File cpdBatFile = Utils.createFile(bundle, "pmd-bin-6.37.0/bin/cpd.bat");
		
		if(smellType.equals(SmellType.DUPLICATE_CODE)) {
			String commandOutput = Utils.runCommand(buildDuplicateCodeToolCommand(cpdBatFile));
			Document xmlDoc = Utils.getXmlDocument(commandOutput);
			
			return extractDuplicates(xmlDoc);
		} else {
			String commandOutput = Utils.runCommand(buildMainToolCommand(pmdBatFile, pmdConfigFile, pmdCacheFile));
			Document xmlDoc = Utils.getXmlDocument(commandOutput);
				
			return extractSmells(xmlDoc, smellType);
		}
	}
	
	/**
	 * Extracts Duplicate Code smells and returns a list that contains all of them.
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @return a list which contains all the duplicate code smells
	 */
	private Set<Smellable> extractDuplicates(Document xmlDoc) {
		Set<Smellable> detectedDuplicates = new HashSet<Smellable>();
		
		NodeList duplicationNodes = xmlDoc.getDocumentElement().getElementsByTagName("duplication");
		for(int i = 0; i < duplicationNodes.getLength(); i++) {
			List<Duplication> duplications = new ArrayList<Duplication>();
			
			NodeList fileEntries = duplicationNodes.item(i).getChildNodes();
			for(int j = 0; j < fileEntries.getLength(); j++) {
				Node fileEntry = fileEntries.item(j);
				//Each duplication has a <codefragment> element which only contains the duplicate code
				//and doesn't have any attributes. So this is recognized this way and is ignored
				if(!fileEntry.hasAttributes())
					continue;
				
				NamedNodeMap fileEntryAttributes = fileEntry.getAttributes();
				int startLine = Integer.parseInt(fileEntryAttributes.getNamedItem("line").getNodeValue());
				int endLine = Integer.parseInt(fileEntryAttributes.getNamedItem("endline").getNodeValue());
				String path = fileEntryAttributes.getNamedItem("path").getNodeValue();
				String className = path.substring(path.lastIndexOf("\\") + 1);
				
				duplications.add(new Duplication(className, startLine, endLine));
			}
			detectedDuplicates.add(new DuplicateCode(duplications));
		}
		
		return detectedDuplicates;
	}

	/**
	 * Extracts God Class, Long Method and Long Parameter List code smells and returns a list
	 * that contains all of them.
	 * 
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @param smellType the type of the requested smell to be checked
	 * @return a list which contains all the detected smells of the given smell type
	 * @throws Exception
	 */
	private Set<Smellable> extractSmells(Document xmlDoc, SmellType smellType) throws Exception {
		Set<Smellable> detectedSmells = new HashSet<Smellable>();
		
		NodeList fileNodes = xmlDoc.getDocumentElement().getElementsByTagName("file");
		for(int i = 0; i < fileNodes.getLength(); i++) {
			NodeList violationNodes = fileNodes.item(i).getChildNodes();
			for(int j = 0; j < violationNodes.getLength(); j++) {
				Node violationNode = violationNodes.item(j);
				String detectedSmell = violationNode.getAttributes().getNamedItem("rule").getNodeValue();
				
				if(MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE.get(detectedSmell) != smellType)
					continue;
				
				String className = violationNode.getAttributes().getNamedItem("class").getNodeValue();
				if(smellType.equals(SmellType.GOD_CLASS)) {
					detectedSmells.add(Utils.createSmellObject(smellType, className));
				} else {
					String methodName = violationNode.getAttributes().getNamedItem("method").getNodeValue();
					detectedSmells.add(Utils.createSmellObject(smellType, className, methodName));
				}
			}
		}
		
		return detectedSmells;
	}

	private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
			new HashSet<SmellType>(Arrays.asList(SmellType.DUPLICATE_CODE,
												SmellType.GOD_CLASS,
												SmellType.LONG_METHOD,
												SmellType.LONG_PARAMETER_LIST)));
	
	@Override
	public Set<SmellType> getSupportedSmellTypes() {
		return SUPPORTED_SMELL_TYPES;
	}

	@Override
	public String getDetectorName() {
		return "PMD";
	}
	
	/**
	 * Builds a list that includes (in parts) the needed command to trigger the main tool via
	 * the command line and produce the smell detection results.
	 * 
	 * @param mainToolBatFile the file of the main tool
	 * @param configFile the configuration file of the main tool
	 * @param javaProject the selected java project for smell detection
	 * @return a list with the needed command
	 */
	private List<String> buildMainToolCommand(File mainToolBatFile, File configFile, File cacheFile) {
		List<String> mainToolCmdList = new ArrayList<String>();
		try {
			mainToolCmdList.add("cmd");
			mainToolCmdList.add("/c");
			mainToolCmdList.add(mainToolBatFile.getAbsolutePath());
			mainToolCmdList.add("-d");
			mainToolCmdList.add(javaProject.getCorrespondingResource().getLocation().toString());
			mainToolCmdList.add("-cache");
			mainToolCmdList.add(cacheFile.getAbsolutePath());
			mainToolCmdList.add("-f");
			mainToolCmdList.add("xml");
			mainToolCmdList.add("-R");
			mainToolCmdList.add(configFile.getAbsolutePath());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		
		return mainToolCmdList;
	}
	
	/**
	 * Builds a list that includes (in parts) the needed command to trigger the duplicate
	 * code tool via the command line and produce the detection results.
	 * 
	 * @param duplicateCodeToolBatFile the file of the duplicate code tool
	 * @return a list with the needed command
	 */
	private List<String> buildDuplicateCodeToolCommand(File duplicateCodeToolBatFile) {
		List<String> duplicateCodeToolCmdList = new ArrayList<String>();
		try {
			duplicateCodeToolCmdList.add("cmd");
			duplicateCodeToolCmdList.add("/c");
			duplicateCodeToolCmdList.add(duplicateCodeToolBatFile.getAbsolutePath());
			duplicateCodeToolCmdList.add("--minimum-tokens");
			duplicateCodeToolCmdList.add("100");
			duplicateCodeToolCmdList.add("--files");
			duplicateCodeToolCmdList.add(javaProject.getCorrespondingResource().getLocation().toString());
			duplicateCodeToolCmdList.add("--format");
			duplicateCodeToolCmdList.add("xml");
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return duplicateCodeToolCmdList;
	}
}
