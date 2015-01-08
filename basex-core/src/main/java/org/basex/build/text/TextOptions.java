package org.basex.build.text;

import org.basex.util.options.*;

/**
 * Options for parsing and serializing text documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class TextOptions extends Options {
  /** Parser option: encoding. */
  public static final StringOption ENCODING = new StringOption("encoding");
  /** Parser option: line-wise parsing. */
  public static final BooleanOption LINES = new BooleanOption("lines", true);

  /**
   * Default constructor.
   */
  public TextOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public TextOptions(final Options opts) {
    super(opts);
  }
}
