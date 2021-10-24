package smelldetectormerger.views;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import smelldetectormerger.smells.Smell;
import smelldetectormerger.smells.SmellType;

public class SmellsView extends ViewPart {

	public static final String ID = "smelldetectormerger.views.SmellsView";
	
	private TableViewer tableViewer;
	private TableViewerColumn smellTypeColumn;
	private TableViewerColumn affectedElementColumn;
	private TableViewerColumn detectedToolCounterColumn;
		
	@Override
	public void createPartControl(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns();
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		setDoubleClickAction();
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
				return smell.getSmellTypeName();
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
		Set<Smell> fullSetOfSmells = new LinkedHashSet<Smell>();
		detectedSmells.forEach( (k,v) -> {
			fullSetOfSmells.addAll(v);
		});
		tableViewer.setInput(fullSetOfSmells);
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
				if(codeSmell.getSmellTypeName().equals("Duplicate Code")) {
					openFile(codeSmell.getTargetIFile(), codeSmell.getTargetStartLine(), codeSmell.getTargetEndLine());
				} else {
					openFile(codeSmell.getTargetIFile(), codeSmell.getTargetStartLine(), 0);
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
	 * @param startLine the line of the element that contains the smell
	 */
	private void openFile(IFile targetIFile, int startLine, int endLine) {
		try {
			IDocumentProvider provider = new TextFileDocumentProvider();
			provider.connect(targetIFile);
			
			IMarker marker = targetIFile.createMarker(IMarker.TEXT);
			marker.setAttribute(IMarker.CHAR_START, provider.getDocument(targetIFile).getLineOffset(startLine - 1));
			if(endLine > 0)
				marker.setAttribute(IMarker.CHAR_END, provider.getDocument(targetIFile).getLineOffset(endLine - 1));
				
			provider.disconnect(targetIFile);
			
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IDE.openEditor(page, marker);
			marker.delete();
		} catch(Exception e) {
			//TODO It would be better to show an error message here
		}
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
			int result = o1.getSmellTypeName().toLowerCase().compareTo(o2.getSmellTypeName().toLowerCase());
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
