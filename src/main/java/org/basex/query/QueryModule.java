package org.basex.query;

/**
 * <p>The XQuery module import statement can be used to import Java code. If the namespace
 * URI is prefixed with the string {@code java:}, the remaining string is interpreted as
 * class path, and an instance of the addressed Java class will be bound to the namespace.
 * </p>
 *
 * <p>If the Java class inherits this abstract class, instances of the following classes
 * will be available:</p>
 * <ul>
 * <li>The {@link QueryContext} provides access to all static and dynamic properties
 *     of the current query</li>
 * </ul>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class QueryModule {
  /** Query context. */
  public QueryContext context;

  /**
   * Initializes the query context and input info.
   * @param ctx query context
   */
  public void init(final QueryContext ctx) {
    context = ctx;
  }
}
