package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Project specific CheckBox implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class BaseXCheckBox extends JCheckBox {
  /**
   * Checkbox.
   * @param txt checkbox text
   * @param sel initial selection state
   * @param win parent window
   */
  public BaseXCheckBox(final String txt, final boolean sel, final Window win) {
    this(txt, sel, 1, win);
  }

  /**
   * Constructor.
   * @param label checkbox text
   * @param sel initial selection state
   * @param dist distance to next component
   * @param win parent window
   */
  public BaseXCheckBox(final String label, final boolean sel, final int dist, final Window win) {
    super(label, sel);
    setOpaque(false);
    setMargin(new Insets(0, 0, dist, 0));
    if(dist == 0) setFont(getFont().deriveFont(Font.BOLD));
    BaseXLayout.addInteraction(this, win);
    if(!(win instanceof BaseXDialog)) return;

    final BaseXDialog dialog = (BaseXDialog) win;
    BaseXLayout.setMnemonic(this, dialog.mnem);
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        dialog.action(e.getSource());
      }
    });
  }

  /**
   * Chooses a large font.
   * @return self reference
   */
  public BaseXCheckBox large() {
    final Font f = getFont();
    setFont(new Font(f.getName(), Font.PLAIN, (int) f.getSize2D() + 4));
    return this;
  }
}
