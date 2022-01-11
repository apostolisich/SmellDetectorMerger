package smelldetectormerger.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import smelldetectormerger.Activator;
import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;

public class ToolAccuracyView {
	
	private Button twoToolsSetButton;
	private Button majorityVotingSetButton;
	private ComboViewer smellTypeDropdown;
	private TableViewer tableViewer;
	private SmellType[] smellsTypes‘ÔDetectAccuracyFor;
	private Map<SmellType, Set<String>> mapFromSmellTypeToSetOfSmellsForTwoPlusTools;
	private Map<SmellType, Set<String>> mapFromSmellTypeToSetOfSmellsForMajorityVoting;
	private Map<String, Map<SmellType, Set<String>>> mapFromToolToSmellTypesToSetOfSmells;
	
	public ToolAccuracyView(SmellType[] smellTypesToDetectAccuracyFor, Set<Smell> fullSetOfSmells, Set<Smell> twoPlusToolsFilteredSet, Set<Smell> majorityVotingFilteredSet) {
		this.smellsTypes‘ÔDetectAccuracyFor = smellTypesToDetectAccuracyFor;
		this.mapFromSmellTypeToSetOfSmellsForTwoPlusTools = extractMapFromSmellTypeToSetOfSmells(twoPlusToolsFilteredSet);
		this.mapFromSmellTypeToSetOfSmellsForMajorityVoting = extractMapFromSmellTypeToSetOfSmells(majorityVotingFilteredSet);
		fillMapFromToolToSmellTypesToSetOfSmells(fullSetOfSmells);
	}
	
	/**
	 * Constructs a new window with some preselected options and calculates/displays precision and recall
	 * for each tool basec on the preselected options.
	 */
	public void display() {
        Shell shell = initializeShell();
        
        setGoldStandardSetsGroup(shell);
        setSmellTypeSelectionGroup(shell);
        setTableViewer(shell);
        fillPrecisionRecallTableData(true);
        
        shell.pack();
        shell.open();
	}
	
	/**
	 * Parses the given set of smells and fills a map which has a {@code SmellType} as the key and
	 * a {@code Set} of smells that correspond to the given type.
	 * 
	 * @param setOfSmells a set of different kinds of smells.
	 * @return the filled map
	 */
	private Map<SmellType, Set<String>> extractMapFromSmellTypeToSetOfSmells(Set<Smell> setOfSmells) {
		Map<SmellType, Set<String>> mapFromSmellTypeToSetOfSmells = new HashMap<>();
		
		setOfSmells.forEach( smell -> {
			if(!mapFromSmellTypeToSetOfSmells.containsKey(smell.getSmellType()))
				mapFromSmellTypeToSetOfSmells.put(smell.getSmellType(), new HashSet<String>());
			
			mapFromSmellTypeToSetOfSmells.get(smell.getSmellType()).add(smell.getAffectedElementName());
		});
		
		return mapFromSmellTypeToSetOfSmells;
	}
	
	/**
	 * Parses the set of all the smells that were detected and fills a map from detector name
	 * to the smell type to the set of the smells that correspond to the previous two criteria.
	 * 
	 * @param fullSetOfSmells the set of all the smells that were detected
	 */
	private void fillMapFromToolToSmellTypesToSetOfSmells(Set<Smell> fullSetOfSmells) {
		mapFromToolToSmellTypesToSetOfSmells = new HashMap<>();
		
		fullSetOfSmells.forEach( smell -> {
			for(String detector: smell.getDetectorNames().split(",")) {
				detector = detector.trim();
				
				if(!mapFromToolToSmellTypesToSetOfSmells.containsKey(detector)) {
					Map<SmellType, Set<String>> tempMap = new HashMap<>();
					tempMap.put(smell.getSmellType(), new HashSet<String>());
					mapFromToolToSmellTypesToSetOfSmells.put(detector, tempMap);
				}
				
				if(!mapFromToolToSmellTypesToSetOfSmells.get(detector).containsKey(smell.getSmellType())) {
					mapFromToolToSmellTypesToSetOfSmells.get(detector).put(smell.getSmellType(), new HashSet<String>());
				}
				
				mapFromToolToSmellTypesToSetOfSmells.get(detector).get(smell.getSmellType()).add(smell.getAffectedElementName());
			}
		});
	}

	/**
	 * Initializes the shell inside which all the options and results for the tools accuracy
	 * are displayed.
	 * 
	 * @return the initialized shell
	 */
	private Shell initializeShell() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		
		Shell shell = new Shell(display);
        shell.setLayout(new RowLayout(SWT.VERTICAL));
        shell.setText("Tool Accuracy");
        ImageDescriptor windowImageDescriptor = ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault().getBundle(),
				new Path("icons/sample.png"),
				null));

        shell.setImage(windowImageDescriptor.createImage());
        
        return shell;
	}
	
	/**
	 * Creates a group which contains radio buttons in order to select which set will be used
	 * as the gold standard for precision and recall calculation.
	 * 
	 * @param shell the main window shell
	 */
	private void setGoldStandardSetsGroup(Shell shell) {
		Group goldStandardGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        goldStandardGroup.setLayout(new RowLayout(SWT.VERTICAL));
        goldStandardGroup.setText("Gold Standard Set");
        
        twoToolsSetButton = new Button(goldStandardGroup, SWT.RADIO);
        twoToolsSetButton.setText("Smells Detected by >=2 Tools");
        twoToolsSetButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(final SelectionEvent e){
                if(twoToolsSetButton.getSelection()){
                	fillPrecisionRecallTableData(true);
                }
            }
		});
        
        majorityVotingSetButton = new Button(goldStandardGroup, SWT.RADIO);
        majorityVotingSetButton.setText("Smells Detected by >50% of Tools");
        majorityVotingSetButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(final SelectionEvent e){
                if(majorityVotingSetButton.getSelection()){
                	fillPrecisionRecallTableData(false);
                }
            }
		});
	}
	
	/**
	 * Creates a dropdown which allows the user to select the smell type for which precision
	 * and recall will be calculated. 
     * 
	 * @param shell the main window shell
	 */
	private void setSmellTypeSelectionGroup(Shell shell) {
		Group smellDropdownGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        smellDropdownGroup.setLayout(new RowLayout(SWT.VERTICAL));
        smellDropdownGroup.setText("Which smell type to calculate accuracy for?");
        
        smellTypeDropdown = new ComboViewer(smellDropdownGroup, SWT.READ_ONLY);
        smellTypeDropdown.setContentProvider(ArrayContentProvider.getInstance());
        smellTypeDropdown.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((SmellType) element).getName();
            }
        });
        smellTypeDropdown.setInput(smellsTypes‘ÔDetectAccuracyFor);
        smellTypeDropdown.setSelection(new StructuredSelection(smellsTypes‘ÔDetectAccuracyFor[0]));
        smellTypeDropdown.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				fillPrecisionRecallTableData(twoToolsSetButton.getSelection());
			}
		});
	}
	
	private void setTableViewer(Shell shell) {
		tableViewer = new TableViewer(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns();
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
	}
	
	/**
	 * Creates the needed columns for the table and adds them to the table view.
	 */
	private void createColumns() {
		TableViewerColumn smellTypeColumn = createNewColumn(tableViewer, "Tool", 150);
		smellTypeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String result = (String) element;
				return result.split("@")[0];
			}
		});
		
		TableViewerColumn affectedElementColumn = createNewColumn(tableViewer, "Precision", 100);
		affectedElementColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String result = (String) element;
				return result.split("@")[1];
			}
		});
		
		TableViewerColumn detectedToolCounterColumn = createNewColumn(tableViewer, "Recall", 100);
		detectedToolCounterColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String result = (String) element;
				return result.split("@")[2];
			}
		});
	}
	
	/**
	 * Creates a new column with the given title and width and adds it to the table viewer.
	 * 
	 * @param tableViewer an object responsible for the display of the smells
	 * @param title the title for the column
	 * @param width the width of the column
	 * @return a new column attached to the table viewer
	 */
	private TableViewerColumn createNewColumn(TableViewer tableViewer, String title, int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		
		return viewerColumn;
	}
	
	/**
	 * Calculates precision and recall for each tool based on the selected smell type and the set
	 * which will be used as the gold standard, and then fills the table with all the data.
	 * 
	 * @param isTwoPlusToolsSetSelected indicates whether two plus tools set will be used as the gold
	 * 									standard or not.
	 */
	private void fillPrecisionRecallTableData(boolean isTwoPlusToolsSetSelected) {
		Map<SmellType, Set<String>> goldStandard = isTwoPlusToolsSetSelected ? mapFromSmellTypeToSetOfSmellsForTwoPlusTools
																			 : mapFromSmellTypeToSetOfSmellsForMajorityVoting;
		
		SmellType selectedSmellType = (SmellType) ((StructuredSelection) smellTypeDropdown.getSelection()).getFirstElement();
		
		List<String> results = new ArrayList<String>();
		mapFromToolToSmellTypesToSetOfSmells.forEach( (k, v) -> {
			Set<String> s1 = v.get(selectedSmellType);
			if(s1 == null)
				return;
			
			Set<String> s2 = goldStandard.get(selectedSmellType);
			if(s2 == null) {
				results.add(String.format("%s@Not Defined@Not Defined", k));
				return;
			}
			
			Set<String> intersectionSet = s1.stream().filter(s2::contains).collect(Collectors.toSet());
			
			double precision = (100 * intersectionSet.size()) / (double) s1.size();
			double recall = (100 * intersectionSet.size()) / (double) s2.size();
			
			results.add(String.format("%s@%.2f%%@%.2f%%", k, precision, recall));
		});
		
		tableViewer.getTable().removeAll();
		tableViewer.setInput(results);
	}
	
}