package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.options.*;

/**
 * View preferences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogViewsPrefs extends BaseXBack {
  /** Maximum color range. */
  private static final int MAXCOLOR = 32;

  /** GUI reference. */
  private final GUI gui;

  /** Show names checkbox. */
  private final BaseXTextField labels;

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
  /** Focus checkbox. */
  private final BaseXCheckBox mousefocus;
  /** Select layout algorithm. */
  private final BaseXCombo mapOffsets;

  /** Red slider. */
  private final BaseXSlider colorRed;
  /** Green slider. */
  private final BaseXSlider colorGreen;
  /** Blue slider. */
  private final BaseXSlider colorBlue;
  /** Reset colors. */
  private final BaseXButton colorReset;

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogViewsPrefs(final BaseXDialog dialog) {
    border(8).setLayout(new ColumnLayout(40));
    gui = dialog.gui();

    final GUIOptions gopts = gui.gopts;
    labels = new BaseXTextField(dialog, GUIOptions.LABELS, gopts);
    mousefocus = new BaseXCheckBox(dialog, RT_FOCUS, GUIOptions.MOUSEFOCUS, gopts);
    treeSlims = new BaseXCheckBox(dialog, ADJUST_NODES, GUIOptions.TREESLIMS, gopts);
    treeAtts = new BaseXCheckBox(dialog, SHOW_ATTS, GUIOptions.TREEATTS, gopts);
    mapAlgo = new BaseXCombo(dialog, GUIOptions.MAPALGO, gopts, MAP_LAYOUTS);
    mapOffsets = new BaseXCombo(dialog, GUIOptions.MAPOFFSETS, gopts, MAP_CHOICES);
    mapWeight = new BaseXSlider(dialog, 0, 100, GUIOptions.MAPWEIGHT, gopts);
    mapAtts = new BaseXCheckBox(dialog, SHOW_ATTS, GUIOptions.MAPATTS, gopts);
    colorRed = colorSlider(dialog, GUIOptions.COLORRED);
    colorGreen = colorSlider(dialog, GUIOptions.COLORGREEN);
    colorBlue = colorSlider(dialog, GUIOptions.COLORBLUE);
    colorReset = new BaseXButton(dialog, RESET);
    mapAlgo.setSize(200, 100);
    labels.setColumns(18);
    BaseXLayout.setWidth(mapWeight, 150);

    BaseXBack p = new BaseXBack().layout(new RowLayout(8)), pp;
    pp = new BaseXBack(new RowLayout());
    pp.add(new BaseXLabel(GENERAL + COL, true, true));
    pp.add(mousefocus);
    p.add(pp);

    pp = new BaseXBack(new RowLayout());
    pp.add(new BaseXLabel(LABEL_ATTS + COL, true, true));
    pp.add(labels);
    p.add(pp);

    pp = new BaseXBack(new RowLayout());
    pp.add(new BaseXLabel(TREE + COL, true, true));
    pp.add(treeSlims);
    pp.add(treeAtts);
    p.add(pp);

    add(p);

    p = new BaseXBack(new RowLayout());
    p.add(new BaseXLabel(MAP + COL, true, true));

    pp = new BaseXBack(new TableLayout(2, 2, 8, 8));
    pp.add(new BaseXLabel(ALGORITHM + COL));
    pp.add(mapAlgo);
    pp.add(new BaseXLabel(OFFSETS + COL));
    pp.add(mapOffsets);
    p.add(pp);

    pp = new BaseXBack(new RowLayout(8));
    pp.add(new BaseXLabel(RATIO + COLS));
    pp.add(mapWeight);
    pp.add(mapAtts);
    p.add(pp);

    add(p);

    p = new BaseXBack(new RowLayout());
    p.add(new BaseXLabel(COLORS + COL, true, true));
    pp = new BaseXBack(new TableLayout(3, 2, 16, 8));
    pp.add(new BaseXLabel(RED));
    pp.add(colorRed);
    pp.add(new BaseXLabel(GREEN));
    pp.add(colorGreen);
    pp.add(new BaseXLabel(BLUE));
    pp.add(colorBlue);
    p.add(pp);
    p.add(colorReset);
    add(p);
  }

  /**
   * Reacts on user input.
   * @param source source
   * @return success flag
   */
  boolean action(final Object source) {
    mousefocus.assign();
    treeSlims.assign();
    treeAtts.assign();
    mapAtts.assign();
    labels.assign();
    mapWeight.assign();
    mapAlgo.assign();
    mapOffsets.assign();

    if(source == colorReset) {
      colorRed.setValue(GUIOptions.COLORRED.value());
      colorGreen.setValue(GUIOptions.COLORGREEN.value());
      colorBlue.setValue(GUIOptions.COLORBLUE.value());
    }
    colorRed.assign();
    colorGreen.assign();
    colorBlue.assign();
    if(source == colorReset || source == colorRed || source == colorGreen ||
        source == colorBlue) gui.updateLayout();
    return true;
  }

  /**
   * Creates a color slider.
   * @param dialog dialog reference
   * @param option option
   * @return slider reference
   */
  private static BaseXSlider colorSlider(final BaseXDialog dialog, final NumberOption option) {
    final BaseXSlider slider = new BaseXSlider(dialog, 0, MAXCOLOR, option, dialog.gui().gopts);
    BaseXLayout.setWidth(slider, 150);
    return slider;
  }
}
