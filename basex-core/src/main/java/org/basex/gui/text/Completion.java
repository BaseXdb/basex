package org.basex.gui.text;

import java.util.*;

/**
 * Candidate for the code completion of a text.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param match string to be matched with the input (lower case)
 * @param label string to be displayed
 * @param value string to be inserted, in which an underscore indicates the new cursor position
 * @param alias alternative spelling of another candidate, only matched by its full name
 */
record Completion(String match, String label, String value, boolean alias) {
  /**
   * Returns a candidate that is displayed and inserted as it is.
   * @param string string to be displayed and inserted
   * @param alias alternative spelling of another candidate
   * @return candidate
   */
  static Completion get(final String string, final boolean alias) {
    return new Completion(string.toLowerCase(Locale.ENGLISH), string, string, alias);
  }
}
