package org.basex.build;

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
  public static final BooleanOption INDENT = new BooleanOption("indent");
}
