package org.basex.index;

import java.io.IOException;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface Index {
  /**
   * Returns information on the index structure.
   * @return info
   */
  byte[] info();

  /**
   * Returns an iterator for the index results.
   * @param token token to be found
   * @return sorted pre values for the token
   */
  IndexIterator iter(final IndexToken token);

  /**
   * Returns the (approximate/estimated) number of hits for the specified token.
   * @param token token to be found
   * @return number of hits
   */
  int count(final IndexToken token);

  /**
   * Closes the index.
   * @throws IOException I/O exception
   */
  void close() throws IOException;
}
