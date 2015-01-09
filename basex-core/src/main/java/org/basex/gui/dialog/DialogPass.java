package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Password dialog.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DialogPass extends BaseXDialog {
  /** New password. */
  private final BaseXPassword pass;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPass(final GUI main) {
    super(main, ALTER_PW);

    pass = new BaseXPassword(this);
    pass.addKeyListener(keys);
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
    final String nm = password();
    ok = nm.matches("[^ ;'\"]*");
    info.setText(ok ? null : Util.info(INVALID_X, PASSWORD), Msg.ERROR);
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
  public String password() {
    return new String(pass.getPassword());
  }
}
