package org.basex.gui.dialog;

import static org.basex.Text.*;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import org.basex.core.proc.List;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.io.IO;
import org.basex.util.StringList;

/**
 * Open Database Dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  final StringList db = List.list();

  /**
   * Default Constructor.
   * @param parent parent frame
   * @param dbname name of database
   */
  public DialogRename(final JFrame parent, final String dbname) {
    super(parent, RENAMETITLE);
    old = dbname;

    info = new BaseXLabel(" ");
    info.setForeground(GUIConstants.COLORERROR);

    name = new BaseXTextField(dbname, null, this);
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

    buttons = BaseXLayout.okCancel(this);
    p.add(buttons, BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);

    finish(parent);
  }

  @Override
  public void action(final String cmd) {
    final String nm = name.getText().trim();
    ok = !db.contains(nm) || nm.equals(old);
    String inf = ok ? "" : RENAMEEXISTS;
    if(ok) {
      ok = nm.length() != 0 && IO.valid(nm);
      if(!ok && nm.length() != 0) inf = RENAMEINVALID;
    }
    info.setText(inf);
    BaseXLayout.enableOK(buttons, BUTTONOK, ok);
  }


  @Override
  public void close() {
    if(!ok) return;
    super.close();
    IO.dbpath(old).renameTo(IO.dbpath(name.getText().trim()));
  }
}
