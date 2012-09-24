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
  /** Last input. */
  String last = "";

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

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }
}
