package smelldetectormerger.detectors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.ui.console.ConsolePlugin;
//import org.eclipse.ui.console.IConsole;
//import org.eclipse.ui.console.MessageConsole;
//import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.Bundle;

public class PMDSmellDetector extends SmellDetector {
	
	// formatter: off
	private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
			new HashSet<SmellType>(Arrays.asList(SmellType.DUPLICATED_CODE,
												SmellType.GOD_CLASS,
												SmellType.LONG_METHOD,
												SmellType.LONG_PARAMETER_LIST)));
	// formatter: on
	
	public static void findSmells(Bundle bundle, IJavaProject javaProject) {
		URL pmdBatUrl = FileLocator.find(bundle, new Path("pmd-bin-6.37.0/bin/pmd.bat"), null);
		URL pmdConfigUrl = FileLocator.find(bundle, new Path("pmd-bin-6.37.0/bin/pmd-config.xml"), null);
		URL cpdBatUrl = FileLocator.find(bundle, new Path("pmd-bin-6.37.0/bin/cpd.bat"), null);

		File pmdBatFile = null;
		File pmdConfigFile = null;
		File cpdBatFile = null;
		try {
			pmdBatUrl = FileLocator.toFileURL(pmdBatUrl);
			pmdBatFile = URIUtil.toFile(URIUtil.toURI(pmdBatUrl));

			pmdConfigUrl = FileLocator.toFileURL(pmdConfigUrl);
			pmdConfigFile = URIUtil.toFile(URIUtil.toURI(pmdConfigUrl));
			
			cpdBatUrl = FileLocator.toFileURL(cpdBatUrl);
			cpdBatFile = URIUtil.toFile(URIUtil.toURI(cpdBatUrl));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		List<String> pmdCmdList = new ArrayList<String>();
		pmdCmdList.add("cmd");
		pmdCmdList.add("/c");
		pmdCmdList.add(pmdBatFile.getAbsolutePath());
		pmdCmdList.add("-d");
		try {
			pmdCmdList.add(javaProject.getCorrespondingResource().getLocation().toString());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		pmdCmdList.add("-f");
		pmdCmdList.add("xml");
		pmdCmdList.add("-R");
		pmdCmdList.add(pmdConfigFile.getAbsolutePath());
		
		ProcessBuilder pb = new ProcessBuilder(pmdCmdList);
		pb.redirectErrorStream(true);
		
//		MessageConsole console = new MessageConsole("My Console", null);
//		console.activate();
//		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
//		MessageConsoleStream stream = console.newMessageStream();
		
		try {
			Process p = pb.start();
			
			BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
			String line;
			while((line = reader.readLine()) != null) {
				System.out.println(line);
//				stream.println(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<String> cpdCmdList = new ArrayList<String>();
		cpdCmdList.add("cmd");
		cpdCmdList.add("/c");
		cpdCmdList.add(cpdBatFile.getAbsolutePath());
		cpdCmdList.add("--minimum-tokens");
		cpdCmdList.add("100");
		cpdCmdList.add("--files");
		try {
			cpdCmdList.add(javaProject.getCorrespondingResource().getLocation().toString());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		cpdCmdList.add("--format");
		cpdCmdList.add("xml");
		
		pb = new ProcessBuilder(cpdCmdList);
		pb.redirectErrorStream(true);
		
		try {
			Process p = pb.start();
			
			BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
			String line;
			while((line = reader.readLine()) != null) {
				System.out.println(line);
//				stream.println(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void findSmells(SmellType smellType) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SmellType> getSupportedSmellTypes() {
		return SUPPORTED_SMELL_TYPES;
	}

	@Override
	public String getDetectorName() {
		return "PMD";
	}
	
}
