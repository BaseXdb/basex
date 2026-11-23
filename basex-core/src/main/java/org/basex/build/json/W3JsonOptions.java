package org.basex.build.json;

import java.util.*;

import org.basex.util.options.*;

/**
 * Options of the W3 JSON parsing functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class W3JsonOptions extends JsonParserOptions {
  /** Options that can be assigned by name (JSON Lines are not supported by the W3 functions). */
  private static final Map<String, Option<?>> DEFINITIONS = new TreeMap<>();

  static {
    for(final Option<?> option : new JsonParserOptions()) {
      if(option != JSON_LINES) DEFINITIONS.put(option.name(), option);
    }
  }

  /**
   * Returns options with the W3 format.
   * @return options
   */
  public static W3JsonOptions w3() {
    final W3JsonOptions options = new W3JsonOptions();
    options.set(FORMAT, JsonFormat.W3);
    return options;
  }

  /**
   * Returns options with the W3 XML format.
   * @return options
   */
  public static W3JsonOptions w3Xml() {
    final W3JsonOptions options = new W3JsonOptions();
    options.set(FORMAT, JsonFormat.W3_XML);
    return options;
  }

  @Override
  protected Map<String, Option<?>> definitions() {
    return DEFINITIONS;
  }
}
