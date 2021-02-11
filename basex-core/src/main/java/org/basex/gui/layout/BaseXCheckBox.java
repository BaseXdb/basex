package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;

import org.basex.util.options.*;

/**
 * Project specific check box implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXCheckBox extends JCheckBox {
  /** Options. */
  private Options options;
  /** Boolean option. */
  private BooleanOption option;

  /**
   * Checkbox.
   * @param win parent window
   * @param label checkbox text
   * @param option option
   * @param options options
   */
  public BaseXCheckBox(final BaseXWindow win, final String label, final BooleanOption option,
      final Options options) {
    this(win, label, options.get(option));
    this.options = options;
    this.option = option;
  }

  /**
   * Checkbox.
   * @param win parent window
   * @param label checkbox text
   * @param selected initial selection state
   */
  public BaseXCheckBox(final BaseXWindow win, final String label, final boolean selected) {
    super(label, selected);
    setOpaque(false);
    setMargin(new Insets(0, 0, 0, 0));

    BaseXLayout.addInteraction(this, win);
    final BaseXDialog dialog = win.dialog();
    if(dialog == null) return;

    BaseXLayout.setMnemonic(this, dialog.mnem);
    addActionListener(e -> dialog.action(e.getSource()));
  }

  /**
   * Chooses a bold font.
   * @return self reference
   */
  public BaseXCheckBox bold() {
    BaseXLayout.boldFont(this);
    return this;
  }

  /**
   * Chooses a large font.
   * @return self reference
   */
  public BaseXCheckBox large() {
    BaseXLayout.boldFont(this);
    BaseXLayout.resizeFont(this, 1.4f);
    return this;
  }

  /**
   * Assigns the current checkbox value to the option specified in the constructor.
   */
  public void assign() {
    options.set(option, isSelected());
  }
}
