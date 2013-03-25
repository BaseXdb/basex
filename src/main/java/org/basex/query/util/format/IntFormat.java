package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IntFormat extends FormatParser {
  /**
   * Constructor.
   * @param p picture
   * @param ii input info
   * @throws QueryException query exception
   */
  public IntFormat(final byte[] p, final InputInfo ii) throws QueryException {
    super(ii);

    final int sc = lastIndexOf(p, ';');
    final byte[] pres = sc == -1 ? p : substring(p, 0, sc);
    if(pres.length == 0) PICEMPTY.thrw(info, p);
    finish(presentation(pres, ONE, false));
    if(sc == -1) return;

    // parses the format modifier
    final byte[] mod = substring(p, sc + 1);

    final TokenParser tp = new TokenParser(mod);
    // parse cardinal/ordinal flag
    if(tp.consume('c') || tp.consume('o')) {
      final TokenBuilder ord = new TokenBuilder();
      if(tp.consume('(')) {
        while(!tp.consume(')')) {
          if(!tp.more()) INVORDINAL.thrw(info, mod);
          ord.add(tp.next());
        };
      }
      ordinal = ord.finish();
    }
    // parse alphabetical/traditional flag
    if(!tp.consume('a')) tp.consume('t');
    if(tp.more()) INVORDINAL.thrw(info, mod);
  }
}
