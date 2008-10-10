package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * Instance Test.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public Iter iter(final XQContext ctx) throws XQException {
    return Bln.get(seq.instance(ctx.iter(expr))).iter();
  }
  
  @Override
  public String toString() {
    return BaseX.info("% instance of %", expr, seq);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }
}
