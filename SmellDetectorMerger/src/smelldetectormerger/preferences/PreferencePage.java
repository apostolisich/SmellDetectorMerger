package smelldetectormerger.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private RadioGroupFieldEditor smellDetectorsRadio;
	private Composite composite;
	private Group smellDetectorsGroup;
	private BooleanFieldEditor checkStyle;
	private BooleanFieldEditor dude;
	private BooleanFieldEditor jDeodorant;
	private BooleanFieldEditor jSpirit;
	private BooleanFieldEditor pmd;
	private ScopedPreferenceStore scopedPreferences;
	
	//https://www.vogella.com/tutorials/EclipsePreferences/article.html
	
	public PreferencePage() {
		super(GRID);
		scopedPreferences = new ScopedPreferenceStore(InstanceScope.INSTANCE, "SmellDetectorMerger");
	}

	@Override
	public void init(IWorkbench arg0) {
        setPreferenceStore(scopedPreferences);
        setTitle("SmellDetectorMerger Options");
	}

	@Override
	protected void createFieldEditors() {
		smellDetectorsRadio = new RadioGroupFieldEditor(PreferenceConstants.USE_ALL_DETECTORS,
													    "Use all detectors for smell detection",
													    1,
													    new String[][] {
															{ "Yes", "yes" },
															{ "No", "no" }
													    },
													    getFieldEditorParent());
		
		addField(smellDetectorsRadio);
		
		composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		smellDetectorsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		smellDetectorsGroup.setLayout(new GridLayout(1, false));
		smellDetectorsGroup.setText("Selected Smell Detectors");
		
		checkStyle = new BooleanFieldEditor(PreferenceConstants.CHECKSTYLE_ENABLED, "CheckStyle", smellDetectorsGroup);
		dude = new BooleanFieldEditor(PreferenceConstants.DUDE_ENABLED, "DuDe", smellDetectorsGroup);
		jDeodorant = new BooleanFieldEditor(PreferenceConstants.JDEODORANT_ENABLED, "JDeodorant", smellDetectorsGroup);
		jSpirit = new BooleanFieldEditor(PreferenceConstants.JSPIRIT_ENABLED, "JSpIRIT", smellDetectorsGroup);
		pmd = new BooleanFieldEditor(PreferenceConstants.PMD_ENABLED, "PMD", smellDetectorsGroup);
		
		addField(checkStyle);
		addField(dude);
		addField(jDeodorant);
		addField(jSpirit);
		addField(pmd);
		
		if(scopedPreferences.getString(PreferenceConstants.USE_ALL_DETECTORS).equals("yes"))
			disableDetectorsSelectionGroup();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if(event.getProperty().equals(FieldEditor.VALUE)) {
			if(smellDetectorsRadio.getSelectionValue().equals("yes")) {
				disableDetectorsSelectionGroup();
			} else {
				enableDetectorsSelectionGroup();
			}
		}
	}
	
	private void disableDetectorsSelectionGroup() {
		checkStyle.setEnabled(false, smellDetectorsGroup);
		dude.setEnabled(false, smellDetectorsGroup);
		jDeodorant.setEnabled(false, smellDetectorsGroup);
		jSpirit.setEnabled(false, smellDetectorsGroup);
		pmd.setEnabled(false, smellDetectorsGroup);
	}
	
	private void enableDetectorsSelectionGroup() {
		checkStyle.setEnabled(true, smellDetectorsGroup);
		dude.setEnabled(true, smellDetectorsGroup);
		jDeodorant.setEnabled(true, smellDetectorsGroup);
		jSpirit.setEnabled(true, smellDetectorsGroup);
		pmd.setEnabled(true, smellDetectorsGroup);
	}

}
