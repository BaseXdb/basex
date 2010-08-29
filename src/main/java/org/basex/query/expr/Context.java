package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.KindTest;
import org.basex.query.path.Step;
import org.basex.util.InputInfo;

/**
 * Context item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    if(ctx.value != null) type = ctx.value.type.seq();
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return checkCtx(ctx).iter(ctx);
  }
  
  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return checkCtx(ctx).atomic(ctx, input);
  }

  @Override
  public Expr addText(final QueryContext ctx) {
    // replacing . with text() for possible index integration
    if(!ctx.leaf) return this;
    ctx.compInfo(OPTTEXT);
    return AxisPath.get(input, null,
        Step.get(input, Axis.CHILD, new KindTest(Type.TXT)));
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
