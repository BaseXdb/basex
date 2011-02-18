package org.basex.query.item;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * NCName item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class NCN extends Str {
  /**
   * Constructor.
   * @param v value
   * @param ii input info
   * @throws QueryException query exception
   */
  public NCN(final byte[] v, final InputInfo ii) throws QueryException {
    super(Token.norm(v), Type.NCN);

    if(v.length == 0) XPNAME.thrw(ii);
    int i = -1;
    while(++i != v.length) {
      final byte c = v[i];
      if(Token.letter(c)) continue;
      if(i == 0 || !Token.digit(c) && c != '-' && c != '_' && c != '.')
        XPINVNAME.thrw(ii, v);
    }
  }
}
