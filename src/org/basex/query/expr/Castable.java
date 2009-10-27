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
import org.basex.util.Token;

/**
 * Castable expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Castable extends Single {
  /** Instance. */
  private final SeqType seq;

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
    if(!checkUp(expr, ctx).i()) return this;
    final Item ok = atomic(ctx);
    ctx.compInfo(ok == Bln.TRUE ? OPTTRUE : OPTFALSE, this);
    return ok;
  }

  @Override
  public Bln atomic(final QueryContext ctx) {
    try {
      final Item it = expr.atomic(ctx);
      seq.cast(it, this, ctx);
      return Bln.TRUE;
    } catch(final QueryException ex) {
      return Bln.FALSE;
    }
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr + " " + CASTABLE + " " + AS + " " + seq;
  }
}
