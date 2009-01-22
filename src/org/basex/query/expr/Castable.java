package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(!expr.i()) return this;
    final Bln ok = eval(ctx, ((Item) expr).iter());
    ctx.compInfo(ok == Bln.TRUE ? OPTTRUE : OPTFALSE, this);
    return ok;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return eval(ctx, ctx.iter(expr)).iter();
  }

  /**
   * Evaluates the cast expression.
   * @param ctx query context
   * @param iter iterator
   * @return result of cast
   */
  private Bln eval(final QueryContext ctx, final Iter iter) {
    try {
      seq.cast(iter, this, ctx);
      return Bln.TRUE;
    } catch(final QueryException e) {
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
