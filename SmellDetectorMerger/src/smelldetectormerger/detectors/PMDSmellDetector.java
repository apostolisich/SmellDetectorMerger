package smelldetectormerger.detectors;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import smelldetector.smells.Smell;
import smelldetector.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class PMDSmellDetector extends SmellDetector {
	
	private Bundle bundle;
	private IJavaProject javaProject;
	
	public PMDSmellDetector(Bundle bundle, IJavaProject javaProject) {
		this.bundle = bundle;
		this.javaProject = javaProject;
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
	
	@Override
	public Set<Smell> findSmells(SmellType smellType) throws Exception {
		File pmdBatFile = Utils.createFile(bundle, "pmd-bin-6.37.0/bin/pmd.bat");
		File pmdConfigFile = Utils.createFile(bundle, "pmd-bin-6.37.0/bin/pmd-config.xml");
		File pmdCacheFile = Utils.createFile(bundle, "pmd-bin-6.37.0/pmd-cache.txt");
		File cpdBatFile = Utils.createFile(bundle, "pmd-bin-6.37.0/bin/cpd.bat");
		
//		if(smellType.equals(SmellType.DUPLICATE_CODE)) {
//			String commandOutput = Utils.runCommand(buildDuplicateCodeToolCommand(cpdBatFile));
//			Document xmlDoc = Utils.getXmlDocument(commandOutput);
//			
//			return extractDuplicates(xmlDoc);
//		} else {
//			String commandOutput = Utils.runCommand(buildMainToolCommand(pmdBatFile, pmdConfigFile, pmdCacheFile));
//			Document xmlDoc = Utils.getXmlDocument(commandOutput);
//				
//			return extractSmells(xmlDoc, smellType);
//		}
		
		String commandOutput = Utils.runCommand(buildDuplicateCodeToolCommand(cpdBatFile));
		Document xmlDoc = Utils.getXmlDocument(commandOutput);
		
		Set<Smell> detectedDuplicates = extractDuplicates(xmlDoc);
		
		String commandOutput2 = Utils.runCommand(buildMainToolCommand(pmdBatFile, pmdConfigFile, pmdCacheFile));
		Document xmlDoc2 = Utils.getXmlDocument(commandOutput2);
			
		Set<Smell> detectedSmells = extractSmells(xmlDoc2);
		
		detectedSmells.addAll(detectedDuplicates);
		
		return detectedSmells;
	}
	
	/**
	 * Extracts Duplicate Code smells and returns a list that contains all of them.
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @return a list which contains all the duplicate code smells
	 * @throws Exception 
	 */
	private Set<Smell> extractDuplicates(Document xmlDoc) throws Exception {
		Set<Smell> detectedDuplicates = new LinkedHashSet<Smell>();
		
		NodeList duplicationNodes = xmlDoc.getDocumentElement().getElementsByTagName("duplication");
		for(int i = 0; i < duplicationNodes.getLength(); i++) {
			
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
				String filePath = path.substring(path.indexOf("\\", path.indexOf(javaProject.getPath().makeRelative().toString())));
				IFile targetFile = javaProject.getProject().getFile(filePath);
				
				detectedDuplicates.add(Utils.createSmellObject(SmellType.DUPLICATE_CODE, i, className, targetFile, startLine, endLine));
			}
		}
		
		return detectedDuplicates;
	}
	
	/**
	 * A map that contains the code smells detected from the tool as the key, and their
	 * corresponding {@code SmellType} as the value.
	 */
	private static final Map<String, SmellType> MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE;
	static {
		Map<String, SmellType> tempMap = new HashMap<String, SmellType>(3);
		tempMap.put("GodClass", SmellType.GOD_CLASS);
		tempMap.put("ExcessiveMethodLength", SmellType.LONG_METHOD);
		tempMap.put("ExcessiveParameterList", SmellType.LONG_PARAMETER_LIST);
		MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE = Collections.unmodifiableMap(tempMap);
	}

	/**
	 * Extracts God Class, Long Method and Long Parameter List code smells and returns a list
	 * that contains all of them.
	 * 
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @return a list which contains all the detected smells of the given smell type
	 * @throws Exception
	 */
	private Set<Smell> extractSmells(Document xmlDoc) throws Exception {
		Set<Smell> detectedSmells = new LinkedHashSet<Smell>();

		NodeList fileNodes = xmlDoc.getDocumentElement().getElementsByTagName("file");
		for(int fileIndex = 0; fileIndex < fileNodes.getLength(); fileIndex++) {
			Node fileNode = fileNodes.item(fileIndex);
			String fileName = fileNode.getAttributes().getNamedItem("name").getNodeValue();
			String filePath = fileName.substring(fileName.indexOf("\\", fileName.indexOf(javaProject.getPath().makeRelative().toString())));
			IFile targetFile = javaProject.getProject().getFile(filePath);
			
			NodeList violationNodes = fileNode.getChildNodes();
			for(int i = 0; i < violationNodes.getLength(); i++) {
				Node violationNode = violationNodes.item(i);
				//This is needed because there are unexpected children, e.g. #text
				if(!violationNode.getNodeName().equals("violation"))
					continue;
				
				int startLine = Integer.parseInt(violationNode.getAttributes().getNamedItem("beginline").getNodeValue());
				String detectedSmell = violationNode.getAttributes().getNamedItem("rule").getNodeValue();
					
				String className = violationNode.getAttributes().getNamedItem("class").getNodeValue();
				if(MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE.get(detectedSmell) == SmellType.GOD_CLASS) {
					detectedSmells.add(Utils.createSmellObject(SmellType.GOD_CLASS, className, targetFile, startLine));
				} else {
					SmellType smellType = MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE.get(detectedSmell);
					String methodName = violationNode.getAttributes().getNamedItem("method").getNodeValue();
					detectedSmells.add(Utils.createSmellObject(smellType, className, methodName, targetFile, startLine));
				}
			}
		}
		
		return detectedSmells;
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
