package org.basex.build;

import org.basex.util.options.*;

/**
 * Options for parsing JSON documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class JsonParserOptions extends JsonOptions {
  /** Option: encoding. */
  public static final StringOption ENCODING = new StringOption("encoding");
  /** Option: unescape special characters. */
  public static final BooleanOption UNESCAPE = new BooleanOption("unescape", true);

  /**
   * Default constructor.
   */
  public JsonParserOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public JsonParserOptions(final JsonParserOptions opts) {
    super(opts);
  }
}
