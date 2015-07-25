package org.basex.io.parse.json;

/**
 * Fallback function for invalid XML characters.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class JsonFallback {
  /**
   * Converts the specified token.
   * @param string input string
   * @return converted token
   */
  public abstract String convert(final String string);
}
