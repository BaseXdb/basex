package org.basex.query.pf;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.util.Token;
import static org.basex.query.pf.PFT.*;

/**
 * Staircase node test.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class ScT {
  /** Node kind. */
  private int kn;
  /** Node name. */
  private int nm;
  
  /**
   * Constructor.
   * @param d data reference
   * @param t of test
   * @throws QueryException query exception
   */
  ScT(final Data d, final byte[] t) throws QueryException {
    final byte[][] split = Token.split(t, ' ');
    kn = k(split[0]);
    if(split.length == 5) nm = d.tagID(split[1]);
  }
  
  /**
   * Performs a node test.
   * @param d data reference
   * @param p pre value
   * @param k node kind
   * @return result of node test
   */
  boolean e(final Data d, final int p, final int k) {
    if(kn == -1) return true;
    if(kn !=  k) return false;
    if(kn == Data.ELEM) return nm == 0 || nm == d.tagID(p);
    if(kn == Data.ATTR) return nm == 0 || nm == d.attNameID(p);
    return true;
  }
  
  /**
   * Returns the kind for the specified token.
   * @param k token
   * @return node kind
   * @throws QueryException query exception
   */
  private int k(final byte[] k) throws QueryException {
    if(Token.eq(k, KNODE)) return -1;
    if(Token.eq(k, KATTR)) return Data.ATTR;
    if(Token.eq(k, KELEM)) return Data.ELEM;
    if(Token.eq(k, KTEXT)) return Data.TEXT;
    if(Token.eq(k, KCOMM)) return Data.COMM;
    if(Token.eq(k, KPI)) return Data.PI;
    throw new QueryException(PFTEST, k);
  }
}
