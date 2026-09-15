package org.basex.build.json;

import org.basex.query.value.seq.*;
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

  /**
   * Default constructor.
   */
  public JsonSerialOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public JsonSerialOptions(final JsonSerialOptions opts) {
    super(opts);
  }

  /**
   * Returns a copy of the options for serializing items converted by the w3-mapping format.
   * @return options
   */
  public JsonSerialOptions withoutMapping() {
    final JsonSerialOptions opts = new JsonSerialOptions(this);
    opts.set(FORMAT, JsonFormat.DIRECT);
    opts.set(MAPPING, Empty.VALUE);
    return opts;
  }
}
