package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for specifying the TreeMap layout.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
public final class DialogTreeOptions extends Dialog {
  /** Show attributes. */
  private final BaseXCheckBox atts;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogTreeOptions(final GUI main) {
    super(main, TREEOPTIONSTITLE, false);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 1, 0, 8));

    // create drop down
     final GUIProp gprop = gui.prop;


    // create checkbox
    atts = new BaseXCheckBox(TREEATT, gprop.is(GUIProp.TREEATTS), this);
    p.add(atts);

    set(p, BorderLayout.CENTER);
    finish(gprop.nums(GUIProp.MAPLAYOUTLOC));

  }

  @Override
  public void action(final Object cmp) {
    final GUIProp gprop = gui.prop;
    gprop.set(GUIProp.TREEATTS, atts.isSelected());
    gui.notify.layout();
  }
}
