package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.List;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.io.IO;
import org.basex.util.StringList;

/**
 * Open database dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogRename extends Dialog {
  /** Old name. */
  final String old;
  /** New name. */
  final BaseXTextField name;
  /** Buttons. */
  final BaseXBack buttons;
  /** Info label. */
  final BaseXLabel info;
  /** Available databases. */
  final StringList db;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param dbname name of database
   */
  public DialogRename(final GUI main, final String dbname) {
    super(main, RENAMETITLE);
    old = dbname;
    db = List.list(main.context);

    info = new BaseXLabel(" ");
    info.setForeground(GUIConstants.COLORERROR);

    name = new BaseXTextField(dbname, this);
    name.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });

    set(name, BorderLayout.NORTH);
    set(info, BorderLayout.CENTER);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());

    buttons = okCancel(this);
    p.add(buttons, BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);

    finish(null);
  }

  @Override
  public void action(final String cmd) {
    final String nm = name.getText().trim();
    ok = !db.contains(nm) || nm.equals(old);
    String inf = ok ? "" : RENAMEEXISTS;
    if(ok) {
      ok = !nm.isEmpty() && IO.valid(nm);
      if(!ok && !nm.isEmpty()) inf = Main.info(INVALID, EDITNAME);
    }
    info.setText(inf);
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
    final Prop prop = gui.context.prop;
    prop.dbpath(old).renameTo(prop.dbpath(name.getText().trim()));
  }
}
