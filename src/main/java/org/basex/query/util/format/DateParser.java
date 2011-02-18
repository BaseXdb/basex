package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * Parser for formatting dates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class DateParser {
  /** Input information. */
  private final InputInfo input;
  /** String. */
  private final String pic;
  /** Position. */
  private int pos;

  /**
   * Constructor.
   * @param ii input info
   * @param p picture
   */
  DateParser(final InputInfo ii, final String p) {
    input = ii;
    pic = p;
  }

  /**
   * Returns true if more characters are found.
   * @return result of check
   */
  boolean more() {
    return pos < pic.length();
  }

  /**
   * Returns the next character, or {@code 0} if a marker was found.
   * @return character
   * @throws QueryException query exception
   */
  char next() throws QueryException {
    final char ch = pic.charAt(pos++);
    if(ch == '[' || ch == ']') {
      if(!more()) PICDATE.thrw(input, pic);
      if(pic.charAt(pos) != ch) {
        if(ch == ']') PICDATE.thrw(input, pic);
        return 0;
      }
      ++pos;
    }
    return ch;
  }

  /**
   * Returns the next marker.
   * @return marker or {@code null} reference
   * @throws QueryException query exception
   */
  String marker() throws QueryException {
    int p = pos;
    while(pic.charAt(pos++) != ']')
      if(!more()) PICDATE.thrw(input, pic);
    final StringBuilder sb = new StringBuilder();
    for(; p < pos - 1; ++p) {
      final char ch = pic.charAt(p);
      if(!Character.isWhitespace(ch)) sb.append(ch);
    }
    return sb.toString();
  }
}
