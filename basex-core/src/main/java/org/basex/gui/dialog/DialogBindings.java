package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.query.*;

/**
 * Dialog window for defining variable and context bindings.

 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DialogBindings extends BaseXDialog {
  /** Dialog. */
  private static Dialog dialog;

  /** Variables. */
  private BaseXTextField[] context = new BaseXTextField[16];

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  private DialogBindings(final GUI main) {
    super(main, EXTERNAL_VARIABLES, false);

    final int cl = context.length;
    for(int c = 0; c < cl; c++) {
      context[c] = new BaseXTextField(this);
      BaseXLayout.setWidth(context[c], c % 2 == 0 ? 80 : 200);
    }

    final BaseXBack table = new BaseXBack(new TableLayout((2 + cl) / 2, 2, 8, 4));
    table.add(new BaseXLabel(NAME + COLS, false, true));
    table.add(new BaseXLabel(VALUE + COLS, false, true));
    for(int c = 0; c < cl; c++) table.add(context[c]);
    set(table, BorderLayout.CENTER);

    fill();
    finish(gui.gopts.get(GUIOptions.BINDINGSLOC));
  }

  /**
   * Activates the dialog window.
   * @param main reference to the main window
   */
  public static void show(final GUI main) {
    if(dialog == null) dialog = new DialogBindings(main);
    dialog.setVisible(true);
  }

  /**
   * Fills the text fields with the currently specified values.
   */
  private void fill() {
    final MainOptions opts = gui.context.options;
    final int cl = context.length;
    int c = 0;
    for(final Map.Entry<String, String> entry : QueryProcessor.bindings(opts).entrySet()) {
      context[c++].setText('$' + entry.getKey());
      context[c++].setText(entry.getValue());
      if(c == cl) break;
    }
    for(; c < cl; c += 2) context[c].setText("$");
  }

  @Override
  public void action(final Object cmp) {
    final StringBuilder bind = new StringBuilder();
    final int cl = context.length;
    for(int c = 0; c < cl; c += 2) {
      final String key = context[c].getText().replaceAll("^\\$", "");
      if(key.isEmpty()) continue;
      if(bind.length() != 0) bind.append(',');
      bind.append(key.replaceAll(",", ",,")).append('=');
      bind.append(context[c + 1].getText().replaceAll(",", ",,"));
    }
    gui.context.options.set(MainOptions.BINDINGS, bind.toString());
  }
}
