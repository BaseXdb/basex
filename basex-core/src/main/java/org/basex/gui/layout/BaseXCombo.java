package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Project specific ComboBox implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class BaseXCombo extends JComboBox<Object> {
  /** Options. */
  private Options options;
  /** Option. */
  private Option<?> option;

  /**
   * Constructor.
   * @param option option
   * @param options options
   * @param values combobox values
   * @param win parent window
   */
  public BaseXCombo(final NumberOption option, final Options options, final String[] values,
      final Window win) {
    this(option, options, values, values[options.get(option)], win);
  }

  /**
   * Constructor.
   * @param option option
   * @param options options
   * @param win parent window
   */
  public BaseXCombo(final BooleanOption option, final Options options, final Window win) {
    this(option, options, new String[] { "true", "false" }, options.get(option), win);
  }

  /**
   * Constructor.
   * @param option option
   * @param options options
   * @param win parent window
   */
  public BaseXCombo(final EnumOption<?> option, final Options options, final Window win) {
    this(option, options, option.strings(), options.get(option), win);
  }

  /**
   * Constructor.
   * @param option option
   * @param options options
   * @param values values
   * @param selected selected value
   * @param win parent window
   */
  private BaseXCombo(final Option<?> option, final Options options, final String[] values,
      final Object selected, final Window win) {
    this(values, win);
    this.options = options;
    this.option = option;
    setSelectedItem(selected.toString());
  }

  /**
   * Constructor.
   * @param win parent window
   */
  public BaseXCombo(final Window win) {
    this(new String[] {}, win);
  }

  /**
   * Constructor.
   * @param values combobox values
   * @param win parent window
   */
  public BaseXCombo(final String[] values, final Window win) {
    super(values);
    BaseXLayout.addInteraction(this, win);
    if(!(win instanceof BaseXDialog)) return;

    final BaseXDialog d = (BaseXDialog) win;
    addItemListener(ie -> {
      if(isValid() && ie.getStateChange() == ItemEvent.SELECTED) d.action(ie.getSource());
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
    final ComboBoxModel<Object> m = getModel();
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
    } else if(option instanceof EnumOption) {
      options.set((EnumOption<?>) option, getSelectedItem());
    } else if(option instanceof StringOption) {
      options.set((StringOption) option, getSelectedItem());
    } else if(option instanceof BooleanOption) {
      options.set((BooleanOption) option, Boolean.parseBoolean(getSelectedItem()));
    } else {
      throw Util.notExpected("Option type not supported: " + option);
    }
  }
}
