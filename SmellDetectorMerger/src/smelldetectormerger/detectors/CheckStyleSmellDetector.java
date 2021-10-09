package smelldetectormerger.detectors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import smelldetector.smells.Smell;
import smelldetector.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class CheckStyleSmellDetector extends SmellDetector {
	
	private Bundle bundle;
	private IJavaProject javaProject;
	
	public CheckStyleSmellDetector(Bundle bundle, IJavaProject javaProject) {
		this.bundle = bundle;
		this.javaProject = javaProject;
	}
	
	private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
			new HashSet<SmellType>(Arrays.asList(SmellType.DUPLICATE_CODE)));

	@Override
	public Set<SmellType> getSupportedSmellTypes() {
		return SUPPORTED_SMELL_TYPES;
	}

	@Override
	public String getDetectorName() {
		return "CheckStyle";
	}

	@Override
	public Set<Smell> findSmells(SmellType smellType) throws Exception {
		File checkStyleJarFile = Utils.createFile(bundle, "checkstyle-8.45/checkstyle-8.45-all.jar");
		File checkStyleConfigFile = Utils.createFile(bundle, "checkstyle-8.45/checkstyle-config.xml");
		
		String toolOutput = Utils.runCommand(buildToolCommand(checkStyleJarFile, checkStyleConfigFile));
		Document xmlDoc = Utils.getXmlDocument(toolOutput);
		
		return extractSmells(xmlDoc);
	}
	
	/**
	 * Builds a list that includes (in parts) the needed command to execute the tool via
	 * the command line and produce the smell detection results.
	 * 
	 * @param checkStyleJarFile the file of the tool
	 * @param checkStyleConfigFile the configuration file of the tool
	 * @return a list with the needed command
	 */
	private List<String> buildToolCommand(File checkStyleJarFile, File checkStyleConfigFile) {
		List<String> checkStyleCmdList = new ArrayList<>();
		
		try {
			checkStyleCmdList.add("cmd");
			checkStyleCmdList.add("/c");
			checkStyleCmdList.add("java");
			checkStyleCmdList.add("-jar");
			checkStyleCmdList.add(checkStyleJarFile.getAbsolutePath());
			checkStyleCmdList.add("-c");
			checkStyleCmdList.add(checkStyleConfigFile.getAbsolutePath());
			checkStyleCmdList.add("-f");
			checkStyleCmdList.add("xml");
			checkStyleCmdList.add(javaProject.getCorrespondingResource().getLocation().toString());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		
		return checkStyleCmdList;
	}
	
	/**
	 * A map that contains the code smells detected from the tool as the key, and their
	 * corresponding {@code SmellType} as the value.
	 */
	private static final Map<String, SmellType> MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE;
	static {
		Map<String, SmellType> tempMap = new HashMap<String, SmellType>(3);
		tempMap.put("FileLengthCheck", SmellType.GOD_CLASS);
		tempMap.put("MethodLengthCheck", SmellType.LONG_METHOD);
		tempMap.put("ParameterNumberCheck", SmellType.LONG_PARAMETER_LIST);
		MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE = Collections.unmodifiableMap(tempMap);
	}
	
	/**
	 * Extracts God Class, Long Method and Long Parameter List code smells and returns a set
	 * that contains all of them.
	 * 
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @return a set which contains all the detected smells of the given smell type
	 * @throws Exception
	 */
	private Set<Smell> extractSmells(Document xmlDoc) throws Exception {
		Set<Smell> detectedSmells = new HashSet<Smell>();
		
		NodeList fileNodes = xmlDoc.getDocumentElement().getElementsByTagName("file");
		for(int i = 0; i < fileNodes.getLength(); i++) {
			Node fileNode = fileNodes.item(i);
			
			String fileName = fileNode.getAttributes().getNamedItem("name").getNodeValue();
			String filePath = fileName.substring(fileName.indexOf("\\", fileName.indexOf(javaProject.getPath().makeRelative().toString())) + 1);
			IFile targetFile = javaProject.getProject().getFile(filePath);
			String className = fileName.substring(fileName.lastIndexOf('\\') + 1).replace(".java", "");
			
			NodeList errorNodes = fileNode.getChildNodes();
			for(int j = 0; j < errorNodes.getLength(); j++) {
				Node errorNode = errorNodes.item(j);
				//This is needed because there are unexpected children
				if(!errorNode.getNodeName().equals("error"))
					continue;
				
				String source = errorNode.getAttributes().getNamedItem("source").getNodeValue();
				String detectedSmell = source.substring(source.lastIndexOf('.') + 1);
				int startLine = Integer.parseInt(errorNode.getAttributes().getNamedItem("line").getNodeValue());
				
				SmellType smellType = MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE.get(detectedSmell);
				if(smellType == SmellType.GOD_CLASS) {
					detectedSmells.add(Utils.createSmellObject(SmellType.GOD_CLASS, className, targetFile, startLine));
				} else {
					String methodName = "";
					if(smellType == SmellType.LONG_PARAMETER_LIST) {
						methodName = extractMethodNameFromFile(targetFile, startLine);
					} else {
						String message = errorNode.getAttributes().getNamedItem("message").getNodeValue().replace("Method ", "");
						methodName = message.substring(0, message.indexOf(" "));
					}
					
					detectedSmells.add(Utils.createSmellObject(smellType, className, methodName, targetFile, startLine));
				}
			}
		}
		
		return detectedSmells;
	}
	
	/**
	 * Parses the given {@code IFile} until it reaches the given line and then extracts the
	 * method name in that line.
	 * 
	 * @param targetFile the {@code IFile} that will be parsed
	 * @param methodLine the line in which the method is declared
	 * @return the name of the method in the given line of the given file
	 * @throws Exception
	 */
	private String extractMethodNameFromFile(IFile targetFile, int methodLine) throws Exception {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(targetFile.getContents()))) {
			int lineCounter = 1;
			String line;
			while((line = reader.readLine()) != null) {
				if(lineCounter == methodLine)
					break;
				
				lineCounter++;
			}
			
			line = line.substring(0, line.indexOf('('));
			
			return line.substring(line.lastIndexOf(" ") + 1);
		} catch (IOException | CoreException e) {
			throw e;
		}
	}
	
}
