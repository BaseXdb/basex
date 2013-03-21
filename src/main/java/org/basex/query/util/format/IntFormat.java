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
    byte[] pres = sc == -1 ? p : substring(p, 0, sc);
    if(pres.length == 0) PICEMPTY.thrw(info, p);
    pres = presentation(pres, ONE, false);

    if(sc != -1) format(substring(p, sc + 1));

    // skip correction of case if modifier has more than one codepoint (Ww)
    cs = cl(pres, 0) < pres.length ? Case.STANDARD :
        (ch(pres, 0) & ' ') == 0 ? Case.UPPER : Case.LOWER;
    primary = lc(pres);
    if(digit == -1) digit = ch(primary, 0);
  }

  /**
   * Parses the format modifier.
   * @param format picture
   * @throws QueryException query exception
   */
  private void format(final byte[] format) throws QueryException {
    final TokenParser tp = new TokenParser(format);
    // parse cardinal/ordinal flag
    if(tp.consume('c') || tp.consume('o')) {
      final TokenBuilder ord = new TokenBuilder();
      if(tp.consume('(')) {
        while(!tp.consume(')')) {
          if(!tp.more()) INVORDINAL.thrw(info, format);
          ord.add(tp.next());
        };
      }
      ordinal = ord.finish();
    }
    // parse alphabetical/traditional flag
    if(!tp.consume('a')) tp.consume('t');
    if(tp.more()) INVORDINAL.thrw(info, format);
  }
}
