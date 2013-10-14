package org.basex.build;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Options for serializing JSON documents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialOptions extends JsonOptions {
  /** Option: escape special characters. */
  public static final BooleanOption ESCAPE = new BooleanOption("escape", true);
  /** Option: indentation. */
  public static final BooleanOption INDENT = new BooleanOption("indent", true);
  /** Option: fallback function. */
  public static final StringOption FALLBACK = new StringOption("fallback");

  /**
   * Default constructor.
   */
  public JsonSerialOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws BaseXException database exception
   */
  public JsonSerialOptions(final String opts) throws BaseXException {
    super(opts);
  }
}
