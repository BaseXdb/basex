package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXPassword;
import org.basex.util.Util;

/**
 * Open database dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogPass extends Dialog {
  /** New password. */
  private final BaseXPassword pass;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;

  /**
   * Default constructor creating invisible frame as main window.
   */
  public DialogPass() {
    this(null);
  }

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  DialogPass(final GUI main) {
    super(main, ALTER_PW);

    pass = new BaseXPassword(this);
    pass.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(!modifier(e)) action(ENTER.is(e) ? e.getSource() : null);
      }
    });
    info = new BaseXLabel(" ");

    final BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(pass, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    buttons = newButtons(B_OK, CANCEL);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    final String nm = pass();
    ok = !nm.isEmpty() && nm.matches("[^ ;'\"]*");
    info.setText(ok || nm.isEmpty() ? null :
      Util.info(INVALID_X, PASSWORD), Msg.ERROR);
    enableOK(buttons, B_OK, ok);
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
