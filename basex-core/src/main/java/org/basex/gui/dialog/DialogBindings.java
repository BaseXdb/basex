package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.io.*;

/**
 * Dialog window for defining variable and context bindings.

 * @author BaseX Team 2005-21, BSD License
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
   * @param gui reference to the main window
   */
  public DialogBindings(final GUI gui) {
    super(gui, EXTERNAL_VARIABLES);
    ((BorderLayout) panel.getLayout()).setHgap(4);

    final BaseXBack west = new BaseXBack(new GridLayout(MAX + 2, 1, 0, 4));
    west.add(new BaseXLabel(NAME + COLS, false, true));
    for(int c = 0; c < MAX; c++) {
      names[c] = new BaseXTextField(this);
      BaseXLayout.setWidth(names[c], 100);
      west.add(names[c]);
    }
    west.add(new BaseXLabel("Context item" + COLS));
    set(west, BorderLayout.WEST);

    final BaseXBack center = new BaseXBack(new GridLayout(MAX + 2, 1, 0, 4));
    center.add(new BaseXLabel(VALUE + COLS, false, true));
    for(int c = 0; c < MAX; c++) {
      values[c] = new BaseXTextField(this);
      BaseXLayout.setWidth(values[c], 250);
      center.add(values[c]);
    }

    final BaseXBack ctx = new BaseXBack().layout(new BorderLayout(8, 0));
    ctxitem = new BaseXTextField(this).hint(gui.editor.context());
    ctx.add(ctxitem, BorderLayout.CENTER);
    final BaseXButton browse = new BaseXButton(this, BROWSE_D);
    browse.addActionListener(e -> choose());
    ctx.add(browse, BorderLayout.EAST);
    center.add(ctx);

    set(center, BorderLayout.CENTER);

    set(okCancel(), BorderLayout.SOUTH);

    fill();
    ok = true;
    setResizable(true);
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

  /**
   * Chooses and assigns a context file and closes the dialog window.
   */
  private void choose() {
    final BaseXFileChooser fc = new BaseXFileChooser(gui, OPEN, gui.gopts.get(GUIOptions.WORKPATH));
    fc.filter(XML_DOCUMENTS, true, gui.gopts.xmlSuffixes());
    final IOFile file = fc.select(Mode.FOPEN);
    if(file == null) return;

    // close dialog, set context
    final GUI win = gui;
    close();
    win.editor.setContext(file);
  }

  @Override
  public void close() {
    final HashMap<String, String> map = new HashMap<>();
    for(int c = 0; c < MAX; c++) {
      final String name = names[c].getText().replaceAll("^\\s*\\$|\\s+$", "");
      if(!name.isEmpty()) map.put(name, values[c].getText());
    }
    final String value = ctxitem.getText();
    if(!value.isEmpty()) map.put("", value);
    assign(map, gui);

    gui.editor.refreshContextLabel();
    super.close();
  }

  /**
   * Assigns variable bindings.
   * @param map map with bindings
   * @param gui GUI reference
   */
  public static void assign(final Map<String, String> map, final GUI gui) {
    final StringBuilder sb = new StringBuilder();
    for(final Entry<String, String> entry : map.entrySet()) {
      final String name = entry.getKey(), value = entry.getValue();
      if(sb.length() != 0) sb.append(',');
      sb.append((name + '=' + value).replaceAll(",", ",,"));
    }
    gui.set(MainOptions.BINDINGS, sb.toString());
  }
}
