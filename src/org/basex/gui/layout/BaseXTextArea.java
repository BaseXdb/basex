package org.basex.gui.layout;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextArea;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Undo;

/**
 * Project specific TextArea implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXTextArea extends JTextArea {
  /** Undo history. */
  protected Undo undo;

  /**
   * Default Constructor.
   * @param hlp help text
   */
  public BaseXTextArea(final byte[] hlp) {
    this(null, hlp, null);
  }

  /**
   * Default Constructor.
   * @param txt input text
   * @param hlp help text
   * @param list reference to the dialog listener
   */
  public BaseXTextArea(final String txt, final byte[] hlp, final Dialog list) {
    setTabSize(2);
    setLineWrap(true);
    setWrapStyleWord(true);
    // restore default focus traversal with TAB key
    setFocusTraversalKeys(0, null);
    setFocusTraversalKeys(1, null);
    BaseXLayout.addDefaultKeys(this, list);
    undo = new Undo();

    if(txt != null) {
      setText(txt);
      selectAll();
    }

    BaseXLayout.addHelp(this, hlp);
    
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(!e.isControlDown()) return;

        final int key = e.getKeyCode();
        if(key == KeyEvent.VK_Z) {
          setText(undo.prev());
          setCaretPosition(undo.cursor());
        } else if(key == KeyEvent.VK_Y) {
          setText(undo.next());
          setCaretPosition(undo.cursor());
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        final int key = e.getKeyCode();
        if(e.isControlDown() && (key == KeyEvent.VK_Z || key == KeyEvent.VK_Y))
          return;

        undo.store(getText(), getCaretPosition());
      }
    });

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        repaint();
      }
      @Override
      public void focusLost(final FocusEvent e) {
        repaint();
      }
    });
  }
  
  @Override
  public void setText(final String t) {
    super.setText(t);
    undo.store(t, getCaretPosition());
  }
}
