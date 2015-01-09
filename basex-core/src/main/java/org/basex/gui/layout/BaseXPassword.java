package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Project specific password field implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BaseXPassword extends JPasswordField {
  /**
   * Constructor.
   * @param win parent window
   */
  public BaseXPassword(final Window win) {
    BaseXLayout.setWidth(this, BaseXTextField.DWIDTH);
    BaseXLayout.addInteraction(this, win);

    if(!(win instanceof BaseXDialog)) return;

    addKeyListener(((BaseXDialog) win).keys);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        BaseXLayout.focus(e.getComponent());
      }
    });
  }
}
