package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPasswordField;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific password field implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class BaseXPassword extends JPasswordField {
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
