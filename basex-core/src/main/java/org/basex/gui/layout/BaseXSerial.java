package org.basex.gui.layout;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.gui.*;
import org.basex.gui.listener.*;
import org.basex.io.serial.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Input component for serialization parameters.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXSerial extends BaseXBack implements ActionListener {
  /** Width of text field. */
  private static final int COMPT = 150;

  /** Reference to dialog. */
  private final BaseXDialog dialog;
  /** Serialization parameters. */
  private SerializerOptions sopts;
  /** Keys. */
  private final BaseXCombo params;
  /** Options panel. */
  private final BaseXBack panel;
  /** Serialization string. */
  private final BaseXLabel info;

  /**
   * Constructor.
   * @param dialog dialog window
   * @param sopts serialization parameters
   */
  public BaseXSerial(final BaseXDialog dialog, final SerializerOptions sopts) {
    super(new RowLayout());

    add(new BaseXLabel(SERIALIZATION + COL, true, true));

    panel = new BaseXBack(new ColumnLayout(8));
    this.dialog = dialog;

    params = new BaseXCombo(dialog, sopts.names());
    panel.add(params);
    add(panel);

    info = new BaseXLabel(sopts + " ").border(4, 0, 8, 0);
    info.setForeground(GUIConstants.dgray);
    add(info);

    params.addActionListener(this);
    init(sopts);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    update(sopts.option(params.getSelectedItem()), sopts, panel);
  }

  /**
   * Returns the serialization parameters.
   * @return serialization parameters
   */
  public SerializerOptions options() {
    return sopts;
  }

  /**
   * Initializes the serialization info string.
   * @param so serialization parameters
   */
  public void init(final SerializerOptions so) {
    sopts = new SerializerOptions(so);
    actionPerformed(null);
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Revalidates the input components.
   * @param option changed option
   * @param options options
   * @param p panel
   */
  private void update(final Option<?> option, final Options options, final BaseXBack p) {
    while(p.getComponentCount() > 1) p.remove(1);

    final Component comp;
    if(option == SerializerOptions.ENCODING) {
      final BaseXCombo combo = new BaseXCombo(dialog, Strings.encodings());
      combo.setSelectedItem(options.get(option));
      combo.addActionListener(e -> {
        options.set((StringOption) option, combo.getSelectedItem());
        update();
      });
      comp = combo;
    } else if(option instanceof final StringOption opt) {
      comp = addInput(new BaseXTextField(dialog, opt, options));
    } else if(option instanceof final NumberOption opt) {
      comp = addInput(new BaseXTextField(dialog, opt, options));
    } else if(option instanceof final BooleanOption opt) {
      comp = addCombo(new BaseXCombo(dialog, opt, options));
    } else if(option instanceof final EnumOption opt) {
      comp = addCombo(new BaseXCombo(dialog, opt, options));
    } else if(option instanceof final OptionsOption opt) {
      comp = addOption(opt, options);
    } else {
      throw Util.notExpected("Unknown option type: " + option);
    }
    p.add(comp);
    p.revalidate();
    p.repaint();
  }

  /**
   * Adds a text field.
   * @param text text field
   * @return text field
   */
  private Component addInput(final BaseXTextField text) {
    text.addKeyListener((KeyReleasedListener) e -> {
      text.assign();
      update();
    });
    BaseXLayout.setWidth(text, COMPT);
    panel.add(text);
    return text;
  }

  /**
   * Adds a combobox.
   * @param combo combo box
   * @return combo box
   */
  private Component addCombo(final BaseXCombo combo) {
    combo.addActionListener(e -> {
      combo.assign();
      update();
    });
    panel.add(combo);
    return combo;
  }

  /**
   * Adds option components.
   * @param option option with embedded options
   * @param options parent options
   * @return panel
   */
  private Component addOption(final OptionsOption<?> option, final Options options) {
    final BaseXBack p = new BaseXBack(new ColumnLayout(8));
    final Options opts = options.get(option);
    final BaseXCombo combo = new BaseXCombo(dialog, opts.names());
    combo.addActionListener(e -> update(opts.option(combo.getSelectedItem()), opts, p));
    p.add(combo);
    return p;
  }

  /**
   * Updates the serialization info string.
   */
  private void update() {
    info.setText(sopts.toString());
  }
}
