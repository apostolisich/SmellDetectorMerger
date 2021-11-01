package smelldetectormerger.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferencePageInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ScopedPreferenceStore scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "SmellDetectorMerger");
        scopedPreferenceStore.setDefault(PreferenceConstants.USE_ALL_DETECTORS, "yes");
        scopedPreferenceStore.setDefault(PreferenceConstants.CHECKSTYLE_ENABLED, false);
        scopedPreferenceStore.setDefault(PreferenceConstants.DUDE_ENABLED, false);
        scopedPreferenceStore.setDefault(PreferenceConstants.JDEODORANT_ENABLED, false);
        scopedPreferenceStore.setDefault(PreferenceConstants.JSPIRIT_ENABLED, false);
        scopedPreferenceStore.setDefault(PreferenceConstants.PMD_ENABLED, false);
        scopedPreferenceStore.setDefault(PreferenceConstants.ORGANIC_ENABLED, false);
	}

}
