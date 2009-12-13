package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific ComboBox implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXCombo extends JComboBox {
  /** Default width of combo boxes. */
  public static final int TWIDTH = 80;

  /**
   * Constructor.
   * @param ch combobox choices
   * @param win parent window
   */
  public BaseXCombo(final String[] ch, final Window win) {
    super(ch);
    BaseXLayout.addInteraction(this, null, win);

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
   * @param listen boolean for Itemlistener
   * @param ch combobox choices
   * @param win parent window
   */
  public BaseXCombo(final boolean listen, final String[] ch, final Window win) {
    super(ch);
    BaseXLayout.addInteraction(this, null, win);

    if(!(win instanceof Dialog)) return;

    addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent ie) {
        if(isValid() && ie.getStateChange() == ItemEvent.SELECTED) {
          if(listen) ((Dialog) win).action(ie.getSource());
        }
      }
    });
  }
}
