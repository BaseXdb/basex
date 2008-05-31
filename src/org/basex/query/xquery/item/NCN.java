package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
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
   * @throws XQException evaluation exception
   */
  public NCN(final byte[] v) throws XQException {
    this(v, Type.NCN);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   * @throws XQException evaluation exception
   */
  public NCN(final byte[] v, final Type t) throws XQException {
    super(Token.norm(v), t);
    check(val);
  }

  /**
   * Checks the validity of the specified name.
   * @param v name to be checked
   * @throws XQException if name is invalid
   */
  private static void check(final byte[] v) throws XQException {
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
