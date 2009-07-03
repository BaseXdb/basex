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
  /** devide rectangles uniformly. */
  final BaseXCheckBox s;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapLayout(final GUI main) {
    super(main, MAPLAYOUTTITLE, false);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(4, 1, 0, 8));

    // create list
    algo = new BaseXListChooser(this, MAPALGO, HELPMAPLAYOUT);
    p.add(algo);

    // create drop down
    border = new BaseXCombo(MAPOFFSETS, HELPMAPOFF, this);
    border.setSelectedIndex(GUIProp.mapoffsets);

    BaseXBack tmp = new BaseXBack();
    tmp.setLayout(new TableLayout(1, 3));
    tmp.add(new BaseXLabel(MAPOFF));
    tmp.add(Box.createHorizontalStrut(25));
    tmp.add(border);
    p.add(tmp);

    algo.setSize(tmp.getPreferredSize().width, 100);

    // create slider
    sizeLabel = new BaseXLabel(MAPSIZE);
    sizeSlider = new BaseXSlider(gui, 0, 100,
        GUIProp.mapweight, HELPMAPSIZE, this);
    BaseXLayout.setWidth(sizeSlider, p.getPreferredSize().width);

    tmp = new BaseXBack();
    tmp.setLayout(new TableLayout(2, 1));
    tmp.add(sizeLabel);
    tmp.add(sizeSlider);
    p.add(tmp);

    // create checkbox
    atts = new BaseXCheckBox(MAPATTS, HELPMAPATTS, GUIProp.mapatts, this);
    p.add(atts);

    s = new BaseXCheckBox(MAPSIMPLE, HELPMAPSIMPLE, GUIProp.mapsimple, this);
//    p.add(s);

    set(p, BorderLayout.CENTER);
    finish(GUIProp.maplayoutloc);

    algo.setIndex(GUIProp.mapalgo);
  }

  @Override
  public void action(final String cmd) {
    final boolean fs = gui.context.data().fs != null;
    GUIProp.mapoffsets = border.getSelectedIndex();
    GUIProp.mapalgo = algo.getIndex();
    GUIProp.mapatts = atts.isSelected();
    GUIProp.mapsimple = s.isSelected();
    final int sizeprp = sizeSlider.value();
    GUIProp.mapweight = sizeprp;
    sizeLabel.setText(MAPSIZE + " " + (sizeprp > 45 && sizeprp < 55 ?
      MAPBOTH : sizeprp < 45 ?  MAPCHILDREN : (fs ? MAPFSSIZE : MAPTEXTSIZE)));

    gui.notify.layout();
  }
}
