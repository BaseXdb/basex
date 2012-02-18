package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.layout.BaseXLayout.DropHandler;

/**
 * Project specific text field implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BaseXTextField extends JTextField {
  /** Default width of text fields. */
  public static final int DWIDTH = 350;
  /** Last input. */
  String last = "";
  /** Text area to search in. */
  BaseXEditor area;

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
    BaseXLayout.addInteraction(this, win);

    if(txt != null) {
      setText(txt);
      selectAll();
    }

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        if(area != null) selectAll();
      }
    });

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(UNDOSTEP.is(e) || REDOSTEP.is(e)) {
          final String t = getText();
          setText(last);
          last = t;
        }
        // check search area
        if(area == null) return;
        final String text = getText();
        final boolean enter = ENTER.is(e);
        if(ESCAPE.is(e) || enter && text.isEmpty()) {
          area.requestFocusInWindow();
        } else if(enter || FINDNEXT.is(e) || FINDPREV.is(e) ||
            FINDNEXT2.is(e) || FINDPREV2.is(e)) {
          area.find(text, FINDPREV.is(e) || FINDPREV2.is(e) || e.isShiftDown());
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if(area == null) return;
        final String text = getText();
        final char ch = e.getKeyChar();
        if(!control(e) && Character.isDefined(ch) && !ENTER.is(e))
          area.find(text, false);
        repaint();
      }
    });

    setDragEnabled(true);
    BaseXLayout.addDrop(this, new DropHandler() {
      @Override
      public void drop(final Object object) {
        replaceSelection(object.toString());
      }
    });
  }

  /**
   * Activates search functionality to the text field.
   * @param a text area to search
   */
  public final void setSearch(final BaseXEditor a) {
    area = a;
    BaseXLayout.setWidth(this, 120);
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }
}
