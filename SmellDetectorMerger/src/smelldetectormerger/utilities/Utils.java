package smelldetectormerger.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.Smell.Builder;
import smelldetectormerger.smells.SmellType;

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
	 * @throws InterruptedException 
	 */
	public static String runCommand(List<String> commandList, String directory, boolean returnOutput) throws InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(commandList);
		pb.redirectErrorStream(true);
		if(directory != null && !directory.isEmpty())
			pb.directory(new File(directory));
		
		StringBuilder output = new StringBuilder();
		try {
			Process p = pb.start();
			
			if(!returnOutput) {
				p.waitFor(2, TimeUnit.MINUTES);
				p.destroy();
				if(!p.waitFor(20, TimeUnit.SECONDS))
					p.destroyForcibly();
				
				return null;
			}
				
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					
			String line;
			while((line = reader.readLine()) != null) {
				//Skipping unnecessary line from CheckStyle tool
				if(line.startsWith("Checkstyle ends with"))
					continue;
				
				output.append(line);
				output.append("\n");
			}
			
			reader.close();
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
		Builder codeSmellBuilder;
		
		/** @formatter: off */
		if(smellType == SmellType.DUPLICATE_CODE) {
			codeSmellBuilder = new Smell.Builder(smellType)
											.setDuplicationGroupId((Integer) args[0])
											.setClassName((String) args[1])
											.setTargetIFile((IFile) args[2])
											.setStartLine((Integer) args[3])
											.setEndLine((Integer) args[4]);
		} else if(Utils.isClassSmell(smellType)) {
			codeSmellBuilder = new Smell.Builder(smellType)
											.setClassName((String) args[0])
											.setTargetIFile((IFile) args[1])
											.setStartLine((Integer) args[2]);
		} else {
			codeSmellBuilder = new Smell.Builder(smellType)
											.setClassName((String) args[0])
											.setMethodName((String) args[1])
											.setTargetIFile((IFile) args[2])
											.setStartLine((Integer) args[3]);
		}
		/** @formatter: on */
		
		return codeSmellBuilder.build();
	}
	
	/**
	 * Adds the given new smell to the detected smells map.
	 * 
	 * @param smellType the {@code SmellType} of the new smell
	 * @param detectedSmells a {@code Map} that contains the detected smells
	 * @param newSmell the new {@code Smell} to be added
	 */
	public static void addSmell(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells, String detectorName, Smell newSmell) {
		if(!detectedSmells.containsKey(smellType)) {
			detectedSmells.put(smellType, new LinkedHashSet<Smell>());
		}
		
		Set<Smell> detectedSmellsForSmellType = detectedSmells.get(smellType);
		if(detectedSmellsForSmellType.contains(newSmell)) {
			detectedSmellsForSmellType.stream().filter( smell -> smell.equals(newSmell)).findFirst().orElse(null).addDetectorName(detectorName);
		} else {
			newSmell.addDetectorName(detectorName);
			detectedSmellsForSmellType.add(newSmell);
		}
	}
	
	/**
	 * A convenience method which checks if the given smell type is linked to smells related to classes.
	 * 
	 * @param smellType the smell type to be checked
	 * @return true if the given smell is related to classes; false otherwise
	 */
	public static boolean isClassSmell(SmellType smellType) {
		if(smellType == SmellType.GOD_CLASS || smellType == SmellType.BRAIN_CLASS || smellType == SmellType.DATA_CLASS ||
		   smellType == SmellType.REFUSED_PARENT_BEQUEST || smellType == SmellType.TRADITION_BREAKER || smellType == SmellType.COMPLEX_CLASS ||
		   smellType == SmellType.CLASS_DATA_SHOULD_BE_PRIVATE || smellType == SmellType.LAZY_CLASS || smellType == SmellType.SPECULATIVE_GENERALITY ||
		   smellType == SmellType.SPAGHETTI_CODE)
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
	
	/**
	 * Parses the given {@code IFile} until it reaches the given line and then extracts the
	 * method name in that line.
	 * 
	 * @param targetFile the {@code IFile} that will be parsed
	 * @param methodLine the line in which the method is declared
	 * @return the name of the method and its correct line from the given file
	 * @throws Exception
	 */
	public static Object[] extractMethodNameAndCorrectLineFromFile(IFile targetFile, int methodLine) throws Exception {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(targetFile.getContents()))) {
			int lineCounter = 1;
			String line;
			while((line = reader.readLine()) != null) {
				//A "hack" needed because there can be cases in which a method has an annotation or java
				//doc, so the methodLine corresponds to these instead of the actual declaration line
				if((lineCounter == methodLine || lineCounter > methodLine) && line.indexOf('(') != -1)
					break;
				
				lineCounter++;
			}
			
			if(line == null)
				throw new Exception("End of file reached");
			
			line = line.substring(0, line.indexOf('('));
			
			return new Object[] { line.substring(line.lastIndexOf(" ") + 1), lineCounter };
		} catch (IOException | CoreException e) {
			throw e;
		}
	}
	
	/**
	 * Finds either the greatest duplication group id from the already detected duplicates, or
	 * returns 1 if no duplicates already exist.
	 * 
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 * @return the greatest duplication group id from existing duplicates or 1 if none already exist
	 */
	public static int getGreatestDuplicationGroupId(Map<SmellType, Set<Smell>> detectedSmells) {
		if(detectedSmells.containsKey(SmellType.DUPLICATE_CODE)) {
			return Collections.max(detectedSmells.get(SmellType.DUPLICATE_CODE), new Comparator<Smell>() {
			    @Override
			    public int compare(Smell first, Smell second) {
			        if (first.getDuplicationGroupId() > second.getDuplicationGroupId())
			            return 1;
			        else if (first.getDuplicationGroupId() < second.getDuplicationGroupId())
			            return -1;
			        return 0;
			    }
			}).getDuplicationGroupId() + 1;
		}
		
		return 1;
	}
	
	/**
	 * Returns the {@code SmellType} that corresponds the given smell name.
	 * 
	 * @param smellName the smellName for which to get the {@code SmellType}
	 * @return the correct {@code SmellType}
	 */
	public static SmellType getSmellTypeFromName(String smellName) {
		switch(smellName) {
			case "God Class":
				return SmellType.GOD_CLASS;
			case "Long Method":
				return SmellType.LONG_METHOD;
			case "Long Parameter List":
				return SmellType.LONG_PARAMETER_LIST;
			case "Feature Envy":
				return SmellType.FEATURE_ENVY;
			case "Duplicate Code":
				return SmellType.DUPLICATE_CODE;
			case "Brain Class":
				return SmellType.BRAIN_CLASS;
			case "Brain Method":
				return SmellType.BRAIN_METHOD;
			case "Data Class":
				return SmellType.DATA_CLASS;
			case "Dispersed Coupling":
				return SmellType.DISPERSED_COUPLING;
			case "Intensive Coupling":
				return SmellType.INTENSIVE_COUPLING;
			case "Refused Parent Bequest":
				return SmellType.REFUSED_PARENT_BEQUEST;
			case "Shotgun Surgery":
				return SmellType.SHOTGUN_SURGERY;
			case "Tradition Breaker":
				return SmellType.TRADITION_BREAKER;
			case "Type Checking":
				return SmellType.TYPE_CHECKING;
			case "Class Data Should Be Private":
				return SmellType.CLASS_DATA_SHOULD_BE_PRIVATE;
			case "Complex Class":
				return SmellType.COMPLEX_CLASS;
			case "Lazy Class":
				return SmellType.LAZY_CLASS;
			case "Message Chain":
				return SmellType.MESSAGE_CHAIN;
			case "Speculative Generality":
				return SmellType.SPECULATIVE_GENERALITY;
			case "Spaghetti Code":
				return SmellType.SPAGHETTI_CODE;
			case "Import CSV":
				return SmellType.IMPORT_CSV;
			default:
				return SmellType.ALL_SMELLS;
		}
	}
	
	/**
	 * Opens a new message dialog with the given message.
	 * 
	 * @param message the message to be displayed
	 */
	public static void openNewMessageDialog(String message) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MessageDialog.openInformation(
				window.getShell(),
				"SmellDetectorMerger",
				message);
	}
	
}
