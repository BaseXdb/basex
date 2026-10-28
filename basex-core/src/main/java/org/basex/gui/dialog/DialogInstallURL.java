package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.layout.*;

/**
 * URL dialog.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogInstallURL extends BaseXDialog {
  /** URL. */
  private final BaseXTextField url;
  /** Buttons. */
  private final BaseXBack buttons;

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogInstallURL(final BaseXDialog dialog) {
    super(dialog, INSTALL_FROM_URL);

    url = new BaseXTextField(this);

    final BaseXLabel link = BaseXLayout.link(REPO_URL, gui);

    set(url, BorderLayout.CENTER);

    final BaseXBack p = new BaseXBack(new BorderLayout());
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
