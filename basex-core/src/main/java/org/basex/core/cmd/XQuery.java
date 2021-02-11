package org.basex.core.cmd;

import org.basex.query.value.*;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQuery extends AQuery {
  /**
   * Default constructor.
   * @param query query to evaluate
   */
  public XQuery(final String query) {
    super(false, query);
  }

  @Override
  protected boolean run() {
    return query(args[0]);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value XQuery value to be bound
   * @return self reference
   */
  public XQuery bind(final String name, final Value value) {
    vars.put(name, value);
    return this;
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound (XQuery value; any other value will be converted to a string)
   * @return self reference
   */
  public XQuery bind(final String name, final String value) {
    return bind(name, value, null);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound
   * @param type type
   * @return self reference
   */
  public XQuery bind(final String name, final String value, final String type) {
    vars.put(name, new String[] { value, type });
    return this;
  }
}
