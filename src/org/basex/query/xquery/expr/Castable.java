package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * Castable expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Castable extends Single {
  /** Instance. */
  final SeqType seq;

  /**
   * Constructor.
   * @param e expression
   * @param s sequence type
   */
  public Castable(final Expr e, final SeqType s) {
    super(e);
    seq = s;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    if(!expr.i()) return this;
    final Bln ok = eval(ctx, ((Item) expr).iter());
    ctx.compInfo(ok == Bln.TRUE ? OPTTRUE : OPTFALSE, this);
    return ok;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return eval(ctx, ctx.iter(expr)).iter();
  }

  /**
   * Evaluates the cast expression.
   * @param ctx query context
   * @param iter iterator
   * @return result of cast
   */
  private Bln eval(final XQContext ctx, final Iter iter) {
    try {
      seq.cast(iter, this, ctx);
      return Bln.TRUE;
    } catch(final XQException e) {
      return Bln.FALSE;
    }
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr + " castable as " + seq;
  }
}
