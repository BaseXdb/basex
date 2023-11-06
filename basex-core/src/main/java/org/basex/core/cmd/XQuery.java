package org.basex.core.cmd;

import java.util.AbstractMap.*;
import java.util.Map.*;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author BaseX Team 2005-23, BSD License
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
    return bind(name, new SimpleEntry<>(value, null));
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value and type to be bound
   * @return self reference
   */
  public XQuery bind(final String name, final Entry<Object, String> value) {
    bindings.put(name, value);
    return this;
  }
}
