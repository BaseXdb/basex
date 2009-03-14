package org.basex.gui.layout;

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
   * Default Constructor.
   * @param label button title
   * @param hlp help text
   * @param sel initial selection state
   * @param list reference to the dialog listener
   */
  public BaseXRadio(final String label, final byte[] hlp,
      final boolean sel, final Dialog list) {

    super(label, sel);
    setOpaque(false);
    setBorder(new EmptyBorder(0, 0, 0, 16));
    BaseXLayout.addHelp(this, hlp);
    BaseXLayout.addDefaultKeys(this, list);

    addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        list.action(getText());
      }
    });
  }
}
