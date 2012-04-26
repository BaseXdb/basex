package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting dates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class DateParser {
  /** Input information. */
  private final InputInfo info;
  /** Picture string. */
  private final byte[] pic;
  /** Position. */
  private int pos;

  /**
   * Constructor.
   * @param ii input info
   * @param p picture
   */
  DateParser(final InputInfo ii, final byte[] p) {
    info = ii;
    pic = p;
  }

  /**
   * Returns true if more characters are found.
   * @return result of check
   */
  boolean more() {
    return pos < pic.length;
  }

  /**
   * Returns the next character, or {@code 0} if a marker was found.
   * @return character
   * @throws QueryException query exception
   */
  int next() throws QueryException {
    final int ch = cp(pic, pos);
    pos += cl(pic, pos);
    if(ch == '[' || ch == ']') {
      if(!more()) PICDATE.thrw(info, pic);
      if(cp(pic, pos) != ch) {
        if(ch == ']') PICDATE.thrw(info, pic);
        return 0;
      }
      pos += cl(pic, pos);
    }
    return ch;
  }

  /**
   * Returns the next marker.
   * @return marker or {@code null} reference
   * @throws QueryException query exception
   */
  byte[] marker() throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    while(more()) {
      final int ch = cp(pic, pos);
      pos += cl(pic, pos);
      if(ch == ']') return tb.finish();
      if(!Character.isWhitespace(ch)) tb.add(ch);
    }
    throw PICDATE.thrw(info, pic);
  }
}
