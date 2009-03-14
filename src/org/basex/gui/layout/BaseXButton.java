package org.basex.gui.layout;

import static org.basex.Text.*;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Token;

/**
 * Project specific button implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXButton extends JButton {
  /**
   * Default Constructor.
   * @param l button title
   * @param hlp help text
   */
  public BaseXButton(final String l, final byte[] hlp) {
    this(l, hlp, null);
  }

  /**
   * Default Constructor.
   * @param l button title
   * @param hlp help text
   * @param d dialog window reference
   */
  public BaseXButton(final String l, final byte[] hlp, final Dialog d) {
    super(l);
    setOpaque(false);
    BaseXLayout.addHelp(this, hlp);
    if(d == null) return;
      
    addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final String text = getText();
        if(text.equals(BUTTONCANCEL)) d.cancel();
        else if(text.equals(BUTTONOK)) d.close();
        else d.action(text);
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) d.cancel();
      }
    });
  }

  /**
   * Default Constructor.
   * @param image image reference
   * @param hlp help text
   */
  public BaseXButton(final ImageIcon image, final byte[] hlp) {
    super(image);
    setOpaque(false);
    BaseXLayout.addHelp(this, hlp);
    if(hlp != null) setToolTipText(Token.string(hlp));
  }

  /**
   * Trims the horizontal button margins.
   */
  public void trim() {
    final Insets in = getMargin();
    in.left /= 4;
    in.right /= 4;
    if(in.top < in.left) setMargin(in);
  }
}
