package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.BaseXLayout.DropHandler;
import org.basex.util.list.*;

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

    if(txt != null) {
      setText(txt);
      selectAll();
    }

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
   * @param gprop gui properties
   * @param option option
   */
  public void history(final GUIProp gprop, final Object[] option) {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ENTER.is(e)) {
          store(gprop, option);
        } else if(NEXTLINE.is(e)) {
          final String[] qu = gprop.strings(option);
          if(hist < qu.length) {
            setText(qu[++hist]);
          }
        } else if(PREVLINE.is(e)) {
          final String[] qu = gprop.strings(option);
          if(hist > 0) {
            setText(qu[++hist]);
          }
        }
      }
    });
  }

  /**
   * Stores the current history.
   * @param gprop gui properties
   * @param option option
   */
  void store(final GUIProp gprop, final Object[] option) {
    final StringList sl = new StringList();
    final String[] qu = gprop.strings(option);
    final String input = getText();
    sl.add(input);
    for(int q = 0; q < qu.length && q < 11; q++) {
      final String f = qu[q];
      if(!f.equals(input)) sl.add(f);
    }
    gprop.set(option, sl.toArray());
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }
}
