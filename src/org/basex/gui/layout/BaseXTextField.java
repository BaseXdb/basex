package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;

/**
 * Project specific text field implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class BaseXTextField extends JTextField {
  /** Default width of text fields. */
  public static final int DWIDTH = 240;
  /** Last Input. */
  String last = "";
  /** Button help. */
  byte[] help;

  /**
   * Constructor.
   * @param win parent window
   */
  public BaseXTextField(final Window win) {
    this(null, win);
  }

  /**
   * Constructor.
   * @param txt input text
   * @param win parent window
   */
  public BaseXTextField(final String txt, final Window win) {
    BaseXLayout.setWidth(this, DWIDTH);
    BaseXLayout.addInteraction(this, null, win);

    if(txt != null) {
      setText(txt);
      selectAll();
    }
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        BaseXLayout.focus(e.getComponent(), help);
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(pressed(UNDO, e) || pressed(REDO, e)) {
          final String t = getText();
          setText(last);
          last = t;
        }
      }
    });
  }

  /**
   * Adds search functionality to the text field.
   * @param area text area to search
   */
  final void addSearch(final BaseXText area) {
    final Font f = getFont();
    setFont(f.deriveFont((float) f.getSize() + 2));
    BaseXLayout.setWidth(this, 80);

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        final String text = getText();
        final boolean enter = pressed(ENTER, e);
        if(pressed(ESCAPE, e) || enter && text.isEmpty()) {
          area.requestFocusInWindow();
        } else if(enter || pressed(FINDNEXT, e) || pressed(FINDPREV, e)) {
          area.find(text, pressed(FINDPREV, e) || e.isShiftDown());
        }
      }
      @Override
      public void keyReleased(final KeyEvent e) {
        final String text = getText();
        final char ch = e.getKeyChar();
        if(!ignoreTyped(e) && Character.isDefined(ch) && !pressed(ENTER, e))
          area.find(text, false);
        repaint();
      }
    });
    repaint();
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }

  /**
   * Sets the text field help text.
   * @param hlp help text
   */
  public final void help(final byte[] hlp) {
    help = hlp;
  }
}
