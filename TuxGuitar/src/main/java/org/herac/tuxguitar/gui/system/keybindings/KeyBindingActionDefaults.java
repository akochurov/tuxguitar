package org.herac.tuxguitar.gui.system.keybindings;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.herac.tuxguitar.gui.system.keybindings.xml.KeyBindingReader;
import org.herac.tuxguitar.gui.util.TGFileUtils;

public class KeyBindingActionDefaults {

	private static final String DEFAULT_SHORTCUT_FILE = "shortcuts-default.xml";

	public static List<KeyBindingAction> getDefaultKeyBindings() {
		List<KeyBindingAction> list = new ArrayList<KeyBindingAction>();
		try {
			InputStream stream = TGFileUtils.getResourceAsStream(DEFAULT_SHORTCUT_FILE);
			if (stream != null) {
				List<KeyBindingAction> defaults = KeyBindingReader.getKeyBindings(stream);
				if (defaults != null) {
					list.addAll(defaults);
				}
			}
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		return list;

	}
}
