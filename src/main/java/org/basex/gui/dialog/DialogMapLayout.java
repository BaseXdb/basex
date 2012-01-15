package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import javax.swing.Box;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXList;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for specifying the TreeMap layout.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogMapLayout extends Dialog {
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
    super(main, MAPLAYOUTTITLE, false);

    final BaseXBack p = new BaseXBack(new TableLayout(4, 1, 0, 8));

    // create list
    algo = new BaseXList(MAPALG, this);
    p.add(algo);

    // create drop down
    final GUIProp gprop = gui.gprop;
    border = new BaseXCombo(this, MAPOFFSET);
    border.setSelectedIndex(gprop.num(GUIProp.MAPOFFSETS));

    BaseXBack tmp = new BaseXBack(new TableLayout(1, 3));
    tmp.add(new BaseXLabel(MAPOFF));
    tmp.add(Box.createHorizontalStrut(25));
    tmp.add(border);
    p.add(tmp);

    algo.setSize(200, 100);

    // create slider
    sizeLabel = new BaseXLabel(MAPSIZE);
    sizeSlider = new BaseXSlider(0, 100, gprop.num(GUIProp.MAPWEIGHT), this);
    BaseXLayout.setWidth(sizeSlider, p.getPreferredSize().width);

    tmp = new BaseXBack(new TableLayout(2, 1));
    tmp.add(sizeLabel);
    tmp.add(sizeSlider);
    p.add(tmp);

    // create checkbox
    atts = new BaseXCheckBox(MAPATT, gprop.is(GUIProp.MAPATTS), this);
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
    sizeLabel.setText(MAPSIZE + " " + (sizeprp > 45 && sizeprp < 55 ?
      MAPBOTH : sizeprp < 45 ?  MAPCHILDREN : MAPTEXTSIZE));

    gui.notify.layout();
  }
}
