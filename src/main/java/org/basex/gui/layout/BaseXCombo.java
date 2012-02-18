package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.dialog.Dialog;

/**
 * Project specific ComboBox implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXCombo extends JComboBox {
  /**
   * Constructor.
   * @param ch combobox choices
   * @param win parent window
   */
  public BaseXCombo(final Window win, final String... ch) {
    super(ch);
    BaseXLayout.addInteraction(this, win);

    if(!(win instanceof Dialog)) return;

    addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent ie) {
        if(isValid() && ie.getStateChange() == ItemEvent.SELECTED) {
          ((Dialog) win).action(ie.getSource());
        }
      }
    });
  }
}
