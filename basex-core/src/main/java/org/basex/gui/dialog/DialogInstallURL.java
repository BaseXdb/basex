package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.util.*;

/**
 * URL dialog.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DialogInstallURL extends BaseXDialog {
  /** URL. */
  private final BaseXTextField url;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogInstallURL(final BaseXDialog dialog) {
    super(dialog, INSTALL_FROM_URL);

    url = new BaseXTextField(this);
    info = new BaseXLabel(" ");

    final BaseXLabel link = new BaseXLabel("<html><u>" + REPO_URL + "</u></html>");
    link.setForeground(GUIConstants.BLUE);
    link.setCursor(GUIConstants.CURSORHAND);
    link.addMouseListener((MouseClickedListener) e -> BaseXDialog.browse(gui, REPO_URL));

    BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(url, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    p = new BaseXBack(new BorderLayout());
    p.add(link, BorderLayout.WEST);
    buttons = newButtons(B_OK, B_CANCEL);
    p.add(buttons, BorderLayout.EAST);

    set(p, BorderLayout.SOUTH);
    action(null);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    ok = !url().isEmpty();
    info.setText(ok ? null : Util.info(INVALID_X, "URL"), Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(ok) super.close();
  }

  /**
   * Returns the url.
   * @return url
   */
  String url() {
    return url.getText();
  }
}
