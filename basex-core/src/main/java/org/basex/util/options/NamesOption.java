package org.basex.util.options;

import java.util.*;
import java.util.regex.*;

/**
 * Option containing a list of names, separated by commas or whitespace.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NamesOption extends StringOption {
  /** Separators outside braced URIs. */
  private static final Pattern SEPARATORS = Pattern.compile("[,\\s]+(?![^{]*})");

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public NamesOption(final String name, final String value) {
    super(name, String.join(",", split(value)));
  }

  @Override
  Object normalize(final Object value) {
    return String.join(",", split((String) value));
  }

  /**
   * Splits a list of names into entries.
   * @param value value
   * @return entries
   */
  public static List<String> split(final String value) {
    return Arrays.stream(SEPARATORS.split(value)).filter(s -> !s.isEmpty()).toList();
  }
}
