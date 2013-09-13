package org.basex.index.query;

import org.basex.index.*;

/**
 * This class defines access to index tokens.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface IndexToken {
  /**
   * Returns the index type.
   * @return type
   */
  IndexType type();

  /**
   * Returns the current token.
   * @return token
   */
  byte[] get();
}
