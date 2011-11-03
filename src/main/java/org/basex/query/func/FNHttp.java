package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.util.http.HTTPClient;
import org.basex.util.InputInfo;

/**
 * HTTP Client Module.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class FNHttp extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNHttp(final InputInfo ii, final Function f, final Expr[] e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);

    // get request node
    final ANode request = expr[0].item(ctx, input) == null ? null :
      checkNode(expr[0].item(ctx, input));

    // get HTTP URI
    final byte[] href = expr.length >= 2 ?
        checkEStr(expr[1].item(ctx, input)) : null;

    // get parameter $bodies
    ItemCache cache = null;
    if(expr.length == 3) {
      final Iter bodies = expr[2].iter(ctx);
      cache = new ItemCache();
      for(Item i; (i = bodies.next()) != null;) cache.add(i);
    }

    // send HTTP request
    return HTTPClient.sendRequest(href, request,
        cache, input, ctx.context.prop);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT || super.uses(u);
  }
}
