package org.basex.gui.layout;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import org.basex.gui.GUI;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific Textfield implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BaseXTextField extends JTextField {
  /** Last Input. */
  String last = "";
  /** Button help. */
  byte[] help;

  /**
   * Default Constructor.
   * @param hlp help text
   */
  public BaseXTextField(final byte[] hlp) {
    this(null, hlp, null);
  }

  /**
   * Default Constructor.
   * @param hlp help text
   * @param list listener
   */
  public BaseXTextField(final byte[] hlp, final Dialog list) {
    this(null, hlp, list);
  }

  /**
   * Default Constructor.
   * @param txt input text
   * @param hlp help text
   * @param list reference to the dialog listener
   */
  public BaseXTextField(final String txt, final byte[] hlp, final Dialog list) {
    BaseXLayout.addDefaultKeys(this, list);
    BaseXLayout.setWidth(this, 200);
    help = hlp;

    if(txt != null) {
      setText(txt);
      selectAll();
    }
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        GUI.get().focus(e.getComponent(), help);
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        final int key = e.getKeyCode();
        final boolean ctrl = e.isControlDown();
        if(ctrl && (key == KeyEvent.VK_Z || key == KeyEvent.VK_Y)) {
          final String t = getText();
          setText(last);
          last = t;
        }
      }
    });
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }

  @Override
  public final void setEnabled(final boolean sel) {
    super.setEnabled(sel);
    setOpaque(sel);
  }

  /**
   * Sets the text field help text.
   * @param hlp help text
   */
  public final void help(final byte[] hlp) {
    help = hlp;
  }
}
