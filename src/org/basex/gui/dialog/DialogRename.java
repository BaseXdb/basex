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
import org.basex.util.Token;

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

  /**
   * Default Constructor.
   * @param parent parent frame
   * @param dbname name of database
   */
  public DialogRename(final JFrame parent, final String dbname) {
    super(parent, RENAMETITLE);
    old = dbname;

    final BaseXBack buttons = BaseXLayout.okCancel(this);
    final BaseXLabel info = new BaseXLabel(" ");
    info.setForeground(GUIConstants.COLORERROR);

    // create database chooser
    final String[] db = List.list();
    
    name = new BaseXTextField(dbname, null, this);
    name.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        final String nm = name.getText();
        ok = true;
        for(final String d : db) ok &= !d.equals(nm);
        ok |= nm.equals(dbname);
        String inf = ok ? "" : RENAMEEXISTS;
        if(ok) {
          ok = nm.length() != 0 &&
            Token.letterOrDigit(Token.token(nm));
          if(!ok) inf = RENAMEINVALID;
        }
        info.setText(inf);
        BaseXLayout.enableOK(buttons, ok);
      }
    });

    set(name, BorderLayout.NORTH);
    set(info, BorderLayout.CENTER);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());

    p.add(buttons, BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);

    finish(parent);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
    
    final String nm = name.getText();
    IO.dbpath(old).renameTo(IO.dbpath(nm));
  }
}
