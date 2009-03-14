package org.basex.gui.layout;

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
  /**
   * Default Constructor.
   */
  public BaseXCombo() {
    this(new String[] {}, null, null);
  }

  /**
   * Constructor.
   * @param choice combobox choice.
   * @param hlp help text
   */
  public BaseXCombo(final String[] choice, final byte[] hlp) {
    this(choice, hlp, null);
  }

  /**
   * Constructor.
   * @param ch combobox choices.
   * @param hlp help text
   * @param list action listener
   */
  public BaseXCombo(final String[] ch, final byte[] hlp, final Dialog list) {
    super(ch);
    BaseXLayout.addDefaultKeys(this, list);
    BaseXLayout.addHelp(this, hlp);
    
    if(list != null) {
      addItemListener(new ItemListener() {
        public void itemStateChanged(final ItemEvent ie) {
          if(ie.getStateChange() == ItemEvent.SELECTED) list.action(null);
        }
      });
    }
  }
}
