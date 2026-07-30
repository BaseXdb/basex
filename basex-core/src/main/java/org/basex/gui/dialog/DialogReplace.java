package org.basex.gui.dialog;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Dialog window for confirming a content replacement.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DialogReplace extends BaseXDialog {
  /** Create backups. */
  private final BaseXCheckBox backup;
  /** Backup path label. */
  private final TextPanel path;
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
    super(gui, Text.INFORMATION);
    this.dir = dir;

    panel.setLayout(new BorderLayout());

    final BaseXBack back = new BaseXBack(new RowLayout(8));
    final TextPanel header = new TextPanel(this,
        Util.info(Text.REPLACE_FILES_X_X + ' ' + Text.ARE_YOU_SURE, BaseXLayout.format(strings),
            BaseXLayout.format(files)), false);
    header.setFont(back.getFont());
    back.add(header);

    backup = new BaseXCheckBox(this, Text.CREATE_BACKUP, GUIOptions.PROJBACKUP, gui.gopts);
    path = new TextPanel(this, false);
    path.setFont(back.getFont());
    final BaseXBack row = new BaseXBack(new RowLayout());
    row.add(backup);
    row.add(path);
    back.add(row);
    set(back, BorderLayout.CENTER);

    final BaseXBack bttns = okCancel();
    set(bttns, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(((Container) bttns.getComponent(0)).getComponent(0)::
      requestFocusInWindow);
    action(backup);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    path.setText(backup.isSelected() ? dir.path() : "");
    path.revalidate();
    pack();
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
