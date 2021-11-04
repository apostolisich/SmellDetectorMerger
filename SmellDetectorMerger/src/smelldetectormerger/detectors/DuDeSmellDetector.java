package smelldetectormerger.detectors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class DuDeSmellDetector extends SmellDetector {
	
	private Bundle bundle;
	private IJavaProject javaProject;
	
	public DuDeSmellDetector(Bundle bundle, IJavaProject javaProject) {
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
		return "DuDe";
	}

	@Override
	public void findSmells(SmellType smellType,  Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		File dudeJarFile = Utils.createFile(bundle, "dude/dude.jar");
		File dudeConfigFile = Utils.createFile(bundle, "dude/selected-project.txt");
		
		writeSelectedProjectPathToConfigFile(dudeConfigFile);
		
		String dudeDirectory = dudeJarFile.getAbsolutePath().toString();
		dudeDirectory = dudeDirectory.substring(0, dudeDirectory.lastIndexOf("\\") + 1);
		
		Utils.runCommand(buildToolCommand(dudeJarFile, dudeConfigFile), dudeDirectory, false);
		
		for(int i = 0; i < 3; i++) {
			File resultsFile = new File(String.format(dudeDirectory + "Result%d.xml", i));
			if(!resultsFile.exists())
				continue;
			
			Document xmlDoc = Utils.getXmlDocument(resultsFile);
			extractDuplicates(xmlDoc, detectedSmells);
			resultsFile.delete();
		}
	}
	
	/**
	 * Writes the project's absolute path to the config file of the tool.
	 * 
	 * @param dudeConfigFile the configuration file of the tool
	 * @throws Exception 
	 */
	private void writeSelectedProjectPathToConfigFile(File dudeConfigFile) throws Exception {
		try(FileWriter selectedProjectFileWriter = new FileWriter(dudeConfigFile)) {
			//This is needed so that the tool knows where the project is located in order to be checked
			selectedProjectFileWriter.write(javaProject.getCorrespondingResource().getLocation().toString());
		} catch (IOException | JavaModelException e) {
			throw e;
		}
	}
	
	/**
	 * Builds a list that includes (in parts) the needed command to execute the tool via
	 * the command line and produce the smell detection results.
	 * 
	 * @param dudeJarFile the file of the tool
	 * @param dudeConfigFile the configuration file of the tool
	 * @return a list with the needed command
	 * @throws JavaModelException 
	 */
	private List<String> buildToolCommand(File dudeJarFile, File dudeConfigFile) throws JavaModelException {
		List<String> dudeCmdList = new ArrayList<>();
		
		dudeCmdList.add("java");
		dudeCmdList.add("-cp");
		dudeCmdList.add(dudeJarFile.getAbsolutePath());
		dudeCmdList.add("lrg.dude.batch.RunBatchMode");
		dudeCmdList.add(dudeConfigFile.getAbsolutePath());
		
		return dudeCmdList;
	}

	/**
	 * Extracts Duplicate Code smells and returns a set that contains all of them.
	 * 
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 * @throws Exception 
	 */
	private void extractDuplicates(Document xmlDoc, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		int duplicationGroupId = Utils.getGreatestDuplicationGroupId(detectedSmells);
		
		NodeList dupChains = xmlDoc.getDocumentElement().getElementsByTagName("DupChain");
		dupChainLoop: for(int i = 0; i < dupChains.getLength(); i++) {
			NodeList codeSnippets = dupChains.item(i).getChildNodes();
			for(int j = 0; j < codeSnippets.getLength(); j++) {
				Node codeSnippet = codeSnippets.item(j);
				
				if(!codeSnippet.getNodeName().equals("CodeSnippet"))
					continue;
				
				String fileName = codeSnippet.getAttributes().getNamedItem("FileName").getNodeValue();
				if(!fileName.endsWith(".java"))
					continue dupChainLoop;
				
				String className = fileName.substring(fileName.lastIndexOf("\\") + 1);
				IFile targetIFile = javaProject.getProject().getFile(fileName);
				int startLine = Integer.parseInt(codeSnippet.getAttributes().getNamedItem("From").getNodeValue());
				int endLine = Integer.parseInt(codeSnippet.getAttributes().getNamedItem("To").getNodeValue());
				
				Utils.addSmell(SmellType.DUPLICATE_CODE, detectedSmells, getDetectorName(),
						Utils.createSmellObject(SmellType.DUPLICATE_CODE, duplicationGroupId, className, targetIFile, startLine, endLine));
			}
			
			duplicationGroupId++;
		}
	}

}
