package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.basex.core.Main;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXPassword;
import org.basex.gui.layout.BaseXLabel.Icon;

/**
 * Open database dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogPass extends Dialog {
  /** New password. */
  final BaseXPassword pass;
  /** Buttons. */
  final BaseXBack buttons;
  /** Info label. */
  final BaseXLabel info;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPass(final GUI main) {
    super(main, ALTERPW);

    pass = new BaseXPassword(this);
    pass.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(!modifier(e)) action(pressed(ENTER, e) ? e.getSource() : null);
      }
    });
    info = new BaseXLabel(" ");

    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout(0, 8));
    p.add(pass, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    buttons = newButtons(this, new Object[] { BUTTONOK, BUTTONCANCEL });
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    final String nm = new String(pass.getPassword());
    ok = !nm.isEmpty() && nm.matches("[\\w]*");
    info.setText(ok || nm.isEmpty() ? null :
      Main.info(INVALID, SERVERPW), Icon.ERR);
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
  }

  /**
   * Returns the password.
   * @return password
   */
  public String pass() {
    return new String(pass.getPassword());
  }
}
