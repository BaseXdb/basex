package org.basex.gui.layout;

import static org.basex.Text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific button implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    BaseXLayout.addHelp(this, hlp);
    setOpaque(false);
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
    setFocusable(false);
    BaseXLayout.addHelp(this, hlp);
  }
}
