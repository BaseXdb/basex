package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPasswordField;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific password field implementation.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class BaseXPassword extends JPasswordField {
  /** Default width of text fields. */
  private static final int DWIDTH = 260;

  /**
   * Constructor.
   * @param win parent window
   */
  public BaseXPassword(final Window win) {
    BaseXLayout.setWidth(this, DWIDTH);
    BaseXLayout.addInteraction(this, null, win);

    if(!(win instanceof Dialog)) return;
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        ((Dialog) win).action(e.getSource());
      }
    });
  }
}
