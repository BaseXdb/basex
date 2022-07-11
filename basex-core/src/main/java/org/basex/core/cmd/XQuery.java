package org.basex.core.cmd;

import java.util.AbstractMap.*;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class XQuery extends AQuery {
  /**
   * Default constructor.
   * @param query query to evaluate
   */
  public XQuery(final String query) {
    super(false, query, query);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound (XQuery value; any other value will be converted to a string)
   * @return self reference
   */
  public XQuery bind(final String name, final Object value) {
    return bind(name, value, null);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound
   * @param type type (can be {@code null})
   * @return self reference
   */
  public XQuery bind(final String name, final Object value, final String type) {
    bindings.put(name, new SimpleEntry<>(value, type));
    return this;
  }
}
