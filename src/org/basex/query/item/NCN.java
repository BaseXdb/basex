package org.basex.query.item;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * NCName item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NCN extends Str {
  /**
   * Constructor.
   * @param v value
   * @throws QueryException evaluation exception
   */
  public NCN(final byte[] v) throws QueryException {
    super(Token.norm(v), Type.NCN);

    if(v.length == 0) Err.or(XPNAME);
    int i = -1;
    while(++i != v.length) {
      final byte c = v[i];
      if(Token.letter(c)) continue;
      if(i == 0 || !Token.digit(c) && c != '-' && c != '_' && c != '.') 
        Err.or(XPINVNAME, v);
    }
  }
}
