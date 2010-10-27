package org.basex.server;

import java.io.OutputStream;
import org.basex.core.BaseXException;

/**
 * This class defines all commands for iterative query execution.
 * It is implemented both by {@link ClientQuery} and {@link LocalQuery}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Query {
  /**
   * Binds an object to a global variable.
   * @param n name of variable
   * @param o object to be bound
   * @throws BaseXException command exception
   */
  public abstract void bind(final String n, final Object o)
      throws BaseXException;

  /**
   * Returns {@code true} if more items are available.
   * @return result of check
   * @throws BaseXException command exception
   */
  public abstract boolean more() throws BaseXException;

  /**
   * Prints the next result to the specified output stream.
   * @param out output stream
   * @throws BaseXException command exception
   */
  public abstract void next(final OutputStream out) throws BaseXException;

  /**
   * Returns the next item.
   * @return item string
   * @throws BaseXException command exception
   */
  public abstract String next() throws BaseXException;

  /**
   * Closes the iterator.
   * @throws BaseXException command exception
   */
  public abstract void close() throws BaseXException;
}
