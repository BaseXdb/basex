package org.basex.query.util.format;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting dates.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DateParser extends TokenParser {
  /** Input information. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info
   * @param picture picture
   */
  DateParser(final InputInfo info, final byte[] picture) {
    super(picture);
    this.info = info;
  }

  /**
   * Returns the next literal and advances the cursor.
   * @return current literal, or {@code -1}
   * @throws QueryException query exception
   */
  int literal() throws QueryException {
    final int ch = next();
    if(ch == '[') { // check begin of variable marker
      if(!more()) throw PICDATE_X.get(info, token); // [$
      if(!consume('[')) return -1; // [...
    } else if(ch == ']') { // check end of variable marker
      if(!consume(']')) throw PICDATE_X.get(info, token); // ]$ or ]...
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
      final int ch = next();
      if(ch == ']') return tb.finish();
      if(!Character.isWhitespace(ch)) tb.add(ch);
    }
    throw PICDATE_X.get(info, token);
  }
}
