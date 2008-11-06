package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.iter.Iter;
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
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    if(!expr.i()) return this;
    
    ctx.compInfo(OPTCAST, expr);
    return seq.cast(iter(expr), this, ctx);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return seq.cast(ctx.iter(expr), this, ctx).iter();
  }

  @Override
  public String toString() {
    return BaseX.info("%(%)", seq, expr);
  }

  @Override
  public String info() {
    return seq.type + " cast";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()), NS, timer());
    expr.plan(ser);
    ser.closeElement();
  }
}
