package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.SeqType;
import org.basex.util.Token;

/**
 * Instance Test.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Instance extends Single {
  /** Instance. */
  private SeqType seq;
  
  /**
   * Constructor.
   * @param e expression
   * @param s sequence type
   */
  public Instance(final Expr e, final SeqType s) {
    super(e);
    seq = s;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(!expr.i()) return this;
    ctx.compInfo(OPTPRE, this);
    return atomic(ctx);
  }
  
  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {
    return Bln.get(seq.instance(expr.iter(ctx)));
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
    return BaseX.info("% instance of %", expr, seq);
  }
}
