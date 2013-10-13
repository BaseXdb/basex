package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Parser options panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
abstract class DialogParser extends BaseXBack {
  /**
   * Constructor.
   */
  DialogParser() {
    setLayout(new BorderLayout(16, 0));
  }

  /**
   * Reacts on user input.
   * @param active indicates if this panel is currently active
   * @return result of check
   */
  abstract boolean action(final boolean active);

  /**
   * Sets parser options.
   */
  abstract void update();

  /**
   * Finalizes the option parsing.
   * @param gui gui reference
   */
  abstract void setOptions(final GUI gui);

  /**
   * Builds a parsing example.
   * @param format format
   * @param input input string
   * @param output output string
   * @return example string
   */
  static final String example(final String format, final String input, final String output) {
    final TokenBuilder text = new TokenBuilder();
    text.bold().add(format).add(COL).norm().nline().add(input).nline().nline();
    return text.bold().add("XML").add(COL).norm().nline().add(output).toString();
  }

  /**
   * Builds an error string.
   * @param ex I/O exception
   * @return error string
   */
  static final String error(final IOException ex) {
    final TokenBuilder text = new TokenBuilder().bold().add(Text.ERROR).add(COL).norm().nline();
    return text.add(ex.getLocalizedMessage()).toString();
  }
}
