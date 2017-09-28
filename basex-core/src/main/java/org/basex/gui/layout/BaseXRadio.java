package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;

/**
 * Project specific RadioButton implementation.
 *
 * @author BaseX Team 2005-17, BSD License
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
    setBorder(BaseXLayout.border(0, 0, 0, 16));
    BaseXLayout.addInteraction(this, win);
    if(win instanceof BaseXDialog)
      addActionListener(e -> ((BaseXDialog) win).action(e.getSource()));
  }
}
