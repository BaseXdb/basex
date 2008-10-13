package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
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

    try {
      seq.cast(iter(expr), this, ctx);
      return Bln.TRUE;
    } catch(final XQException e) {
      return Bln.FALSE;
    }
  }

  @Override
  public Iter iter(final XQContext ctx) {
    try {
      seq.cast(ctx.iter(expr), this, ctx);
      return Bln.TRUE.iter();
    } catch(final XQException e) {
      return Bln.FALSE.iter();
    }
  }

  @Override
  public String toString() {
    return expr + " castable?";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }
}
