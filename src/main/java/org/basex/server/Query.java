package org.basex.server;

import org.basex.core.BaseXException;

/**
 * <p>This class defines methods for executing queries.
 * It is implemented by {@link ClientQuery}.</p>
 * <p>Results are either returned as string or serialized to the output
 * stream that has been specified via the constructor or via
 * {@link Session#setOutputStream(java.io.OutputStream)}.</p>
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Query {
  /**
   * Binds a value to an external variable.
   * @param n name of variable
   * @param v value to be bound
   * @throws BaseXException command exception
   */
  public final void bind(final String n, final Object v) throws BaseXException {
    bind(n, v, "");
  }

  /**
   * Binds a value with an optional type to an external variable.
   * @param n name of variable
   * @param v value to be bound
   * @param t data type
   * @throws BaseXException command exception
   */
  public abstract void bind(final String n, final Object v, final String t)
      throws BaseXException;

  /**
   * Initializes the query expression and starts serialization.
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
   * Returns the next item of the query.
   * @return item string or {@code null}.
   * @throws BaseXException command exception
   */
  public abstract String next() throws BaseXException;

  /**
   * Returns the complete result of the query.
   * @return item string or {@code null}.
   * @throws BaseXException command exception
   */
  public abstract String execute() throws BaseXException;

  /**
   * Returns query info.
   * @return query info
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
