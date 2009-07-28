package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import javax.swing.Box;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for specifying the TreeMap layout.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogMapLayout extends Dialog {
  /** Map layouts. */
  final BaseXListChooser algo;
  /** Layout slider. */
  final BaseXSlider sizeSlider;
  /** Show attributes. */
  final BaseXCheckBox atts;
  /** Select layout algorithm. */
  final BaseXCombo border;
  /** Size slider label. */
  final BaseXLabel sizeLabel;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapLayout(final GUI main) {
    super(main, MAPLAYOUTTITLE, false);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(4, 1, 0, 8));

    // create list
    algo = new BaseXListChooser(MAPALG, HELPMAPLAYOUT, this);
    p.add(algo);

    // create drop down
    final GUIProp gprop = gui.prop;
    border = new BaseXCombo(MAPOFFSET, HELPMAPOFF, this);
    border.setSelectedIndex(gprop.num(GUIProp.MAPOFFSETS));

    BaseXBack tmp = new BaseXBack();
    tmp.setLayout(new TableLayout(1, 3));
    tmp.add(new BaseXLabel(MAPOFF));
    tmp.add(Box.createHorizontalStrut(25));
    tmp.add(border);
    p.add(tmp);

    algo.setSize(tmp.getPreferredSize().width, 100);

    // create slider
    sizeLabel = new BaseXLabel(MAPSIZE);
    sizeSlider = new BaseXSlider(0, 100,
        gprop.num(GUIProp.MAPWEIGHT), HELPMAPSIZE, this);
    BaseXLayout.setWidth(sizeSlider, p.getPreferredSize().width);

    tmp = new BaseXBack();
    tmp.setLayout(new TableLayout(2, 1));
    tmp.add(sizeLabel);
    tmp.add(sizeSlider);
    p.add(tmp);

    // create checkbox
    atts = new BaseXCheckBox(MAPATT, HELPMAPATTS,
        gprop.is(GUIProp.MAPATTS), this);
    p.add(atts);

    set(p, BorderLayout.CENTER);
    finish(gprop.nums(GUIProp.MAPLAYOUTLOC));

    algo.setIndex(gprop.num(GUIProp.MAPALGO));
  }

  @Override
  public void action(final String cmd) {
    final boolean fs = gui.context.data().fs != null;
    final GUIProp gprop = gui.prop;
    gprop.set(GUIProp.MAPOFFSETS, border.getSelectedIndex());
    gprop.set(GUIProp.MAPALGO, algo.getIndex());
    gprop.set(GUIProp.MAPATTS, atts.isSelected());
    final int sizeprp = sizeSlider.value();
    gprop.set(GUIProp.MAPWEIGHT, sizeprp);
    sizeLabel.setText(MAPSIZE + " " + (sizeprp > 45 && sizeprp < 55 ?
      MAPBOTH : sizeprp < 45 ?  MAPCHILDREN : fs ? MAPFSSIZE : MAPTEXTSIZE));

    gui.notify.layout();
  }
}
