package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

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
  /** History. */
  BaseXHistory history;
  /** Last input. */
  String last = "";
  /** History pointer. */
  int hist;

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

    if(txt != null) setText(txt);

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        selectAll();
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
      }
    });
    if(dialog != null) addKeyListener(dialog.keys);

    setDragEnabled(true);
    BaseXLayout.addDrop(this, new DropHandler() {
      @Override
      public void drop(final Object object) {
        setText(object.toString());
        if(dialog != null) dialog.action(BaseXTextField.this);
      }
    });
  }

  /**
   * Attaches a history.
   * @param gui gui reference
   * @param option option
   */
  public void history(final GUI gui, final Object[] option) {
    history = new BaseXHistory(gui, option);
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ENTER.is(e)) {
          store();
        } else if(NEXTLINE.is(e) || PREVLINE.is(e)) {
          final boolean next = NEXTLINE.is(e);
          final String[] qu = gui.gopts.strings(option);
          if(qu.length == 0) return;
          hist = next ? Math.min(qu.length - 1, hist + 1) : Math.max(0, hist - 1);
          setText(qu[hist]);
        }
      }
    });
  }

  /**
   * Stores the current history.
   */
  public void store() {
    if(history == null) return;
    history.store(getText());
    hist = 0;
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }
}
