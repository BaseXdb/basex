package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.basex.gui.*;
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
  /** Attached text area to search in. */
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
        if(area != null) {
          selectAll();
          find();
        }
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
        // attached text area
        if(area == null) return;
        if(ESCAPE.is(e) || ENTER.is(e) && getText().trim().isEmpty()) {
          area.requestFocusInWindow();
          setBackground(GUIConstants.WHITE);
        } else if(FINDPREV.is(e) || FINDPREV2.is(e) || ENTER.is(e) && e.isShiftDown()) {
          area.find(false);
        } else if(FINDNEXT.is(e) || FINDNEXT2.is(e) || ENTER.is(e)) {
          area.find(true);
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if(area == null) return;
        if(!control(e) && Character.isDefined(e.getKeyChar()) && !ENTER.is(e)) {
          final String text = getText().trim().toLowerCase(Locale.ENGLISH);
          final String old = area.keyword(text);
          if(text.equals(old)) return;
          find();
        }
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
   * Finds the specified keyword in the attached editor.
   */
  void find() {
    setBackground(area.find() || getText().trim().isEmpty() ?
        GUIConstants.WHITE : GUIConstants.LRED);
  }

  /**
   * Activates search functionality to the text field.
   * @param a text area to search
   */
  public final void setSearch(final BaseXEditor a) {
    area = a;
    BaseXLayout.setWidth(this, 100);
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }
}
