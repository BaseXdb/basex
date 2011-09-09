package org.basex.server;

import java.io.IOException;

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
   * @throws IOException I/O exception
   */
  public final void bind(final String n, final Object v) throws IOException {
    bind(n, v, "");
  }

  /**
   * Binds a value with an optional type to an external variable.
   * @param n name of variable
   * @param v value to be bound
   * @param t data type
   * @throws IOException I/O exception
   */
  public abstract void bind(final String n, final Object v, final String t)
      throws IOException;

  /**
   * Initializes the query expression and starts serialization.
   * @return result header or {@code null}.
   * @throws IOException I/O exception
   */
  public abstract String init() throws IOException;

  /**
   * Returns {@code true} if more items are available.
   * @return result of check
   * @throws IOException I/O exception
   */
  public abstract boolean more() throws IOException;

  /**
   * Returns the next item of the query.
   * @return item string or {@code null}.
   * @throws IOException I/O exception
   */
  public abstract String next() throws IOException;

  /**
   * Returns the complete result of the query.
   * @return item string or {@code null}.
   * @throws IOException I/O exception
   */
  public abstract String execute() throws IOException;

  /**
   * Returns query info.
   * @return query info
   * @throws IOException I/O exception
   */
  public abstract String info() throws IOException;

  /**
   * Finishes result serialization and closes the iterator.
   * @return result footer or {@code null}.
   * @throws IOException I/O exception
   */
  public abstract String close() throws IOException;
}
