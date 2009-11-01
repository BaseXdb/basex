package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific RadioButton implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXRadio extends JRadioButton {
  /**
   * Default constructor.
   * @param label button title
   * @param sel initial selection state
   * @param win parent window
   */
  public BaseXRadio(final String label, final boolean sel, final Window win) {
    super(label, sel);
    setOpaque(false);
    setBorder(new EmptyBorder(0, 0, 0, 16));
    BaseXLayout.addInteraction(this, null, win);

    if(!(win instanceof Dialog)) return;
    addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        ((Dialog) win).action(getText());
      }
    });
  }
}
