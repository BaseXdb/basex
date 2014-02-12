package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.http.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * HTTP Client Module.
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 */
public final class FNHttp extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNHttp(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);

    // get request node
    final Item req = expr[0].item(ctx, info);
    final ANode request = req == null ? null : checkNode(req);

    // get HTTP URI
    final byte[] href = expr.length >= 2 ? checkEStr(expr[1].item(ctx, info)) : null;

    // get parameter $bodies
    ValueBuilder cache = null;
    if(expr.length == 3) {
      final Iter bodies = expr[2].iter(ctx);
      cache = new ValueBuilder();
      for(Item i; (i = bodies.next()) != null;) cache.add(i);
    }

    // send HTTP request
    return new HTTPClient(info, ctx.context.options).sendRequest(href, request, cache);
  }
}
