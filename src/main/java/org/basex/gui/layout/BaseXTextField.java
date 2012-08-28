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
   * @param gui main window
   */
  public BaseXTextField(final GUI gui) {
    this(null, gui, null);
  }

  /**
   * Constructor.
   * @param dialog dialog window
   */
  public BaseXTextField(final BaseXDialog dialog) {
    this(null, dialog, dialog);
  }

  /**
   * Constructor.
   * @param txt input text
   * @param dialog dialog window
   */
  public BaseXTextField(final String txt, final BaseXDialog dialog) {
    this(txt, dialog, dialog);
  }

  /**
   * Constructor.
   * @param txt input text
   * @param win parent window
   * @param dialog dialog reference
   */
  private BaseXTextField(final String txt, final Window win, final BaseXDialog dialog) {
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
          find(getText().trim());
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
        if(area != null) updateKeyword(true);
      }
    });
    if(dialog != null) addKeyListener(dialog.keys);

    setDragEnabled(true);
    BaseXLayout.addDrop(this, new DropHandler() {
      @Override
      public void drop(final Object object) {
        setText(object.toString());
        updateKeyword(true);
        if(dialog != null) dialog.action(BaseXTextField.this);
      }
    });
  }

  /**
   * Searches the current keyword if an editor is attached.
   * @param search automatically search keyword
   */
  void updateKeyword(final boolean search) {
    if(area == null) return;
    final String text = getText().trim().toLowerCase(Locale.ENGLISH);
    final String old = area.keyword(text);
    if(search && !text.equals(old)) find(text);
  }

  /**
   * Finds the specified keyword in the attached editor.
   * @param t current text
   */
  void find(final String t) {
    setBackground(area.find() || t.isEmpty() ? GUIConstants.WHITE : GUIConstants.LRED);
  }

  /**
   * Activates search functionality to the text field.
   * @param a text area to search
   */
  public final void setSearch(final BaseXEditor a) {
    area = a;
    updateKeyword(false);
    BaseXLayout.setWidth(this, 100);
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }
}
