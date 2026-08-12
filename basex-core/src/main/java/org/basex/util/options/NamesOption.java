package org.basex.util.options;

import java.util.regex.*;

/**
 * Option containing a list of names, from which whitespace is removed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NamesOption extends StringOption {
  /** Whitespace pattern. */
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public NamesOption(final String name, final String value) {
    super(name, strip(value));
  }

  @Override
  Object normalize(final Object value) {
    return strip((String) value);
  }

  /**
   * Removes whitespace from a value.
   * @param value value
   * @return normalized value
   */
  private static String strip(final String value) {
    return WHITESPACE.matcher(value).replaceAll("");
  }
}
