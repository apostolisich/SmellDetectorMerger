package smelldetectormerger.views;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import smelldetectormerger.Activator;
import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;
import smelldetectormerger.utilities.Utils;

public class SmellsView extends ViewPart {

	public static final String ID = "smelldetectormerger.views.SmellsView";
	
	private TableViewer tableViewer;
	private TableViewerColumn smellTypeColumn;
	private TableViewerColumn affectedElementColumn;
	private TableViewerColumn detectedToolCounterColumn;
	private Action exportResultsAction;
	private Action twoPlusToolsFiltering;
	private Action majorityVotingFiltering;
	private Action undoFilteringAction;
	private Action toolsAccuracyAction;
	private Set<Smell> fullSetOfSmells;
	private Map<SmellType, Integer> mapFromSmellNameToMaxToolCount;
		
	@Override
	public void createPartControl(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns();
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		setDoubleClickAction();
		setTwoPlusToolsFilteringAction();
		setMajorityVotingFilteringAction();
		setUndoFilteringAction();
		setToolsAccuracyAction();
		setExportResultsAction();
		getSite().setSelectionProvider(tableViewer);
	}

	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}
	
	/**
	 * Creates the needed columns for the table and adds them to the table view.
	 */
	private void createColumns() {
		smellTypeColumn = createNewColumn("Smell Type", 170);
		smellTypeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Smell smell = (Smell) element;
				return smell.getSmellType().getName();
			}
		});
		smellTypeColumn.getColumn().addListener(SWT.Selection, COLUMN_SORT_LISTENER);
		
		affectedElementColumn = createNewColumn("Affected Element", 300);
		affectedElementColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Smell smell = (Smell) element;
				return smell.getAffectedElementName();
			}
		});
		affectedElementColumn.getColumn().addListener(SWT.Selection, COLUMN_SORT_LISTENER);
		
		detectedToolCounterColumn = createNewColumn("Detected by", 200);
		detectedToolCounterColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Smell smell = (Smell) element;
				return smell.getDetectorNames();
			}
		});
		detectedToolCounterColumn.getColumn().addListener(SWT.Selection, COLUMN_SORT_LISTENER);
	}
	
	/**
	 * Creates a new column with the given title and width and adds it to the table viewer.
	 * 
	 * @param tableViewer an object responsible for the display of the smells
	 * @param title the title for the column
	 * @param width the width of the column
	 * @return a new column attached to the table viewer
	 */
	private TableViewerColumn createNewColumn(String title, int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		
		return viewerColumn;
	}
	
	/**
	 * Passes the detected smells as an input to the table viewer in order to fill it with
	 * the available data.
	 * 
	 * @param detectedSmells the smells detected by the different tools
	 */
	public void addDetectedSmells(Map<SmellType, Set<Smell>> detectedSmells) {
		fullSetOfSmells = new LinkedHashSet<Smell>();
		detectedSmells.forEach( (k,v) -> {
			fullSetOfSmells.addAll(v);
		});
		tableViewer.setInput(fullSetOfSmells);
		
		if(fullSetOfSmells.size() > 0) {
			exportResultsAction.setEnabled(true);
			twoPlusToolsFiltering.setEnabled(true);
			majorityVotingFiltering.setEnabled(true);
			toolsAccuracyAction.setEnabled(true);
		}
	}
	
	public void setMaxToolCountMap(Map<SmellType, Integer> mapFromSmellNameToMaxToolCount) {
		this.mapFromSmellNameToMaxToolCount = mapFromSmellNameToMaxToolCount;
	}
	
	/**
	 * Creates a double click action in order to handle double clicks on the detected smells
	 * and then sets this action to the listener of the table that contains the smells.
	 */
	private void setDoubleClickAction() {
		Action doubleClickAction = new Action() {
			@Override
			public void run() {
				IStructuredSelection currentSelection = (IStructuredSelection) tableViewer.getStructuredSelection();
				Smell codeSmell = (Smell) currentSelection.getFirstElement();
				if(codeSmell.getSmellType().equals(SmellType.DUPLICATE_CODE)) {
					openFile(codeSmell.getTargetIFile(), codeSmell.getTargetStartLine(), codeSmell.getTargetEndLine(), null);
				} else {
					openFile(codeSmell.getTargetIFile(), codeSmell.getTargetStartLine(), 0, codeSmell.getSmellType());
				}
			}
		};
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				doubleClickAction.run();
			}
		});
	}
	
	/**
	 * Opens a new tab in the IDE to display the given file in which a smell was detected,
	 * at the given line.
	 * 
	 * @param targetFile the file to be opened (a java class)
	 * @param startLine the starting line of the element that contains the smell
	 * @param endLine the ending line of the element that contains the smell
	 * @param smellType the smell type that will be displayed in the given file
	 */
	private void openFile(IFile targetIFile, int startLine, int endLine, SmellType smellType) {
		try {
			IDocumentProvider provider = new TextFileDocumentProvider();
			provider.connect(targetIFile);
			
			if(endLine == 0 && !Utils.isClassSmell(smellType))
				startLine = (int) Utils.extractMethodNameAndCorrectLineFromFile(targetIFile, startLine)[1];
			
			IMarker marker = targetIFile.createMarker(IMarker.TEXT);
			marker.setAttribute(IMarker.CHAR_START, provider.getDocument(targetIFile).getLineOffset(startLine - 1));
			if(endLine > 0)
				marker.setAttribute(IMarker.CHAR_END, provider.getDocument(targetIFile).getLineOffset(endLine - 1));
				
			provider.disconnect(targetIFile);
			
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IDE.openEditor(page, marker);
			marker.delete();
		} catch(Exception e) {
			Utils.openNewMessageDialog("An error occured while trying to display the selected smell. Please try again...");
		}
	}
	
	/**
	 * Creates a filter action in order to allow the user to quickly view only the smells that were
	 * detected by at least 2 detectors.
	 */
	private void setTwoPlusToolsFilteringAction() {
		twoPlusToolsFiltering = new Action() {
			@Override
			public void run() {
				tableViewer.getTable().removeAll();
				tableViewer.setInput(getTwoPlusToolsFilteredSet());
				
				undoFilteringAction.setEnabled(true);
			}
		};
		
		ImageDescriptor actionImageDescriptor = ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault().getBundle(),
																	new Path("icons/filter.png"),
																	null));
		
		twoPlusToolsFiltering.setToolTipText("Smells Detected by >=2 Tools");
		twoPlusToolsFiltering.setImageDescriptor(actionImageDescriptor);
		twoPlusToolsFiltering.setEnabled(false);
		getViewSite().getActionBars().getToolBarManager().add(twoPlusToolsFiltering);
	}
	
	private Set<Smell> getTwoPlusToolsFilteredSet() {
		return fullSetOfSmells.stream().filter( smell -> smell.getDetectorNames().split(",").length >= 2).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	/**
	 * Creates a filter action in order to allow the user to quickly view only the smells that were
	 * detected by >50% of the tools that are able to detect a given smell.
	 */
	private void setMajorityVotingFilteringAction() {
		majorityVotingFiltering = new Action() {
			@Override
			public void run() {
				tableViewer.getTable().removeAll();
				tableViewer.setInput(getMajorityVotingFilteredSet());
				
				undoFilteringAction.setEnabled(true);
			}
		};
		
		ImageDescriptor actionImageDescriptor = ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault().getBundle(),
																	new Path("icons/filter.png"),
																	null));
		
		majorityVotingFiltering.setToolTipText("Smells Detected by >50% of Tools");
		majorityVotingFiltering.setImageDescriptor(actionImageDescriptor);
		majorityVotingFiltering.setEnabled(false);
		getViewSite().getActionBars().getToolBarManager().add(majorityVotingFiltering);
	}
	
	private Set<Smell> getMajorityVotingFilteredSet() {
		Set<Smell> tempSet = new LinkedHashSet<Smell>();
		
		fullSetOfSmells.forEach(smell -> {
			int maxToolCount = mapFromSmellNameToMaxToolCount.get(smell.getSmellType());
			int currentToolCount = smell.getDetectorNames().split(",").length;
			boolean isMajorityVoting = maxToolCount == 1 ?
										false :
										currentToolCount > maxToolCount / 2.0;
			
			if(isMajorityVoting)
				tempSet.add(smell);
		});
		
		return tempSet;
	}
	
	/**
	 * Creates an undo action which can be used in order to remove any applied filtering and
	 * add all the detected smells back to the view.
	 */
	private void setUndoFilteringAction() {
		undoFilteringAction = new Action() {
			@Override
			public void run() {
				tableViewer.getTable().removeAll();
				tableViewer.setInput(fullSetOfSmells);
				
				this.setEnabled(false);
			}
		};
		
		undoFilteringAction.setToolTipText("Undo Filtering");
		undoFilteringAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		undoFilteringAction.setEnabled(false);
		getViewSite().getActionBars().getToolBarManager().add(undoFilteringAction);
	}
	
	/**
	 * Creates an action which displays a new window to the user, in which precision and recall are calculated and displayed
	 * based on the selected smell type and gold standard set.
	 */
	private void setToolsAccuracyAction() {
		toolsAccuracyAction = new Action() {
			@Override
			public void run() {
				SmellType[] applicableSmellTypes = mapFromSmellNameToMaxToolCount.entrySet().stream().filter( e -> e.getValue() > 1).map(Map.Entry::getKey).toArray(SmellType[]::new);
				
				ToolAccuracyView app = new ToolAccuracyView(applicableSmellTypes, fullSetOfSmells, getTwoPlusToolsFilteredSet(), getMajorityVotingFilteredSet());
				app.display();
			}
		};
		
		toolsAccuracyAction.setToolTipText("Tools Accuracy");
		toolsAccuracyAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		toolsAccuracyAction.setEnabled(false);
		getViewSite().getActionBars().getToolBarManager().add(toolsAccuracyAction);
	}
	
	/**
	 * Creates an export action in order to save the results of the detection in a csv file.
	 */
	private void setExportResultsAction() {
		exportResultsAction = new Action() {
			@Override
			public void run() {
				String path = System.getProperty("user.dir");
				File exportFile = new File(path + "\\exported-results-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + ".csv");
				
				try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(exportFile))) {
					Set<Smell> detectedSmells = (Set<Smell>) tableViewer.getInput();
					for(Smell smell: detectedSmells) {
						fileWriter.write(smell.getSmellType().getName());
						fileWriter.write(',');
						fileWriter.write(smell.getAffectedElementName());
						fileWriter.write(',');
						fileWriter.write(smell.getDetectorNames());
						fileWriter.write('\n');
					}
				} catch (IOException e) {
					Utils.openNewMessageDialog("An error occured while exporting the results. Please try again...");
				}
				
				Utils.openNewMessageDialog("Results saved in " + exportFile.getAbsolutePath());
			}
		};
		
		exportResultsAction.setToolTipText("Export Results");
		exportResultsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		exportResultsAction.setEnabled(false);
		getViewSite().getActionBars().getToolBarManager().add(exportResultsAction);
	}
	
	private final Listener COLUMN_SORT_LISTENER = new Listener() {
		@Override
		public void handleEvent(Event e) {
			TableColumn column = (TableColumn) e.widget;
			
			@SuppressWarnings("unchecked")
			Set<Smell> detectedSmells = (Set<Smell>) tableViewer.getInput();
			
			Set<Smell> sortedSmells = null;
			if(column == smellTypeColumn.getColumn())
				sortedSmells = new TreeSet<Smell>(SMELL_NAME_COMPARATOR);
			else if(column == affectedElementColumn.getColumn())
				sortedSmells = new TreeSet<Smell>(AFFECTED_ELEMENT_COMPARATOR);
			else
				sortedSmells = new TreeSet<Smell>(DETECTOR_NAMES_COMPARATOR);
				
			sortedSmells.addAll(detectedSmells);
			tableViewer.getTable().setSortColumn(column);
			tableViewer.getTable().removeAll();
			tableViewer.setInput(sortedSmells);
		}
	};
	
	private final Comparator<Smell> SMELL_NAME_COMPARATOR = new Comparator<Smell> () {
		@Override
		public int compare(Smell o1, Smell o2) {
			int result = o1.getSmellType().getName().toLowerCase().compareTo(o2.getSmellType().getName().toLowerCase());
			//Hack to avoid deleting elements in case they are equal with those already in the set.
			return result == 0 ? -1 : result;
		}
	};
	
	private final Comparator<Smell> AFFECTED_ELEMENT_COMPARATOR = new Comparator<Smell> () {
		@Override
		public int compare(Smell o1, Smell o2) {
			int result = o1.getAffectedElementName().toLowerCase().compareTo(o2.getAffectedElementName().toLowerCase());
			//Hack to avoid deleting elements in case they are equal with those already in the set.
			return result == 0 ? -1 : result;
		}
	};
	
	private final Comparator<Smell> DETECTOR_NAMES_COMPARATOR = new Comparator<Smell> () {
		@Override
		public int compare(Smell o1, Smell o2) {
			int result = o1.getDetectorNames().toLowerCase().compareTo(o2.getDetectorNames().toLowerCase());
			//Hack to avoid deleting elements in case they are equal with those already in the set.
			return result == 0 ? -1 : result;
		}
	};
 
}
