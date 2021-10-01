package smelldetectormerger.detectors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;

public class DuDeSmellDetector {
	
	public static void findSmells(Bundle bundle, IJavaProject javaProject) {
		URL jarUrl = FileLocator.find(bundle, new Path("dude/dude.jar"), null);
		URL configUrl = FileLocator.find(bundle, new Path("dude/selected-project.txt"), null);
		File jarFile = null;
		File configFile = null;
		try {
			jarUrl = FileLocator.toFileURL(jarUrl);
			jarFile = URIUtil.toFile(URIUtil.toURI(jarUrl));

			configUrl = FileLocator.toFileURL(configUrl);
			configFile = URIUtil.toFile(URIUtil.toURI(configUrl));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try(FileWriter selectedProjectFileWriter = new FileWriter(configFile)) {
			selectedProjectFileWriter.write(javaProject.getCorrespondingResource().getLocation().toString());
			//TODO this should go in a finally block
			selectedProjectFileWriter.close();
		} catch (IOException | JavaModelException e1) {
			e1.printStackTrace();
		}
		
		List<String> checkStyleCmdList = new ArrayList<>();
		checkStyleCmdList.add("cmd");
		checkStyleCmdList.add("/c");
		checkStyleCmdList.add("java");
		checkStyleCmdList.add("-cp");
		checkStyleCmdList.add(jarFile.getAbsolutePath());
		checkStyleCmdList.add("lrg.dude.batch.RunBatchMode");
		checkStyleCmdList.add(configFile.getAbsolutePath());

		ProcessBuilder pb = new ProcessBuilder(checkStyleCmdList);
		pb.redirectErrorStream(true);

//		MessageConsole console = new MessageConsole("My Console", null);
//		console.activate();
//		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
//		MessageConsoleStream stream = console.newMessageStream();

		try {
			Process p = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
//				stream.println(line);
			}
			reader.close();
			
			//TODO For the results, I should parse the generated files in System.getProperty("user.dir")
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
