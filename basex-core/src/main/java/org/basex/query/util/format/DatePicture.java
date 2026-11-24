package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Picture of a date formatting function, parsed in advance.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DatePicture {
  /** Literal strings before, between and after the variable markers. */
  final byte[][] literals;
  /** Parsed variable markers. */
  final DateFormat[] formats;

  /**
   * Constructor.
   * @param picture picture
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DatePicture(final byte[] picture, final InputInfo info) throws QueryException {
    final TokenList lits = new TokenList();
    final ArrayList<DateFormat> fmts = new ArrayList<>();
    final TokenBuilder literal = new TokenBuilder();
    final TokenParser tp = new TokenParser(picture);
    while(tp.more()) {
      final int ch = tp.next();
      if(ch == '[' && !tp.consume('[')) {
        // variable marker (whitespace is ignored)
        final TokenBuilder marker = new TokenBuilder();
        while(true) {
          if(!tp.more()) throw PICDATE_X.get(info, picture);
          final int cp = tp.next();
          if(cp == ']') break;
          if(!ws(cp)) marker.add(cp);
        }
        if(marker.isEmpty()) throw PICDATE_X.get(info, picture);
        lits.add(literal.next());
        fmts.add(new DateFormat(marker.finish(), info));
      } else {
        // literal (closing brackets must be doubled)
        if(ch == ']' && !tp.consume(']')) throw PICDATE_X.get(info, picture);
        literal.add(ch);
      }
    }
    lits.add(literal.finish());
    literals = lits.finish();
    formats = fmts.toArray(DateFormat[]::new);
  }
}
