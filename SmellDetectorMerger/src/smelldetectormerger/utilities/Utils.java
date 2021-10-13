package smelldetectormerger.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import smelldetector.smells.BrainClass;
import smelldetector.smells.BrainMethod;
import smelldetector.smells.DataClass;
import smelldetector.smells.DisperseCoupling;
import smelldetector.smells.DuplicateCode;
import smelldetector.smells.FeatureEnvy;
import smelldetector.smells.GodClass;
import smelldetector.smells.IntensiveCoupling;
import smelldetector.smells.LongMethod;
import smelldetector.smells.LongParameterList;
import smelldetector.smells.RefusedParentBequest;
import smelldetector.smells.ShotgunSurgery;
import smelldetector.smells.Smell;
import smelldetector.smells.SmellType;
import smelldetector.smells.TraditionBreaker;
import smelldetector.smells.TypeChecking;

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
		if(fileUrl == null)
			return null;
		
		try {
			fileUrl = FileLocator.toFileURL(fileUrl);
			createdFile = URIUtil.toFile(URIUtil.toURI(fileUrl));
		} catch (URISyntaxException | IOException e1) {
			//Ignore. The created file will be null and checks will be made in the tool's class
		}
		
		return createdFile;
	}
	
	/**
	 * Runs the given command from the command list and returns the output as a {@code String}
	 * 
	 * @param commandList a list that contains the parts of the command to be processed
	 * @param returnOutput a flag that indicates whether output should be returned or not
	 * @return the output of the command after it's run
	 */
	public static String runCommand(List<String> commandList, boolean returnOutput) {
		ProcessBuilder pb = new ProcessBuilder(commandList);
		pb.redirectErrorStream(true);
		
		StringBuilder output = new StringBuilder();
		BufferedReader reader;
		try {
			Process p = pb.start();
			
			if(!returnOutput) {
				p.destroy();
				return null;
			}
				
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					
			String line;
			while((line = reader.readLine()) != null) {
				//Skipping unnecessary line from CheckStyle tool
				if(line.startsWith("Checkstyle ends with"))
					continue;
				
				output.append(line);
				output.append("\n");
			}
			
			p.destroy();
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
	
	/**
	 * Creates and returns a new smell object based on the given arguments.
	 * 
	 * @param smellType the type of the smell to be created
	 * @param args the arguments for the smell constructor
	 * @return a new smell object
	 * @throws Exception
	 */
	public static Smell createSmellObject(SmellType smellType, Object... args) throws Exception {
		Smell codeSmell = null;
		switch(smellType) {
			case GOD_CLASS:
				codeSmell = new GodClass((String) args[0], (IFile) args[1], (Integer) args[2]);
				break;
			case LONG_METHOD:
				codeSmell = new LongMethod((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			case LONG_PARAMETER_LIST:
				codeSmell = new LongParameterList((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			case DUPLICATE_CODE:
				codeSmell = new DuplicateCode((Integer) args[0], (String) args[1], (IFile) args[2], (Integer) args[3], (Integer) args[4]);
				break;
			case BRAIN_CLASS:
				codeSmell = new BrainClass((String) args[0], (IFile) args[1], (Integer) args[2]);
				break;
			case BRAIN_METHOD:
				codeSmell = new BrainMethod((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			case DATA_CLASS:
				codeSmell = new DataClass((String) args[0], (IFile) args[1], (Integer) args[2]);
				break;
			case DISPERSE_COUPLING:
				codeSmell = new DisperseCoupling((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			case FEATURE_ENVY:
				codeSmell = new FeatureEnvy((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			case INTENSIVE_COUPLING:
				codeSmell = new IntensiveCoupling((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			case REFUSED_PARENT_BEQUEST:
				codeSmell = new RefusedParentBequest((String) args[0], (IFile) args[1], (Integer) args[2]);
				break;
			case SHOTGUN_SURGERY:
				codeSmell = new ShotgunSurgery((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			case TRADITION_BREAKER:
				codeSmell = new TraditionBreaker((String) args[0], (IFile) args[1], (Integer) args[2]);
				break;
			case TYPE_CHECKING:
				codeSmell = new TypeChecking((String) args[0], (String) args[1], (IFile) args[2], (Integer) args[3]);
				break;
			default:
				throw new Exception("Unexpected smell type: " + smellType.getName());
		}
		return codeSmell;
	}
	
	/**
	 * Adds the given new smell to the detected smells map.
	 * 
	 * @param smellType the {@code SmellType} of the new smell
	 * @param detectedSmells a {@code Map} that contains the detected smells
	 * @param newSmell the new {@code Smell} to be added
	 */
	public static void addSmell(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells, Smell newSmell) {
		if(!detectedSmells.containsKey(smellType)) {
			detectedSmells.put(smellType, new LinkedHashSet<Smell>());
		}
		
		detectedSmells.get(smellType).add(newSmell);
	}
	
	/**
	 * A convenience method which checks if the given smell type is linked to smells related to classes.
	 * 
	 * @param smellType the smell type to be checked
	 * @return true if the given smell is related to classes; false otherwise
	 */
	public static boolean isClassSmell(SmellType smellType) {
		if(smellType == SmellType.GOD_CLASS || smellType == SmellType.BRAIN_CLASS || smellType == SmellType.DATA_CLASS ||
				smellType == SmellType.REFUSED_PARENT_BEQUEST || smellType == SmellType.TRADITION_BREAKER)
			return true;
		
		return false;
	}
	
	/**
	 * Calculates the line number in which the given character offset is located
	 * (credits to JSpIRIT).
	 * 
	 * @param cUnit the compilation unit of the java element to be checked
	 * @param offSet the character offset to be checked
	 * @return the line number of the offset
	 */
	public static int getLineNumFromOffset(ICompilationUnit cUnit, int offSet) {
        try {
            String source = cUnit.getSource();
            IType type = cUnit.findPrimaryType();
            if(type != null) {
                String sourcetodeclaration = source.substring(0, offSet);
                int lines = 0;
                char[] chars = new char[sourcetodeclaration.length()];
                sourcetodeclaration.getChars(0, sourcetodeclaration.length(), chars, 0);
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] == '\n') {
                    	lines++;
                    }
                }
                
                return lines + 1;
            }
        } catch (JavaModelException jme) {
        }
        
        return 0;      
	}
}
