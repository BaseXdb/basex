package org.basex.io.parse.json;

/**
 * Fallback function for invalid XML characters.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface JsonFallback {
  /**
   * Converts the specified token.
   * @param string input string
   * @return converted token
   */
  String convert(String string);
}
