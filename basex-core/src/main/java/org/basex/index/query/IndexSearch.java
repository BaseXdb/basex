package org.basex.index.query;

import org.basex.index.*;

/**
 * This class provides an index search definition.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface IndexSearch {
  /**
   * Returns the index type.
   * @return type
   */
  IndexType type();

  /**
   * Returns the current token.
   * @return token
   */
  byte[] token();
}
