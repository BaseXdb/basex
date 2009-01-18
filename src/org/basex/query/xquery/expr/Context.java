package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.Axis;
import org.basex.query.xquery.path.AxisPath;
import org.basex.query.xquery.path.KindTest;
import org.basex.query.xquery.path.Step;

/**
 * Context Item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Context extends Simple {
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return checkCtx(ctx);
  }

  @Override
  public Expr addText(final XQContext ctx) {
    // replacing . with text() for possible index integration
    if(!ctx.leaf) return this;
    ctx.compInfo(OPTTEXT);
    return AxisPath.get(null, Step.get(Axis.CHILD, new KindTest(Type.TXT)));
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
