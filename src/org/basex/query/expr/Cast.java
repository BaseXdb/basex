package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.util.Token;

/**
 * Cast expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Cast extends Single {
  /** Type. */
  public final SeqType seq;

  /**
   * Function constructor.
   * @param e expression
   * @param t data type
   */
  public Cast(final Expr e, final SeqType t) {
    super(e);
    seq = t;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(!expr.i()) return this;

    ctx.compInfo(OPTPRE, this);
    return seq.cast(((Item) expr).iter(), this, ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return seq.cast(ctx.iter(expr), this, ctx).iter();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String info() {
    return seq.type + " " + CAST;
  }

  @Override
  public String toString() {
    return BaseX.info("% cast as %", expr, seq);
  }
}
