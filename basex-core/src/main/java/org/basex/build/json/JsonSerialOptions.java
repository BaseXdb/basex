package org.basex.build.json;

import org.basex.util.options.*;

/**
 * Options for serializing JSON documents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialOptions extends JsonOptions {
  /** Option: merge type information (xml-to-json, custom). */
  public static final BooleanOption INDENT = new BooleanOption("indent", false);
  /** Option: escape special characters (custom). */
  public static final BooleanOption ESCAPE = new BooleanOption("escape", true);
}
