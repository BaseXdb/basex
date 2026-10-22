package org.basex.gui.dialog;

import java.awt.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Dialog window for confirming a content replacement.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DialogReplace extends BaseXDialog {
  /** Yes button. */
  private final BaseXButton yes;
  /** No button. */
  private final BaseXButton no;
  /** Create backups. */
  private final BaseXCheckBox backup;
  /** Backup path label. */
  private final BaseXTextArea path;
  /** Backup directory. */
  private final IOFile dir;

  /**
   * Default constructor.
   * @param gui reference to the main window
   * @param strings number of strings to be replaced
   * @param files number of files to be changed
   * @param dir backup directory
   */
  public DialogReplace(final GUI gui, final int strings, final int files, final IOFile dir) {
    super(gui, Text.QUESTION);
    this.dir = dir;

    panel.setLayout(new BorderLayout());

    final BaseXBack back = new BaseXBack(new RowLayout(8));
    back.add(new BaseXTextArea(this,
        Util.info(Text.REPLACE_FILES_X_X + ' ' + Text.ARE_YOU_SURE, BaseXLayout.format(strings),
            BaseXLayout.format(files))));

    backup = new BaseXCheckBox(this, Text.CREATE_BACKUP, GUIOptions.PROJBACKUP, gui.gopts);
    path = new BaseXTextArea(this, "");
    final BaseXBack row = new BaseXBack(new RowLayout());
    row.add(backup);
    row.add(path);
    back.add(row);
    set(back, BorderLayout.CENTER);

    yes = new BaseXButton(this, Text.B_YES);
    no = new BaseXButton(this, Text.B_NO);
    setButtons(newButtons(yes, no), 1);
    BaseXLayout.defaultButton(back, no);
    action(backup);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    if(cmp == yes) {
      close();
    } else if(cmp == no) {
      cancel();
    } else {
      path.setText(backup.isSelected() ? dir.path() : "");
      path.revalidate();
      pack();
    }
  }

  @Override
  public void close() {
    backup.assign();
    super.close();
  }

  /**
   * Indicates if backups are to be created.
   * @return result of check
   */
  public boolean backup() {
    return backup.isSelected();
  }
}
