package smelldetectormerger.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import smelldetector.smells.GodClass;
import smelldetector.smells.LongMethod;
import smelldetector.smells.LongParameterList;
import smelldetector.smells.Smellable;
import smelldetector.smells.SmellType;

public abstract class Utils {
	
	/**
	 * Creates a new file that already exists inside the given bundle of the plugin.
	 * 
	 * @param bundle the bundle of the plugin
	 * @param filePath that path of the file to be created
	 * @return the requested file
	 */
	public static File createFile(Bundle bundle, String filePath) {
		URL fileUrl = FileLocator.find(bundle, new Path(filePath), null);
		
		File createdFile = null;
		try {
			fileUrl = FileLocator.toFileURL(fileUrl);
			createdFile = URIUtil.toFile(URIUtil.toURI(fileUrl));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return createdFile;
	}
	
	/**
	 * Runs the given command from the command list and returns the output as a {@code String}
	 * 
	 * @param commandList a list that contains the parts of the command to be processed
	 * @return the output of the command after it's run
	 */
	public static String runCommand(List<String> commandList) {
		ProcessBuilder pb = new ProcessBuilder(commandList);
		pb.redirectErrorStream(true);
		
		StringBuilder output = new StringBuilder();
		BufferedReader reader;
		try {
			Process p = pb.start();
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					
			String line;
			while((line = reader.readLine()) != null) {
				output.append(line);
				output.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return output.toString();
	}
	
	/**
	 * Parses the given XML {@code String} and creates a new XML {@code Document}.
	 * 
	 * @param xmlString an XML formatted as a {@code String}
	 * @return the created XML {@code Document}
	 */
	public static Document getXmlDocument(String xmlString) {
		DocumentBuilder db = createDocumentBuilder();
		Document doc = null;
		try {
			doc = db.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	/**
	 * Parses the given XML {@code File} and creates a new XML {@code Document}.
	 * 
	 * @param xmlFile an XML {@code File}
	 * @return the created XML {@code Document}
	 */
	public static Document getXmlDocument(File xmlFile) {
		DocumentBuilder db = createDocumentBuilder();
		Document doc = null;
		try {
			doc = db.parse(xmlFile);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	/**
	 * Gets a new instance of the {@code DocumentBuilderFactory} and then creates and returns
	 * a new {@code DocumentBuilder}.
	 * 
	 * @return a {@code DocumentBuilder}
	 */
	private static DocumentBuilder createDocumentBuilder() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return db;
	}
	
	public static Smellable createSmellObject(SmellType smellType, String... args) throws Exception {
		Smellable codeSmell = null;
		switch(smellType) {
			case GOD_CLASS:
				codeSmell = new GodClass(args[0]);
				break;
			case LONG_METHOD:
				codeSmell = new LongMethod(args[0], args[1]);
				break;
			case LONG_PARAMETER_LIST:
				codeSmell = new LongParameterList(args[0], args[1]);
			default:
				throw new Exception("Unexpected smell type");
		}
		return codeSmell;
	}
	
}
