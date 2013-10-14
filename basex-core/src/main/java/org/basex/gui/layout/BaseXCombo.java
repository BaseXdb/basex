package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Project specific ComboBox implementation.
 *
 * @author BaseX Team 2005-13, BSD License
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
    if(!(win instanceof BaseXDialog)) return;

    final BaseXDialog d = (BaseXDialog) win;
    addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent ie) {
        if(isValid() && ie.getStateChange() == ItemEvent.SELECTED) d.action(ie.getSource());
      }
    });
  }

  @Override
  public String getSelectedItem() {
    final Object o = super.getSelectedItem();
    return o == null ? null : o.toString();
  }

  @Override
  public void setSelectedItem(final Object object) {
    final ComboBoxModel m = getModel();
    final int s = m.getSize();
    for(int i = 0; i < s; i++) {
      if(m.getElementAt(i).equals(object)) {
        super.setSelectedItem(object);
        return;
      }
    }
  }
}
