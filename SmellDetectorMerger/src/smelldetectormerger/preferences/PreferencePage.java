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
	private BooleanFieldEditor organic;
	private ScopedPreferenceStore scopedPreferences;
	
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
		smellDetectorsGroup.setText("Enabled Smell Detectors");
		
		checkStyle = new BooleanFieldEditor(PreferenceConstants.CHECKSTYLE_ENABLED, "CheckStyle", smellDetectorsGroup);
		dude = new BooleanFieldEditor(PreferenceConstants.DUDE_ENABLED, "DuDe", smellDetectorsGroup);
		jDeodorant = new BooleanFieldEditor(PreferenceConstants.JDEODORANT_ENABLED, "JDeodorant", smellDetectorsGroup);
		jSpirit = new BooleanFieldEditor(PreferenceConstants.JSPIRIT_ENABLED, "JSpIRIT", smellDetectorsGroup);
		pmd = new BooleanFieldEditor(PreferenceConstants.PMD_ENABLED, "PMD", smellDetectorsGroup);
		organic = new BooleanFieldEditor(PreferenceConstants.ORGANIC_ENABLED, "Organic", smellDetectorsGroup);
		
		addField(checkStyle);
		addField(dude);
		addField(jDeodorant);
		addField(jSpirit);
		addField(pmd);
		addField(organic);
		
		if(!scopedPreferences.contains(PreferenceConstants.USE_ALL_DETECTORS)
				|| (scopedPreferences.contains(PreferenceConstants.USE_ALL_DETECTORS) && scopedPreferences.getString(PreferenceConstants.USE_ALL_DETECTORS).equals(("yes"))))
			toggleDetectorsSelectionAvailability(true);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if(event.getProperty().equals(FieldEditor.VALUE)) {
			boolean useAllDetectors = smellDetectorsRadio.getSelectionValue().equals("yes");
			toggleDetectorsSelectionAvailability(useAllDetectors);
		}
	}
	
	private void toggleDetectorsSelectionAvailability(boolean useAllDetectors) {
		checkStyle.setEnabled(useAllDetectors ? false : true, smellDetectorsGroup);
		dude.setEnabled(useAllDetectors ? false : true, smellDetectorsGroup);
		jDeodorant.setEnabled(useAllDetectors ? false : true, smellDetectorsGroup);
		jSpirit.setEnabled(useAllDetectors ? false : true, smellDetectorsGroup);
		pmd.setEnabled(useAllDetectors ? false : true, smellDetectorsGroup);
		organic.setEnabled(useAllDetectors ? false : true, smellDetectorsGroup);
	}

}
