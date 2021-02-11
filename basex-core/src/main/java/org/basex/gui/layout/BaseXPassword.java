package org.basex.gui.layout;

import javax.swing.*;

import org.basex.gui.listener.*;

/**
 * Project specific password field implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXPassword extends JPasswordField {
  /**
   * Constructor.
   * @param win parent window
   */
  public BaseXPassword(final BaseXWindow win) {
    BaseXLayout.setWidth(this, BaseXTextField.DWIDTH);
    BaseXLayout.addInteraction(this, win);

    final BaseXDialog dialog = win.dialog();
    if(dialog == null) return;

    addKeyListener(dialog.keys);
    addMouseListener((MouseEnteredListener) e -> BaseXLayout.focus(this));
  }
}
