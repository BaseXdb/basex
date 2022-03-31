package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.layout.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dialog with a single text field.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
final class DialogInput extends BaseXDialog {
  /** User input. */
  private final BaseXTextField input;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;
  /** Available databases. */
  private final StringList databases;
  /** Rename/copy/delete dialog. */
  private final Action action;

  /** Action. */
  enum Action {
    /** Rename document. */ RENAME_DOCUMENT(RENAME, TARGET_PATH),
    /** Alter database.  */ ALTER_DATABASE(RENAME_DB, NAME_OF_DB),
    /** Copy database.   */ COPY_DATABASE(COPY_DB, NAME_OF_DB_COPY),
    /** Create backup.   */ CREATE_BACKUP(COMMENT, COMMENT);

    /** Title of action. */
    final String title;
    /** Descriptive label. */
    final String label;

    /**
     * Constructor.
     * @param title title of action
     * @param label descriptive label
     */
    Action(final String title, final String label) {
      this.title = title;
      this.label = label;
    }
  }

  /**
   * Default constructor.
   * @param old old input
   * @param dialog dialog window
   * @param action action
   */
  DialogInput(final String old, final BaseXDialog dialog, final Action action) {
    super(dialog, action.title);
    this.action = action;
    databases = dialog.gui.context.listDBs();

    set(new BaseXLabel(action.label + COL, false, true).border(0, 0, 6, 0), BorderLayout.NORTH);

    input = new BaseXTextField(this, old);
    info = new BaseXLabel(" ");

    final BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(input, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    buttons = newButtons(B_OK, B_CANCEL);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish();
  }

  /**
   * Returns the user input.
   * @return input
   */
  String input() {
    return input.getText().trim();
  }

  @Override
  public void action(final Object cmp) {
    final String in = input();

    String inf = null;
    Msg icon = Msg.ERROR;
    if(action == Action.RENAME_DOCUMENT) {
      // document checks
      ok = !in.isEmpty() && MetaData.normPath(in) != null;
      if(!ok) inf = Util.info(INVALID_X, PATH);
    } else if(action == Action.CREATE_BACKUP) {
      ok = true;
    } else {
      // database checks
      ok = Databases.validName(in);
      if(!ok) {
        inf = Util.info(INVALID_X, NAME);
      } else if(databases.contains(in)) {
        inf = Util.info(DB_EXISTS_X, in);
        icon = Msg.WARN;
      }
    }
    info.setText(inf, icon);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(ok) super.close();
  }
}
