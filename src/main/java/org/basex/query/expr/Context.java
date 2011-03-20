package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.AxisStep;
import org.basex.query.path.Test;
import org.basex.util.InputInfo;

/**
 * Context item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Context extends Simple {
  /**
   * Constructor.
   * @param ii input info
   */
  public Context(final InputInfo ii) {
    super(ii);
  }

  @Override
  public Expr comp(final QueryContext ctx) {
    if(ctx.value != null) {
      type = ctx.value.type.seq();
      size = ctx.value.size();
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return checkCtx(ctx).iter();
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return checkCtx(ctx).item(ctx, input);
  }

  @Override
  public Expr addText(final QueryContext ctx) {
    // replacing . with text() for possible index integration
    if(!ctx.leaf) return this;
    ctx.compInfo(OPTTEXT);
    return AxisPath.get(input, null, AxisStep.get(input, Axis.CHILD, Test.TXT));
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Context;
  }

  @Override
  public String toString() {
    return ".";
  }
}
