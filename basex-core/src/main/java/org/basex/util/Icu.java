package org.basex.util;

import org.basex.util.list.*;

import com.ibm.icu.text.*;

/**
 * ICU functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Icu {
  /** Private constructor. */
  private Icu() { }

  /**
   * Splits a string into graphemes.
   * @param list tokens
   * @param string string
   */
  public static void split(final TokenList list, final String string) {
    final BreakIterator bi = BreakIterator.getCharacterInstance();
    bi.setText(string);
    for(int s = bi.first(), e = bi.next(); e != BreakIterator.DONE; s = e, e = bi.next()) {
      list.add(string.substring(s, e));
    }
  }
}
