package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * CSV parser panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class DialogTextParser extends DialogParser {
  /** Options. */
  private TextOptions topts;
  /** Text: encoding. */
  private final BaseXCombo encoding;
  /** Text: Use lines. */
  private final BaseXCheckBox lines;

  /**
   * Constructor.
   * @param d dialog reference
   * @param opts main options
   */
  DialogTextParser(final BaseXDialog d, final MainOptions opts) {
    try {
      topts = new TextOptions(opts.get(MainOptions.TEXTPARSER));
    } catch(final IOException ex) { topts = new TextOptions(); }

    BaseXBack pp  = new BaseXBack(new TableLayout(2, 1, 0, 8));

    encoding = DialogExport.encoding(d, topts.get(TextOptions.ENCODING));
    lines = new BaseXCheckBox(SPLIT_INPUT_LINES, topts.get(TextOptions.LINES), 0, d);

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(encoding);
    pp.add(p);
    pp.add(lines);

    add(pp, BorderLayout.WEST);
  }

  @Override
  boolean action(final boolean active) {
    return true;
  }

  @Override
  void update() {
    topts.set(TextOptions.ENCODING, encoding.getSelectedItem());
    topts.set(TextOptions.LINES, lines.isSelected());
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.TEXTPARSER, topts.toString());
  }
}
