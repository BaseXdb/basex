package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.build.text.*;
import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * CSV parser panel.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DialogTextParser extends DialogParser {
  /** Options. */
  private final TextOptions topts;
  /** Text: encoding. */
  private final BaseXCombo encoding;
  /** Text: Use lines. */
  private final BaseXCheckBox lines;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param opts main options
   */
  DialogTextParser(final BaseXDialog dialog, final MainOptions opts) {
    topts = new TextOptions(opts.get(MainOptions.TEXTPARSER));

    final BaseXBack pp  = new BaseXBack(new RowLayout(8));

    encoding = encoding(dialog, topts.get(TextOptions.ENCODING));
    lines = new BaseXCheckBox(dialog, SPLIT_INPUT_LINES, TextOptions.LINES, topts);

    final BaseXBack p = new BaseXBack(new ColumnLayout(8));
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
    final String enc = encoding.getSelectedItem();
    topts.set(TextOptions.ENCODING, enc.equals(Strings.UTF8) ? null : enc);
    topts.set(TextOptions.LINES, lines.isSelected());
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.TEXTPARSER, topts);
  }
}
