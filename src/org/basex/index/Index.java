package org.basex.index;

import java.io.IOException;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Index {
  /**
   * Returns information on the index structure.
   * @return info
   */
  public abstract byte[] info();

  /**
   * Returns an iterator for the index results.
   * @param tok token to be found
   * @return ids
   */
  public abstract IndexIterator ids(final IndexToken tok);

  /**
   * Returns the (approximate/estimated) number of ids for the specified token.
   * @param tok token to be found
   * @return number of ids
   */
  public abstract int nrIDs(final IndexToken tok);

  /**
   * Closes the index.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;
}
