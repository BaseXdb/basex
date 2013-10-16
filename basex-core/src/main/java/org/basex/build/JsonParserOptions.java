package org.basex.build;

import org.basex.util.options.*;

/**
 * Options for parsing JSON documents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class JsonParserOptions extends JsonOptions {
  /** Option: encoding. */
  public static final StringOption ENCODING = new StringOption("encoding");
  /** Option: unescape special characters. */
  public static final BooleanOption UNESCAPE = new BooleanOption("unescape", true);
  /** Option: merge type information. */
  public static final BooleanOption MERGE = new BooleanOption("merge", false);
  /** Option: include string types. */
  public static final BooleanOption STRINGS = new BooleanOption("strings", false);
}
