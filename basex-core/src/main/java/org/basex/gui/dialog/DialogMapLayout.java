package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for specifying the TreeMap layout.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogMapLayout extends BaseXDialog {
  /** Algorithm combobox. */
  private final BaseXCombo algo;
  /** Layout slider. */
  private final BaseXSlider sizeSlider;
  /** Show attributes. */
  private final BaseXCheckBox atts;
  /** Select layout algorithm. */
  private final BaseXCombo border;
  /** Size slider label. */
  private final BaseXLabel sizeLabel;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapLayout(final GUI main) {
    super(main, MAP_LAYOUT, false);

    final BaseXBack p = new BaseXBack(new TableLayout(4, 1, 0, 8));

    // create list
    BaseXBack tmp = new BaseXBack(new TableLayout(1, 2, 8, 0));
    tmp.add(new BaseXLabel(ALGORITHM + COL));
    algo = new BaseXCombo(this, MAP_LAYOUTS);
    tmp.add(algo);
    p.add(tmp);

    // create drop down
    final GUIOptions gopts = gui.gopts;
    border = new BaseXCombo(this, MAP_CHOICES);
    border.setSelectedIndex(gopts.num(GUIOptions.MAPOFFSETS));

    tmp = new BaseXBack(new TableLayout(1, 2, 8, 0));
    tmp.add(new BaseXLabel(OFFSETS + COL));
    tmp.add(border);
    p.add(tmp);

    algo.setSize(200, 100);

    // create slider
    sizeLabel = new BaseXLabel(RATIO + COLS);
    sizeSlider = new BaseXSlider(0, 100, gopts.num(GUIOptions.MAPWEIGHT), this);
    BaseXLayout.setWidth(sizeSlider, p.getPreferredSize().width);

    tmp = new BaseXBack(new TableLayout(2, 1));
    tmp.add(sizeLabel);
    tmp.add(sizeSlider);
    p.add(tmp);

    // create checkbox
    atts = new BaseXCheckBox(SHOW_ATTS, gopts.is(GUIOptions.MAPATTS), this);
    p.add(atts);

    set(p, BorderLayout.CENTER);
    finish(gopts.nums(GUIOptions.MAPLAYOUTLOC));

    algo.setSelectedIndex(gopts.num(GUIOptions.MAPALGO));
  }

  @Override
  public void action(final Object cmp) {
    final GUIOptions gopts = gui.gopts;
    gopts.set(GUIOptions.MAPOFFSETS, border.getSelectedIndex());
    gopts.set(GUIOptions.MAPALGO, algo.getSelectedIndex());
    gopts.set(GUIOptions.MAPATTS, atts.isSelected());
    final int sizeprp = sizeSlider.value();
    gopts.set(GUIOptions.MAPWEIGHT, sizeprp);
    sizeLabel.setText(RATIO + COLS + (sizeprp > 45 && sizeprp < 55 ?
      CHILDREN_TEXT_LEN : sizeprp < 45 ?  NUMBER_CHILDREN : TEXT_LENGTH));

    gui.notify.layout();
  }
}
