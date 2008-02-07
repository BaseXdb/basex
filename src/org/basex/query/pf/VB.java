package org.basex.query.pf;

import org.basex.query.QueryException;
import org.basex.util.Token;
import static org.basex.query.pf.PFT.*;

/**
 * This class generates new XQuery values.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class VB {
  /** Data ids. */
  private static final int[] IDS = {
    INT, INT, PRE, DBL, DBL, BLN, STR, STR, STR };
  /** Data types. */
  private static final byte[][] TYPES = {
    TINT, TNAT, TNODE, TDBL, TDEC, TBLN, TSTR, TQNAME, TUA
  };
  
  /** Private constructor, preventing class instantiation. */
  private VB() { }
  
  /**
   * Returns the type(s) for the specified token.
   * @param t token
   * @return type
   * @throws QueryException query exception
   */
  static int type(final byte[] t) throws QueryException {
    if(t.length == 0) return 0; // data type = ''?
    
    int v = 0;
    for(byte[] split : Token.split(t, ' ')) {
      for(int n = 0; n < TYPES.length; n++) {
        if(Token.eq(split, TYPES[n])) v |= IDS[n];
      }
    }
    if(v == 0) throw new QueryException(PFTYPE, t);
    return v;
  }
  
  /**
   * Returns a value for the specified token and data type.
   * @param s token
   * @param t type
   * @return value
   * @throws QueryException query exception
   */
  static V v(final byte[] s, final int t) throws QueryException {
    if((t & INT) != 0) return new I(s);
    if((t & PRE) != 0) return new N(s);
    if((t & DBL) != 0) return new D(s);
    if((t & STR) != 0) return new S(s);
    if((t & BLN) != 0) return B.v(Token.eq(s, Token.TRUE));
    throw new QueryException(PFTYPE, t);
  }
}

