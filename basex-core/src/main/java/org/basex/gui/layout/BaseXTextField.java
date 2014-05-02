package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.util.options.*;

/**
 * Project specific text field implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class BaseXTextField extends JTextField {
  /** Default width of text fields. */
  public static final int DWIDTH = 350;

  /** Options. */
  private Options options;
  /** Option. */
  private Option<?> option;

  /** History. */
  private BaseXHistory history;
  /** Hint. */
  private BaseXTextHint hint;
  /** Last input. */
  private String last = "";
  /** History pointer. */
  private int hist;

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
   * @param opt option
   * @param opts options
   * @param dialog dialog window
   */
  public BaseXTextField(final NumberOption opt, final Options opts, final BaseXDialog dialog) {
    this((Option<?>) opt, opts, dialog);
  }

  /**
   * Constructor.
   * @param opt option
   * @param opts options
   * @param dialog dialog window
   */
  public BaseXTextField(final StringOption opt, final Options opts, final BaseXDialog dialog) {
    this((Option<?>) opt, opts, dialog);
  }

  /**
   * Constructor.
   * @param opt option
   * @param opts options
   * @param dialog dialog window
   */
  private BaseXTextField(final Option<?> opt, final Options opts, final BaseXDialog dialog) {
    this(opts.get(opt).toString(), dialog, dialog);
    options = opts;
    option = opt;
  }

  /**
   * Constructor.
   * @param text input text
   * @param dialog dialog window
   */
  public BaseXTextField(final String text, final BaseXDialog dialog) {
    this(text, dialog, dialog);
  }

  /**
   * Constructor.
   * @param text input text
   * @param win parent window
   * @param dialog dialog reference
   */
  private BaseXTextField(final String text, final Window win, final BaseXDialog dialog) {
    BaseXLayout.setWidth(this, DWIDTH);
    BaseXLayout.addInteraction(this, win);

    if(text != null) setText(text);

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
  }

  @Override
  public void setFont(final Font f) {
    super.setFont(f);
    if(hint != null) hint.setFont(f);
  }

  /**
   * Attaches a history.
   * @param so option
   * @param win windows reference
   */
  public void history(final StringsOption so, final Window win) {
    final GUI gui;
    final BaseXDialog dialog;
    if(win instanceof BaseXDialog) {
      dialog = (BaseXDialog) win;
      gui = dialog.gui;
    } else {
      dialog = null;
      gui = (GUI) win;
    }

    history = new BaseXHistory(gui, so);
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ENTER.is(e)) {
          store();
        } else if(NEXTLINE.is(e) || PREVLINE.is(e)) {
          final boolean next = NEXTLINE.is(e);
          final String[] qu = gui.gopts.get(so);
          if(qu.length == 0) return;
          hist = next ? Math.min(qu.length - 1, hist + 1) : Math.max(0, hist - 1);
          setText(qu[hist]);
          if(dialog != null) dialog.action(this);
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

  /**
   * Adds a hint to the text field.
   * @param label text of the hint
   */
  public void hint(final String label) {
    if(hint == null) {
      hint = new BaseXTextHint(label, this);
    } else {
      hint.setText(label);
    }
    setToolTipText(label.replaceAll("\\.\\.\\.$", ""));
  }

  @Override
  public void setText(final String txt) {
    last = txt;
    super.setText(txt);
  }

  /**
   * Assigns the current checkbox value to the option specified in the constructor.
   */
  public void assign() {
    if(option instanceof NumberOption) {
      try {
        options.set((NumberOption) option, Integer.parseInt(getText()));
      } catch(final NumberFormatException ignored) { }
    } else {
      options.set((StringOption) option, getText());
    }
  }
}
