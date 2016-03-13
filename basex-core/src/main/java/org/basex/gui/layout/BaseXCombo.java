package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.util.options.*;

/**
 * Project specific ComboBox implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class BaseXCombo extends JComboBox<String> {
  /** Options. */
  private Options options;
  /** Option. */
  private Option<?> option;

  /**
   * Constructor.
   * @param ch combobox choices
   * @param opt option
   * @param opts options
   * @param win parent window
   */
  public BaseXCombo(final Window win, final NumberOption opt, final Options opts,
      final String... ch) {
    this(win, (Option<?>) opt, opts, ch);
    setSelectedIndex(opts.get(opt));
  }

  /**
   * Constructor.
   * @param ch combobox choices
   * @param opt option
   * @param opts options
   * @param win parent window
   */
  private BaseXCombo(final Window win, final Option<?> opt, final Options opts,
      final String... ch) {
    this(win, ch);
    options = opts;
    option = opt;
  }

  /**
   * Constructor.
   * @param ch combobox choices
   * @param win parent window
   */
  public BaseXCombo(final Window win, final String... ch) {
    super(ch);
    BaseXLayout.addInteraction(this, win);
    if(!(win instanceof BaseXDialog)) return;

    final BaseXDialog d = (BaseXDialog) win;
    addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent ie) {
        if(isValid() && ie.getStateChange() == ItemEvent.SELECTED) d.action(ie.getSource());
      }
    });
  }

  @Override
  public String getSelectedItem() {
    final Object o = super.getSelectedItem();
    return o == null ? null : o.toString();
  }

  @Override
  public void setSelectedItem(final Object object) {
    if(object == null) return;
    final String value = object.toString();
    final ComboBoxModel<String> m = getModel();
    final int s = m.getSize();
    for(int i = 0; i < s; i++) {
      if(m.getElementAt(i).equals(value)) {
        super.setSelectedItem(value);
        return;
      }
    }
  }

  /**
   * Assigns the current checkbox value to the option specified in the constructor.
   */
  public void assign() {
    if(option instanceof NumberOption) {
      options.set((NumberOption) option, getSelectedIndex());
    } else {
      options.set((StringOption) option, getSelectedItem());
    }
  }
}
