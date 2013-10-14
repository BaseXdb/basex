package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for specifying the TreeMap layout.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Wolfgang Miller
 */
public final class DialogTreeOptions extends BaseXDialog {
  /** Slim rectangles to text length. */
  private final BaseXCheckBox slim;
  /** Show attributes. */
  private final BaseXCheckBox atts;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogTreeOptions(final GUI main) {
    super(main, TREE_OPTIONS, false);

    final BaseXBack p = new BaseXBack(new TableLayout(2, 1, 0, 8));
    final GUIOptions gopts = gui.gopts;

    // create checkbox
    slim = new BaseXCheckBox(ADJUST_NODES, GUIOptions.TREESLIMS, gopts, this);
    p.add(slim);

    // create checkbox
    atts = new BaseXCheckBox(SHOW_ATTS, GUIOptions.TREEATTS, gopts, this);
    p.add(atts);

    set(p, BorderLayout.CENTER);
    finish(gopts.get(GUIOptions.MAPLAYOUTLOC));
  }

  @Override
  public void action(final Object cmp) {
    final GUIOptions gopts = gui.gopts;
    gopts.set(GUIOptions.TREESLIMS, slim.isSelected());
    gopts.set(GUIOptions.TREEATTS, atts.isSelected());
    gui.notify.layout();
  }
}
