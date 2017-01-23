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

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogBindings(final GUI main) {
    super(main, EXTERNAL_VARIABLES);

    final BaseXBack table = new BaseXBack(new TableLayout(MAX + 1, 2, 8, 4));
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
    boolean empty = false;
    for(final Entry<String, String> entry : opts.toMap(MainOptions.BINDINGS).entrySet()) {
      String name = entry.getKey();
      if(name.isEmpty()) {
        empty = true;
        name = ".";
      } else {
        name = '$' + name;
      }
      names[c].setText(name);
      values[c].setText(entry.getValue());
      if(++c == MAX) break;
    }
    for(; c < MAX; c++) {
      names[c].setText(empty || c < MAX - 1 ? "$" : ".");
      values[c].setText("");
    }
  }

  @Override
  public void close() {
    if(!ok) return;

    super.close();
    final StringBuilder bind = new StringBuilder();
    for(int c = 0; c < MAX; c++) {
      String name = names[c].getText().trim();
      if(name.isEmpty() || name.equals("$")) continue;
      if(name.startsWith("$")) {
        name = name.substring(1);
      } else if(name.equals(".")) {
        name = "";
      }
      final String value = values[c].getText();
      if(bind.length() != 0) bind.append(',');
      bind.append((name + '=' + value).replaceAll(",", ",,"));
    }
    gui.set(MainOptions.BINDINGS, bind.toString());
  }
}
