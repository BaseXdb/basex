package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Appearance preferences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogAppearancePrefs extends BaseXBack {
  /** Scaling factors of the user interface in percent. */
  private static final int[] SCALES = { 100, 125, 150, 175, 200, 250, 300 };
  /** Predefined font sizes. */
  private static final String[] SIZES =
    { "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
      "22", "24", "26", "28", "30", "33", "36", "40", "48", "64", "96" };
  /** Look and feels. */
  private static final String[] LOOKANDFEELS = {
    // https://www.formdev.com/flatlaf/
    "com.formdev.flatlaf.FlatLightLaf",
    "com.formdev.flatlaf.FlatDarkLaf",
    "com.formdev.flatlaf.FlatIntelliJLaf",
    "com.formdev.flatlaf.FlatDarculaLaf",
    "com.formdev.flatlaf.themes.FlatMacLightLaf",
    "com.formdev.flatlaf.themes.FlatMacDarkLaf",
    // http://www.jtattoo.net/
    "com.jtattoo.plaf.acryl.AcrylLookAndFeel",
    "com.jtattoo.plaf.aero.AeroLookAndFeel",
    "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel",
    "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel",
    "com.jtattoo.plaf.fast.FastLookAndFeel",
    "com.jtattoo.plaf.graphite.GraphiteLookAndFeel",
    "com.jtattoo.plaf.hifi.HiFiLookAndFeel",
    "com.jtattoo.plaf.luna.LunaLookAndFeel",
    "com.jtattoo.plaf.mcwin.McWinLookAndFeel",
    "com.jtattoo.plaf.mint.MintLookAndFeel",
    "com.jtattoo.plaf.noire.NoireLookAndFeel",
    "com.jtattoo.plaf.smart.SmartLookAndFeel",
    "com.jtattoo.plaf.texture.TextureLookAndFeel",
  };
  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

  /** GUI reference. */
  private final GUI gui;

  /** Look and feel. */
  private final BaseXCombo lookfeel;
  /** Scaling of the user interface. */
  private final BaseXCombo uiScale;
  /** Language combobox. */
  private final BaseXCombo lang;
  /** Translation credits. */
  private final BaseXLabel creds;

  /** Font name chooser. */
  private final BaseXList font;
  /** Monospace font name chooser. */
  private final BaseXList font2;
  /** Font size chooser. */
  private final BaseXList size;
  /** Only display monospace fonts. */
  private final BaseXCheckBox onlyMono;
  /** Antialiasing. */
  private final BaseXCombo antiAlias;
  /** Monospace fonts. */
  private final String[] monoFonts;

  /** Class names of the look and feels. */
  private final StringList classes = new StringList();

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogAppearancePrefs(final BaseXDialog dialog) {
    border(8).setLayout(new ColumnLayout(20));
    gui = dialog.gui();

    final GUIOptions gopts = gui.gopts;
    final StringList lafs = new StringList("(default)");
    classes.add("");
    int l = 0;
    final String laf = gopts.get(GUIOptions.LOOKANDFEEL);
    for(final String clzz : lookAndFeels()) {
      lafs.add(clzz.replaceAll("^.*\\.|LookAndFeel$", ""));
      classes.add(clzz);
      if(clzz.equals(laf)) l = classes.size() - 1;
    }
    lookfeel = new BaseXCombo(dialog, lafs.finish());
    lookfeel.setSelectedIndex(l);

    final StringList scales = new StringList("(default)");
    final int scale = gopts.get(GUIOptions.UISCALE);
    int s = 0;
    for(final int sc : SCALES) {
      scales.add(sc + "%");
      if(sc == scale) s = scales.size() - 1;
    }
    uiScale = new BaseXCombo(dialog, scales.finish());
    uiScale.setSelectedIndex(s);

    lang = new BaseXCombo(dialog, LANGS[0]);
    lang.setSelectedItem(gui.context.soptions.get(StaticOptions.LANG));
    creds = new BaseXLabel(" ");

    final String[] fonts = GUIConstants.fonts();
    font = new BaseXList(dialog, fonts);
    font.setWidth(250);
    font.setValue(gopts.get(GUIOptions.FONT));
    font2 = new BaseXList(dialog, fonts);
    font2.setWidth(250);
    font2.setValue(gopts.get(GUIOptions.MONOFONT));
    font2.setEnabled(false);
    size = new BaseXList(dialog, SIZES);
    size.setWidth(50);
    size.setValue(Integer.toString(gopts.get(GUIOptions.FONTSIZE)));
    antiAlias = new BaseXCombo(dialog, "System", "GASP", "On", "Off");
    antiAlias.setSelectedItem(gopts.get(GUIOptions.ANTIALIAS));
    onlyMono = new BaseXCheckBox(dialog, "Monospace", GUIOptions.LISTMONO, gopts);

    BaseXBack p = new BaseXBack(new RowLayout()), pp;
    p.add(new BaseXLabel(FONTS + COL, true, true));
    pp = new BaseXBack(new TableLayout(2, 3, 8, 8));
    pp.add(font);
    pp.add(font2);
    pp.add(size);
    final BaseXBack ppp = new BaseXBack(new ColumnLayout(8));
    ppp.add(new BaseXLabel("Antialiasing"));
    ppp.add(antiAlias);
    pp.add(ppp);
    pp.add(onlyMono);
    p.add(pp);
    add(p);

    p = new BaseXBack(new RowLayout());
    p.add(new BaseXLabel(AFTER_RESTART + COL, true, true));
    p.add(new BaseXLabel(JAVA_LF + COL));
    p.add(lookfeel);
    p.add(new BaseXLabel(UI_SCALE + COL).border(8, 0, 0, 0));
    p.add(uiScale);
    p.add(new BaseXLabel(LANGUAGE + COL).border(8, 0, 0, 0));
    p.add(lang);
    p.add(new BaseXLabel(TRANSLATION + COL).border(8, 0, 0, 0));
    p.add(creds);
    add(p);

    monoFonts = GUIConstants.monoFonts();
    action(onlyMono);
  }

  /**
   * Returns the translation credits for the specified language.
   * @param language language
   * @return credits
   */
  static String credits(final String language) {
    final int ll = LANGS[0].length;
    for(int l = 0; l < ll; l++) {
      if(LANGS[0][l].equals(language)) return LANGS[1][l];
    }
    return "";
  }

  /**
   * Reacts on user input.
   * @param source source
   * @return success flag
   */
  boolean action(final Object source) {
    final GUIOptions gopts = gui.gopts;
    gopts.set(GUIOptions.LOOKANDFEEL, classes.get(lookfeel.getSelectedIndex()));
    final int s = uiScale.getSelectedIndex();
    gopts.set(GUIOptions.UISCALE, s == 0 ? 0 : SCALES[s - 1]);
    gui.context.soptions.set(StaticOptions.LANG, lang.getSelectedItem());
    creds.setText(credits(lang.getSelectedItem()));

    boolean changed = false;
    if(source == antiAlias) {
      gopts.set(GUIOptions.ANTIALIAS, antiAlias.getSelectedItem());
      changed = true;
    } else if(source == onlyMono) {
      final boolean selected = onlyMono.isSelected();
      gopts.set(GUIOptions.LISTMONO, selected);
      if(selected) {
        final boolean ready = monoFonts != null;
        font2.setEnabled(ready);
        font2.setData(ready ? monoFonts : new String[] { PLEASE_WAIT_D });
      } else {
        font2.setEnabled(true);
        font2.setData(GUIConstants.fonts());
      }
      font2.setValue(gopts.get(GUIOptions.MONOFONT));
    } else if(source == font) {
      final String name = font.getValue();
      if(!name.isEmpty()) {
        gopts.set(GUIOptions.FONT, name);
        changed = true;
      }
    } else if(source == font2) {
      final String name = font2.getValue();
      if(!name.isEmpty()) {
        gopts.set(GUIOptions.MONOFONT, name);
        changed = true;
      }
    } else if(source == size) {
      final int num = size.getNum();
      if(num > 0) {
        gopts.set(GUIOptions.FONTSIZE, num);
        changed = true;
      }
    }
    if(changed) {
      font.setFont(gopts.get(GUIOptions.FONT));
      font2.setFont(gopts.get(GUIOptions.MONOFONT));
      gui.updateLayout();
    }
    return true;
  }

  /**
   * Returns the available look and feels.
   * @return class names
   */
  private static StringList lookAndFeels() {
    final StringList sl = new StringList();
    for(final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
      sl.add(laf.getClassName());
    }
    for(final String laf : LOOKANDFEELS) {
      if(Reflect.find(laf) != null) sl.add(laf);
    }
    return sl;
  }
}
