package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.KindTest;
import org.basex.query.path.Step;

/**
 * Context item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Context extends Simple {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return checkCtx(ctx).iter();
  }

  @Override
  public Expr addText(final QueryContext ctx) {
    // replacing . with text() for possible index integration
    if(!ctx.leaf) return this;
    ctx.compInfo(OPTTEXT);
    return AxisPath.get(null, Step.get(Axis.CHILD, new KindTest(Type.TXT)));
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.CTX || u == Use.ELM;
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
