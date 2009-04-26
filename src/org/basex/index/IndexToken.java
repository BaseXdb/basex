package org.basex.index;

import org.basex.data.Data.Type;

/**
 * This class defines access to index tokens.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class IndexToken {
  /** Index type. */
  public Type type;

  /**
   * Constructor.
   * @param t index type
   */
  public IndexToken(final Type t) {
    type = t;
  }
  
  /**
   * Returns the current index token.
   * @return token
   */
  public abstract byte[] get();
  
  /**
   * Returns the range flag. Can be overwritten by an implementation to
   * support range searches.
   * @return range flag
   */
  public boolean range() {
    return false;
  }
}
