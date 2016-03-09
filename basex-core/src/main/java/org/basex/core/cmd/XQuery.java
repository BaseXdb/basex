package org.basex.core.cmd;

import org.basex.core.users.*;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class XQuery extends AQuery {
  /**
   * Default constructor.
   * @param query query to evaluate
   */
  public XQuery(final String query) {
    super(Perm.NONE, false, query);
  }

  @Override
  protected boolean run() {
    return query(args[0]);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound
   * @return reference
   */
  public XQuery bind(final String name, final String value) {
    return bind(name, value, null);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound
   * @param type type
   * @return reference
   */
  public XQuery bind(final String name, final String value, final String type) {
    vars.put(name, new String[] { value, type });
    return this;
  }
}
