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
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class FNHttp extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNHttp(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);

    // get request node
    final Item req = exprs[0].item(ctx, info);
    final ANode request = req == null ? null : checkNode(req);

    // get HTTP URI
    final byte[] href = exprs.length >= 2 ? checkEStr(exprs[1].item(ctx, info)) : null;

    // get parameter $bodies
    ValueBuilder cache = null;
    if(exprs.length == 3) {
      final Iter bodies = exprs[2].iter(ctx);
      cache = new ValueBuilder();
      for(Item i; (i = bodies.next()) != null;) cache.add(i);
    }

    // send HTTP request
    return new HTTPClient(info, ctx.context.options).sendRequest(href, request, cache);
  }
}
