package org.basex.query.util.format;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;

/**
 * Parser for formatting dates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DateParser {
  /** Calling expression. */
  private final ParseExpr expr;
  /** String. */
  private final String pic;
  /** Position. */
  private int pos;

  /**
   * Constructor.
   * @param e calling expression
   * @param p picture
   */
  DateParser(final ParseExpr e, final String p) {
    expr = e;
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
      if(!more()) expr.error(PICDATE, pic);
      if(pic.charAt(pos) != ch) {
        if(ch == ']') expr.error(PICDATE, pic);
        return 0;
      }
      pos++;
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
      if(!more()) expr.error(PICDATE, pic);
    final StringBuilder sb = new StringBuilder();
    for(; p < pos - 1; p++) {
      final char ch = pic.charAt(p);
      if(!Character.isWhitespace(ch)) sb.append(ch);
    }
    return sb.toString();
  }
}
