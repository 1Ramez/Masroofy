package masroofy.controller;

import masroofy.data.DAOLayer;
import masroofy.view.UiTheme;

/**
 * Loads/saves the app theme mode and applies it to {@link UiTheme}.
 */
public class ThemeController {

    public enum Mode {
        DARK,
        LIGHT
    }

    private final DAOLayer daoLayer;

    public ThemeController() {
        this.daoLayer = new DAOLayer();
    }

    public Mode getSavedMode() {
        String v = daoLayer.getSetting("theme_mode");
        if (v == null)
            return Mode.DARK;
        if ("light".equalsIgnoreCase(v))
            return Mode.LIGHT;
        return Mode.DARK;
    }

    public void applySavedTheme() {
        apply(getSavedMode());
    }

    public void apply(Mode mode) {
        if (mode == Mode.LIGHT) {
            UiTheme.applyLight();
        } else {
            UiTheme.applyDark();
        }
    }

    public void saveAndApply(Mode mode) {
        daoLayer.saveSetting("theme_mode", mode == Mode.LIGHT ? "light" : "dark");
        apply(mode);
    }
}

