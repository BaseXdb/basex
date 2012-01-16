package org.basex.gui.layout;

import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific CheckBox implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXCheckBox extends JCheckBox {
  /**
   * Default constructor.
   * @param txt checkbox text
   * @param sel initial selection state
   * @param win parent window
   */
  public BaseXCheckBox(final String txt, final boolean sel, final Window win) {
    this(txt, sel, 1, win);
  }

  /**
   * Default constructor.
   * @param label button title
   * @param sel initial selection state
   * @param dist distance to next component
   * @param win parent window
   */
  public BaseXCheckBox(final String label, final boolean sel,
      final int dist, final Window win) {

    super(label, sel);
    setOpaque(false);
    setMargin(new Insets(0, 0, dist, 0));
    if(dist == 0) setFont(getFont().deriveFont(1));

    BaseXLayout.addInteraction(this, win);

    if(!(win instanceof Dialog)) return;
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        ((Dialog) win).action(e.getSource());
      }
    });
  }
}
