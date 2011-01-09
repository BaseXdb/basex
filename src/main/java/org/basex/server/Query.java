package org.basex.server;

import org.basex.core.BaseXException;

/**
 * This class defines commands for iterative query execution.
 * It is implemented by {@link ClientQuery}.
 * Results are either returned as string or serialized to the output
 * stream that has been specified in the constructor or in
 * {@link Session#setOutputStream(java.io.OutputStream)}.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public abstract class Query {
  /**
   * Binds a value to an external variable.
   * @param n name of variable
   * @param v value to be bound
   * @throws BaseXException command exception
   */
  public final void bind(final String n, final String v) throws BaseXException {
    bind(n, v, "");
  }

  /**
   * Binds a value with a specific type to an external variable.
   * @param n name of variable
   * @param v value to be bound
   * @param t data type
   * @throws BaseXException command exception
   */
  public abstract void bind(final String n, final String v, final String t)
      throws BaseXException;

  /**
   * Returns {@code true} if more items are available.
   * @return result header or {@code null}.
   * @throws BaseXException command exception
   */
  public abstract String init() throws BaseXException;

  /**
   * Returns {@code true} if more items are available.
   * @return result of check
   * @throws BaseXException command exception
   */
  public abstract boolean more() throws BaseXException;

  /**
   * Returns the next item.
   * @return item string or {@code null}.
   * @throws BaseXException command exception
   */
  public abstract String next() throws BaseXException;

  /**
   * Returns the complete query result.
   * @return item string or {@code null}.
   * @throws BaseXException command exception
   */
  public abstract String execute() throws BaseXException;

  /**
   * Returns the query info.
   * @return item string or {@code null}.
   * @throws BaseXException command exception
   */
  public abstract String info() throws BaseXException;

  /**
   * Finishes result serialization and closes the iterator.
   * @return result footer or {@code null}.
   * @throws BaseXException command exception
   */
  public abstract String close() throws BaseXException;
}
