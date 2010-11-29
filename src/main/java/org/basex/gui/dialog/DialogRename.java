package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.basex.core.Command;
import org.basex.core.cmd.List;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.util.StringList;
import org.basex.util.Util;

/**
 * Rename database dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DialogRename extends Dialog {
  /** New name. */
  private final BaseXTextField name;
  /** Old name. */
  private final String old;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;
  /** Available databases. */
  private final StringList db;

  /**
   * Default constructor.
   * @param dbname name of database
   * @param main reference to the main window
   * @param fs file system flag
   */
  DialogRename(final String dbname, final GUI main, final boolean fs) {
    super(main, RENAMETITLE);
    old = dbname;
    db = fs ? List.listFS(main.context) : List.list(main.context);

    set(new BaseXLabel(CREATENAME, false, true).border(0, 0, 4, 0),
        BorderLayout.NORTH);

    name = new BaseXTextField(dbname, this);
    name.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(!modifier(e)) action(ENTER.is(e) ? e.getSource() : null);
      }
    });
    info = new BaseXLabel(" ");

    final BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(name, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    buttons = newButtons(this, BUTTONOK, BUTTONCANCEL);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  /**
   * Returns the edited database name.
   * @return name
   */
  String name() {
    return name.getText().trim();
  }

  @Override
  public void action(final Object cmp) {
    final String nm = name();
    ok = !db.contains(nm) || nm.equals(old);
    String msg = ok ? null : Util.info(DBEXISTS, nm);
    if(ok) {
      ok = Command.validName(nm);
      if(!ok) msg =  nm.isEmpty() ? DBWHICH : Util.info(INVALID, EDITNAME);
    }
    info.setText(msg, Msg.ERROR);
    enableOK(buttons, BUTTONOK, ok && !nm.isEmpty());
  }

  @Override
  public void close() {
    if(ok) super.close();
  }
}
