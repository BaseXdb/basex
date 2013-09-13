package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for specifying the TreeMap layout.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogMapLayout extends BaseXDialog {
  /** Map layouts. */
  private final BaseXList algo;
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
    algo = new BaseXList(MAP_LAYOUTS, this);
    p.add(algo);

    // create drop down
    final GUIProp gprop = gui.gprop;
    border = new BaseXCombo(this, MAP_CHOICES);
    border.setSelectedIndex(gprop.num(GUIProp.MAPOFFSETS));

    BaseXBack tmp = new BaseXBack(new TableLayout(1, 3));
    tmp.add(new BaseXLabel(OFFSETS + COL));
    tmp.add(Box.createHorizontalStrut(25));
    tmp.add(border);
    p.add(tmp);

    algo.setSize(200, 100);

    // create slider
    sizeLabel = new BaseXLabel(RATIO + COLS);
    sizeSlider = new BaseXSlider(0, 100, gprop.num(GUIProp.MAPWEIGHT), this);
    BaseXLayout.setWidth(sizeSlider, p.getPreferredSize().width);

    tmp = new BaseXBack(new TableLayout(2, 1));
    tmp.add(sizeLabel);
    tmp.add(sizeSlider);
    p.add(tmp);

    // create checkbox
    atts = new BaseXCheckBox(SHOW_ATTS, gprop.is(GUIProp.MAPATTS), this);
    p.add(atts);

    set(p, BorderLayout.CENTER);
    finish(gprop.nums(GUIProp.MAPLAYOUTLOC));

    algo.setIndex(gprop.num(GUIProp.MAPALGO));
  }

  @Override
  public void action(final Object cmp) {
    final GUIProp gprop = gui.gprop;
    gprop.set(GUIProp.MAPOFFSETS, border.getSelectedIndex());
    gprop.set(GUIProp.MAPALGO, algo.getIndex());
    gprop.set(GUIProp.MAPATTS, atts.isSelected());
    final int sizeprp = sizeSlider.value();
    gprop.set(GUIProp.MAPWEIGHT, sizeprp);
    sizeLabel.setText(RATIO + COLS + (sizeprp > 45 && sizeprp < 55 ?
      CHILDREN_TEXT_LEN : sizeprp < 45 ?  NUMBER_CHILDREN : TEXT_LENGTH));

    gui.notify.layout();
  }
}
