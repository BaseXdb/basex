package org.basex.index;

import java.io.IOException;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
   * Close the index.
   * @throws IOException in case of write errors
   */
  @SuppressWarnings("unused")
  public void close() throws IOException { }
}
