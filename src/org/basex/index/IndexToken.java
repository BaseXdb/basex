package org.basex.index;

import org.basex.data.Data.Type;

/**
 * This class defines access to index tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public interface IndexToken {
  /**
   * Returns the index type.
   * @return type
   */
  Type type();

  /**
   * Returns the current token.
   * @return token
   */
  byte[] get();
}
