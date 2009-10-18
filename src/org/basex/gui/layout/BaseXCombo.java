package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JComboBox;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific ComboBox implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXCombo extends JComboBox {
  /**
   * Constructor.
   * @param ch combobox choices.
   * @param hlp help text
   * @param win parent window
   */
  public BaseXCombo(final String[] ch, final byte[] hlp, final Window win) {
    super(ch);
    BaseXLayout.addInteraction(this, hlp, win);

    if(!(win instanceof Dialog)) return;

    addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent ie) {
        if(isValid() && ie.getStateChange() == ItemEvent.SELECTED) {
          ((Dialog) win).action(null);
        }
      }
    });
  }
  
  /**
   * Constructor.
   * @param ch combobox choices.
   * @param hlp help text
   * @param win parent window
   */
  public BaseXCombo(final Vector<String> ch,
      final byte[] hlp, final Window win) {
    super(ch);
    BaseXLayout.addInteraction(this, hlp, win);

    if(!(win instanceof Dialog)) return;

    addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent ie) {
        if(isValid() && ie.getStateChange() == ItemEvent.SELECTED) {
          ((Dialog) win).action(null);
        }
      }
    });
  }
}
