package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for defining variable and context bindings.

 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class DialogBindings extends BaseXDialog {
  /** Maximum number of supported bindings. */
  private static final int MAX = 8;
  /** Names. */
  private final BaseXTextField[] names = new BaseXTextField[MAX];
  /** Values. */
  private final BaseXTextField[] values = new BaseXTextField[MAX];
  /** Context item. */
  private final BaseXTextField ctxitem;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogBindings(final GUI main) {
    super(main, EXTERNAL_VARIABLES);

    final BaseXBack table = new BaseXBack(new TableLayout(MAX + 2, 2, 8, 4));
    table.add(new BaseXLabel(NAME + COLS, false, true));
    table.add(new BaseXLabel(VALUE + COLS, false, true));
    for(int c = 0; c < MAX; c++) {
      names[c] = new BaseXTextField(this);
      BaseXLayout.setWidth(names[c], 80);
      table.add(names[c]);
      values[c] = new BaseXTextField(this);
      BaseXLayout.setWidth(values[c], 200);
      table.add(values[c]);
    }
    table.add(new BaseXLabel("Context item" + COLS));
    ctxitem = new BaseXTextField(this);
    BaseXLayout.setWidth(ctxitem, 200);
    table.add(ctxitem);

    set(table, BorderLayout.CENTER);
    set(okCancel(), BorderLayout.SOUTH);

    fill();
    ok = true;
    finish();
  }

  /**
   * Fills the text fields with the currently specified values.
   */
  private void fill() {
    final MainOptions opts = gui.context.options;
    int c = 0;
    for(final Entry<String, String> entry : opts.toMap(MainOptions.BINDINGS).entrySet()) {
      final String name = entry.getKey(), value = entry.getValue();
      if(name.isEmpty()) {
        ctxitem.setText(value);
      } else if(c < MAX) {
        names[c].setText('$' + name.replaceAll("^\\$", ""));
        values[c].setText(value);
        c++;
      }
    }
    for(; c < MAX; c++) {
      names[c].setText("$");
      values[c].setText("");
    }
  }

  @Override
  public void close() {
    if(!ok) return;

    super.close();
    final StringBuilder bind = new StringBuilder();
    for(int c = 0; c < MAX; c++) {
      final String name = names[c].getText().replaceAll("^\\s*\\$|\\s+$", "");
      if(!name.isEmpty())
        bind.append((name + '=' + values[c].getText()).replaceAll(",", ",,")).append(',');
    }
    final String value = ctxitem.getText();
    if(!value.isEmpty()) bind.append('=').append(value.replaceAll(",", ",,")).append(',');

    gui.set(MainOptions.BINDINGS, bind.toString().replaceAll(",$", ""));
  }
}
