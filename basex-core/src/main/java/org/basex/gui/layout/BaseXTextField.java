package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.listener.*;
import org.basex.util.options.*;

/**
 * Project specific text field implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class BaseXTextField extends JTextField {
  /** Default width of text fields. */
  public static final int DWIDTH = 450;
  /** Default foreground color. */
  private static Color back;

  /** Options. */
  private Options options;
  /** Option. */
  private Option<?> option;

  /** Hint. */
  private BaseXTextHint hint;
  /** Last input. */
  private String last = "";

  /**
   * Constructor.
   * @param gui reference to the main window
   */
  public BaseXTextField(final GUI gui) {
    this(gui, null);
  }

  /**
   * Constructor.
   * @param dialog dialog window
   */
  public BaseXTextField(final BaseXDialog dialog) {
    this(dialog, null);
  }

  /**
   * Constructor.
   * @param dialog dialog window
   * @param option option
   * @param options options
   */
  public BaseXTextField(final BaseXDialog dialog, final NumberOption option,
      final Options options) {
    this(dialog, (Option<?>) option, options);
  }

  /**
   * Constructor.
   * @param dialog dialog window
   * @param option option
   * @param options options
   */
  public BaseXTextField(final BaseXDialog dialog, final StringOption option,
      final Options options) {
    this(dialog, (Option<?>) option, options);
  }

  /**
   * Constructor.
   * @param dialog dialog window
   * @param option option
   * @param options options
   */
  private BaseXTextField(final BaseXDialog dialog, final Option<?> option, final Options options) {
    this(dialog, options.get(option) == null ? null : options.get(option).toString());
    this.options = options;
    this.option = option;
  }

  /**
   * Constructor.
   * @param win window (of type {@link BaseXDialog} or {@link GUI})
   * @param text input text (can be {@code null})
   */
  public BaseXTextField(final BaseXWindow win, final String text) {
    BaseXLayout.setWidth(this, DWIDTH);
    BaseXLayout.addInteraction(this, win);
    if(back == null) back = getBackground();

    if(text != null) setText(text);

    addFocusListener((FocusGainedListener) e -> selectAll());
    addKeyListener((KeyPressedListener) e -> {
      if(UNDOSTEP.is(e) || REDOSTEP.is(e)) {
        final String t = getText();
        setText(last);
        last = t;
      }
    });

    final BaseXDialog dialog = win.dialog();
    if(dialog != null) addKeyListener(dialog.keys);
  }

  @Override
  public void setFont(final Font f) {
    super.setFont(f);
    if(hint != null) hint.setFont(f);
  }

  /**
   * Adds a hint to the text field.
   * @param label text of the hint
   * @return self reference
   */
  public final BaseXTextField hint(final String label) {
    if(hint == null) {
      hint = new BaseXTextHint(label, this);
    } else {
      hint.setText(label);
    }
    setToolTipText(label.replaceAll("\\.\\.\\.$", ""));
    return this;
  }

  @Override
  public void setText(final String text) {
    last = text;
    super.setText(text);
    if(hint != null) hint.update();
  }

  /**
   * Assigns the current value.
   * @return success flag
   */
  public final boolean assign() {
    return check(true);
  }

  /**
   * Checks the current value.
   * @return success flag
   */
  public final boolean check() {
    return check(false);
  }

  /**
   * Checks and assigns the current value.
   * @param assign assign value
   * @return success flag
   */
  private boolean check(final boolean assign) {
    if(option instanceof NumberOption) {
      try {
        final int num = Integer.parseInt(getText());
        if(assign) options.set((NumberOption) option, num);
        setBackground(back);
      } catch(final NumberFormatException ignored) {
        setBackground(GUIConstants.LRED);
        return false;
      }
    } else {
      options.set((StringOption) option, getText());
    }
    return true;
  }
}
