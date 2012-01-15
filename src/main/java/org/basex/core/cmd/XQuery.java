package org.basex.core.cmd;

import org.basex.core.Context;

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
    super(STANDARD, query);
  }

  @Override
  protected boolean run() {
    return query(args[0]);
  }

  @Override
  public boolean updating(final Context ctx) {
    return super.updating(ctx) || updating(ctx, args[0]);
  }
}
