package org.basex.gui.layout;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Token;

/**
 * Project specific CheckBox implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXCheckBox extends JCheckBox {
  /**
   * Default Constructor.
   * @param hlp help text
   * @param label button title
   * @param sel initial selection state
   * @param list reference to the dialog listener
   */
  public BaseXCheckBox(final String label, final byte[] hlp, final boolean sel,
      final Dialog list) {
    this(label, hlp, sel, 5, list);
  }

  /**
   * Default Constructor.
   * @param label button title
   * @param hlp help text
   * @param sel initial selection state
   * @param dist distance to next component
   * @param list reference to the dialog listener
   */
  public BaseXCheckBox(final String label, final byte[] hlp, final boolean sel,
      final int dist, final Dialog list) {

    super(label, sel);
    setMargin(new Insets(0, 0, dist, 0));
    setOpaque(false);
    BaseXLayout.addHelp(this, hlp);
    if(hlp != null) setToolTipText(Token.string(hlp));
    if(list == null) return;

    BaseXLayout.addDefaultKeys(this, list);

    addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        list.action(getText());
      }
    });
  }
}
