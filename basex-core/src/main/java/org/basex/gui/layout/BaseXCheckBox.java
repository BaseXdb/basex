package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;

import org.basex.util.options.*;

/**
 * Project specific check box implementation.
 *
 * @author BaseX Team 2005-17, BSD License
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
  public BaseXCheckBox(final Window win, final String label, final BooleanOption option,
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
  public BaseXCheckBox(final Window win, final String label, final boolean selected) {
    super(label, selected);
    setOpaque(false);
    setMargin(new Insets(0, 0, 0, 0));

    BaseXLayout.addInteraction(this, win);
    if(!(win instanceof BaseXDialog)) return;

    final BaseXDialog dialog = (BaseXDialog) win;
    BaseXLayout.setMnemonic(this, dialog.mnem);
    addActionListener(e -> dialog.action(e.getSource()));
  }

  /**
   * Chooses a bold font.
   * @return self reference
   */
  public BaseXCheckBox bold() {
    setFont(getFont().deriveFont(Font.BOLD));
    return this;
  }

  /**
   * Chooses a large font.
   * @return self reference
   */
  public BaseXCheckBox large() {
    final Font f = getFont();
    setFont(new Font(f.getName(), Font.BOLD, (int) (f.getSize2D() * 1.4)));
    return this;
  }

  /**
   * Assigns the current checkbox value to the option specified in the constructor.
   */
  public void assign() {
    options.set(option, isSelected());
  }
}
