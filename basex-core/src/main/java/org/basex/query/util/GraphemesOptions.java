package org.basex.query.util;

import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

import com.ibm.icu.text.*;

/**
 * Graphemes.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class GraphemesOptions extends Options {
  /** Availability of ICU. */
  public static final boolean AVAILABLE = Reflect.available("com.ibm.icu.text.BreakIterator");

  /** Option. */
  public static final StringOption CLUSTERS = new StringOption("clusters", "extended");

  /**
   * Splits a string into graphemes.
   * @param string string
   * @return graphemes
   */
  public byte[][] split(final String string) {
    final TokenList list = new TokenList();
    final BreakIterator bi = BreakIterator.getCharacterInstance();
    bi.setText(string);
    for(int s = bi.first(), e = bi.next(); e != BreakIterator.DONE; s = e, e = bi.next()) {
      list.add(string.substring(s, e));
    }
    return list.finish();
  }
}
