package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Sort dialog.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DialogSort extends BaseXDialog {
  /** Case sensitive. */
  private final BaseXCheckBox cs;
  /** Sort ascending. */
  private final BaseXCheckBox asc;
  /** Merge duplicate lines. */
  private final BaseXCheckBox merge;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogSort(final GUI main) {
    super(main, SORT);

    final BaseXBack p = new BaseXBack(new TableLayout(3, 1));

    final GUIOptions gopts = gui.gopts;
    asc = new BaseXCheckBox(ASCENDING_ORDER, GUIOptions.ASCSORT, gopts, this);
    cs = new BaseXCheckBox(CASE_SENSITIVE, GUIOptions.CASESORT, gopts, this);
    merge = new BaseXCheckBox(MERGE_DUPLICATES, GUIOptions.MERGEDUPL, gopts, this);
    p.add(asc);
    p.add(cs);
    p.add(merge);
    set(p, BorderLayout.CENTER);

    set(newButtons(B_OK, CANCEL), BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  @Override
  public void close() {
    cs.assign();
    asc.assign();
    merge.assign();
    super.close();
  }
}
