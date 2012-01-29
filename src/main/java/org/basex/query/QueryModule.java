package org.basex.query;

import org.basex.util.InputInfo;

/**
 * Java module binding.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class QueryModule {
  /** Query context. */
  protected QueryContext context;
  /** Input info. */
  protected InputInfo input;

  /**
   * Constructor.
   * @param ctx query context
   * @param ii input info
   */
  public void init(final QueryContext ctx, final InputInfo ii) {
    context = ctx;
    input = ii;
  }
}
