package org.basex.build.json;

import org.basex.util.options.*;

/**
 * Options for serializing JSON documents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialOptions extends JsonOptions {
  /** Option: indentation. */
  public static final BooleanOption INDENT = new BooleanOption("indent");
  /** Option: escape special characters (custom). */
  public static final BooleanOption ESCAPE = new BooleanOption("escape", true);
  /** Option: escape solidus (custom). */
  public static final BooleanOption ESCAPE_SOLIDUS = new BooleanOption("escape-solidus", true);
}
