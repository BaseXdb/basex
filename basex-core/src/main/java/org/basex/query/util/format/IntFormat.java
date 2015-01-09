package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class IntFormat extends FormatParser {
  /**
   * Constructor.
   * @param picture picture
   * @param info input info
   * @throws QueryException query exception
   */
  public IntFormat(final byte[] picture, final InputInfo info) throws QueryException {
    super(info);

    final int sc = lastIndexOf(picture, ';');
    final byte[] pres = sc == -1 ? picture : substring(picture, 0, sc);
    if(pres.length == 0) throw PICEMPTY.get(info, picture);
    finish(presentation(pres, ONE, false));
    if(sc == -1) return;

    // parses the format modifier
    final byte[] mod = substring(picture, sc + 1);

    final TokenParser tp = new TokenParser(mod);
    // parse cardinal/ordinal flag
    if(tp.consume('c') || tp.consume('o')) {
      final TokenBuilder tb = new TokenBuilder();
      if(tp.consume('(')) {
        while(!tp.consume(')')) {
          if(!tp.more()) throw INVORDINAL_X.get(info, mod);
          final int cp = tp.next();
          if(cp != '-') tb.add(cp);
        }
      }
      ordinal = tb.finish();
    }
    // parse alphabetical/traditional flag
    if(!tp.consume('a')) tp.consume('t');
    if(tp.more()) throw INVORDINAL_X.get(info, mod);
  }
}
