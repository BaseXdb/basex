package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.gui.*;

/**
 * Project specific button implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BaseXButton extends JButton {
  /**
   * Constructor for text buttons.
   * @param l button title
   * @param win parent window
   */
  public BaseXButton(final String l, final Window win) {
    super(l);
    BaseXLayout.addInteraction(this, win);
    if(!(win instanceof BaseXDialog)) return;

    final BaseXDialog d = (BaseXDialog) win;
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String text = getText();
        if(text.equals(B_CANCEL)) d.cancel();
        else if(text.equals(B_OK)) d.close();
        else d.action(e.getSource());
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ESCAPE.is(e)) d.cancel();
      }
    });
    BaseXLayout.setMnemonic(this, d.mnem);
  }

  /**
   * Constructor for image buttons.
   * @param gui main window
   * @param img image reference
   * @param hlp help text
   */
  public BaseXButton(final Window gui, final String img, final String hlp) {
    super(BaseXLayout.icon(img));
    BaseXLayout.addInteraction(this, gui);
    if(hlp != null) setToolTipText(hlp);
    setOpaque(false);

    // trim horizontal button margins
    final Insets in = getMargin();
    in.left /= 4;
    in.right /= 4;
    if(in.top < in.left) setMargin(in);
  }

  /**
   * Sets the label borders.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return self reference
   */
  public BaseXButton border(final int t, final int l, final int b, final int r) {
    setBorder(new EmptyBorder(t, l, b, r));
    return this;
  }

  /**
   * Creates a new image button for the specified command.
   * @param cmd command
   * @param gui reference to main window
   * @return button
   */
  public static BaseXButton command(final GUICommand cmd, final GUI gui) {
    final BaseXButton button = new BaseXButton(gui,
        cmd.toString().toLowerCase(Locale.ENGLISH), cmd.help());
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    return button;
  }

  @Override
  public void setEnabled(final boolean flag) {
    if(flag != isEnabled()) super.setEnabled(flag);
  }
}
