package org.basex.gui.layout;

import javax.swing.*;

/**
 * Project specific radio button implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXRadio extends JRadioButton {
  /**
   * Default constructor.
   * @param win parent window
   * @param text button text
   * @param selected initial selection state
   */
  public BaseXRadio(final BaseXWindow win, final String text, final boolean selected) {
    super(text, selected);
    setOpaque(false);
    setBorder(BaseXLayout.border(0, 0, 0, 16));
    BaseXLayout.addInteraction(this, win);

    final BaseXDialog dialog = win.dialog();
    if(dialog != null) addActionListener(e -> dialog.action(e.getSource()));
  }
}
