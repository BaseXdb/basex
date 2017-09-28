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
 * Visualization preferences.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class DialogVisualPrefs extends BaseXBack {
  /** GUI reference. */
  private final GUI gui;
  /** Show names checkbox. */
  private final BaseXCheckBox showNames;

  /** Slim rectangles to text length. */
  private final BaseXCheckBox treeSlims;
  /** Show attributes. */
  private final BaseXCheckBox treeAtts;

  /** Algorithm combobox. */
  private final BaseXCombo mapAlgo;
  /** Layout slider. */
  private final BaseXSlider mapWeight;
  /** Show attributes. */
  private final BaseXCheckBox mapAtts;
  /** Select layout algorithm. */
  private final BaseXCombo mapOffsets;
  /** Simple file dialog checkbox. */
  private final BaseXCombo lookfeel;
  /** Scale UI components. */
  private final BaseXCheckBox scale;
  /** Serialization parameters. */
  private final BaseXSerial serial;

  /** Look and feels. */
  private final StringList classes = new StringList();

  /**
   * Default constructor.
   * @param d dialog reference
   */
  DialogVisualPrefs(final BaseXDialog d) {
    border(8).setLayout(new TableLayout(2, 1, 0, 8));
    gui = d.gui;

    final GUIOptions gopts = d.gui.gopts;
    showNames = new BaseXCheckBox(SHOW_NAME_ATTS, GUIOptions.SHOWNAME, gopts, d);
    treeSlims = new BaseXCheckBox(ADJUST_NODES, GUIOptions.TREESLIMS, gopts, d);
    treeAtts = new BaseXCheckBox(SHOW_ATTS, GUIOptions.TREEATTS, gopts, d);
    mapAlgo = new BaseXCombo(GUIOptions.MAPALGO, gopts, MAP_LAYOUTS, d);
    mapOffsets = new BaseXCombo(GUIOptions.MAPOFFSETS, gopts, MAP_CHOICES, d);
    mapWeight = new BaseXSlider(0, 100, GUIOptions.MAPWEIGHT, gopts, d);
    mapAtts = new BaseXCheckBox(SHOW_ATTS, GUIOptions.MAPATTS, gopts, d);
    mapAlgo.setSize((int) (GUIConstants.scale * 200), (int) (GUIConstants.scale * 100));
    BaseXLayout.setWidth(mapWeight, 150);

    final StringList lafs = new StringList("(default)");
    classes.add("");
    int i = 0, c = 0;
    final String laf = gopts.get(GUIOptions.LOOKANDFEEL);
    for(final String clzz : lf()) {
      lafs.add(clzz.replaceAll("^.*\\.|LookAndFeel$", ""));
      classes.add(clzz);
      c++;
      if(clzz.equals(laf)) i = c;
    }
    lookfeel = new BaseXCombo(lafs.finish(), d);
    lookfeel.setSelectedIndex(i);

    scale = new BaseXCheckBox(SCALE_GUI, GUIOptions.SCALE, gopts, d);
    serial = new BaseXSerial(d, gui.context.options.get(MainOptions.SERIALIZER));

    BaseXBack pp = new BaseXBack().layout(new TableLayout(3, 1, 0, 8)), ppp;
    ppp = new BaseXBack(new TableLayout(3, 1));
    ppp.add(new BaseXLabel(JAVA_LF + COL, true, true));
    ppp.add(lookfeel);
    ppp.add(scale);
    pp.add(ppp);

    ppp = new BaseXBack(new TableLayout(2, 1));
    ppp.add(new BaseXLabel(GENERAL + COL, true, true));
    ppp.add(showNames);
    pp.add(ppp);

    ppp = new BaseXBack(new TableLayout(3, 1));
    ppp.add(new BaseXLabel(TREE + COL, true, true));
    ppp.add(treeSlims);
    ppp.add(treeAtts);
    pp.add(ppp);

    final BaseXBack p = new BaseXBack(new TableLayout(1, 2, 40, 0));
    p.add(pp);

    pp = new BaseXBack(new TableLayout(3, 1));
    pp.add(new BaseXLabel(MAP + COL, true, true));

    ppp = new BaseXBack(new TableLayout(2, 2, 8, 8));
    ppp.add(new BaseXLabel(ALGORITHM + COL));
    ppp.add(mapAlgo);
    ppp.add(new BaseXLabel(OFFSETS + COL));
    ppp.add(mapOffsets);
    pp.add(ppp);

    ppp = new BaseXBack(new TableLayout(3, 1, 0, 8));
    ppp.add(new BaseXLabel(RATIO + COLS));
    ppp.add(mapWeight);
    ppp.add(mapAtts);
    pp.add(ppp);
    p.add(pp);
    add(p);

    add(serial);
  }

  /**
   * Reacts on user input.
   * @return success flag
   */
  boolean action() {
    treeSlims.assign();
    treeAtts.assign();
    mapAtts.assign();
    showNames.assign();
    mapWeight.assign();
    mapAlgo.assign();
    mapOffsets.assign();
    scale.assign();
    gui.gopts.set(GUIOptions.LOOKANDFEEL, classes.get(lookfeel.getSelectedIndex()));
    return true;
  }

  /** Look and feels. */
  private static final String[] LOOKANDFEELS = {
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

  /**
   * Returns available look and feels.
   * @return string list
   */
  private static StringList lf() {
    final StringList sl = new StringList();
    for(final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
      sl.add(laf.getClassName());
    }
    for(final String laf : LOOKANDFEELS) {
      if(Reflect.find(laf) != null) sl.add(laf);
    }
    return sl;
  }

  /**
   * Updates the panel.
   */
  void update() {
    serial.init(gui.context.options.get(MainOptions.SERIALIZER));
  }

  /**
   * Closes the panel.
   */
  void cancel() {
    gui.set(MainOptions.SERIALIZER, serial.options());
  }
}
