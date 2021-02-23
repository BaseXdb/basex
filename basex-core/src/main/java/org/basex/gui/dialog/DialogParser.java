package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Parser options panel.
 *
 * @author BaseX Team 2005-21, BSD License
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
  abstract boolean action(boolean active);

  /**
   * Updates the chosen parser options.
   */
  abstract void update();

  /**
   * Sets the global parser options.
   * @param gui gui reference
   */
  abstract void setOptions(GUI gui);

  /**
   * Builds a parsing example string.
   * @param format format
   * @param input input string
   * @param output output string
   * @return example string
   */
  static String example(final String format, final String input, final String output) {
    final TokenBuilder text = new TokenBuilder();
    text.bold().add(format).add(COL).norm().nline().add(input).nline().nline();
    return text.bold().add("XML").add(COL).norm().nline().add(output).toString();
  }

  /**
   * Builds an error string.
   * @param ex I/O exception
   * @return error string
   */
  static String error(final IOException ex) {
    final TokenBuilder text = new TokenBuilder().bold().add(Text.ERROR).add(COL).norm().nline();
    return text.add(ex.getLocalizedMessage()).toString();
  }

  /**
   * Creates an encoding combo box and selects the specified encoding.
   * @param dialog dialog reference
   * @param encoding original encoding
   * @return combo box
   */
  static BaseXCombo encoding(final BaseXDialog dialog, final String encoding) {
    final String[] encodings = Strings.encodings();
    final BaseXCombo cb = new BaseXCombo(dialog, encodings);
    boolean f = false;
    String enc = encoding == null ? Strings.UTF8 : encoding;
    for(final String s : encodings) f |= s.equals(enc);
    if(!f) {
      enc = enc.toUpperCase(Locale.ENGLISH);
      for(final String s : encodings) f |= s.equals(enc);
    }
    if(!f) enc = Strings.UTF8;
    cb.setSelectedItem(enc);
    return cb;
  }
}
