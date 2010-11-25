package org.basex.core.cmd;

import org.basex.core.Context;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  public boolean writing(final Context ctx) {
    return super.writing(ctx) || updating(ctx, args[0]);
  }
}
