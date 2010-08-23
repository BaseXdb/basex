package org.basex.server;

import java.io.OutputStream;
import org.basex.core.BaseXException;

/**
 * This class defines all commands for iterative query execution.
 * It is implemented both by {@link LocalSession} and {@link ClientSession}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Query {
  /**
   * Checks for the next item.
   * @return value of check
   * @throws BaseXException database exception
   */
  public abstract boolean more() throws BaseXException;

  /**
   * Prints the next result to the specified output stream.
   * @param out output stream
   * @throws BaseXException database exception
   */
  public abstract void next(final OutputStream out) throws BaseXException;

  /**
   * Returns the next item.
   * @return item string
   * @throws BaseXException database exception
   */
  public abstract String next() throws BaseXException;

  /**
   * Closes the iterator.
   * @throws BaseXException database exception
   */
  public abstract void close() throws BaseXException;
}
