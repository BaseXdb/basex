package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Sort dialog.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class DialogSort extends BaseXDialog {
  /** Case sensitive. */
  private final BaseXCheckBox cs;
  /** Sort ascending. */
  private final BaseXCheckBox asc;
  /** Merge duplicate lines. */
  private final BaseXCheckBox merge;
  /** Column. */
  private final BaseXTextField column;
  /** Collation. */
  private final BaseXTextField coll;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogSort(final GUI main) {
    super(main, SORT);

    final BaseXBack p = new BaseXBack(new TableLayout(5, 1));

    final GUIOptions gopts = gui.gopts;
    asc = new BaseXCheckBox(ASCENDING_ORDER, GUIOptions.ASCSORT, gopts, this);
    cs = new BaseXCheckBox(CASE_SENSITIVE, GUIOptions.CASESORT, gopts, this);
    merge = new BaseXCheckBox(MERGE_DUPLICATES, GUIOptions.MERGEDUPL, gopts, this);
    column = new BaseXTextField(GUIOptions.COLUMN, gopts, this);
    coll = new BaseXTextField(GUIOptions.COLLATION, gopts, this).hint("lang=en;strength=primary");
    column.setColumns(4);
    coll.setColumns(20);

    final BaseXBack pp = new BaseXBack(new TableLayout(2, 2, 8, 4));
    pp.add(new BaseXLabel("Collation" + COLS));
    pp.add(coll);
    pp.add(new BaseXLabel(COLUMN + COLS));
    pp.add(column);

    p.add(pp);
    p.add(Box.createVerticalStrut(4));
    p.add(cs);
    p.add(asc);
    p.add(merge);
    set(p, BorderLayout.CENTER);

    set(newButtons(B_OK, B_CANCEL), BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  @Override
  public void action(final Object source) {
    cs.setEnabled(coll.getText().isEmpty());
  }

  @Override
  public void close() {
    cs.assign();
    asc.assign();
    merge.assign();
    column.assign();
    coll.assign();
    super.close();
  }
}
