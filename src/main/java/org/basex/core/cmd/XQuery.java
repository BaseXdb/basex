package org.basex.core.cmd;

import org.basex.core.*;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author BaseX Team 2005-12, BSD License
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
}
