package org.basex.query.xquery.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xquery.XQText.*;
import org.basex.data.Serializer;
import org.basex.query.FTPos;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTSelect extends Single {
  /** Position filter. */
  public FTPos pos;
  /** Window. */
  public Expr window;
  /** Distance occurrences. */
  public Expr[] dist;
  /** Weight. */
  public Expr weight;

  /**
   * Constructor.
   * @param e expression
   * @param u fulltext selections
   */
  public FTSelect(final Expr e, final FTPos u) {
    super(e);
    pos = u;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final FTPos tmp = ctx.ftpos;
    ctx.ftpos = pos;
    pos.init(ctx.ftitem);

    final Item it = ctx.iter(expr).next();
    ctx.ftpos = tmp;
    final double s = it.dbl();
    if(s == 0 || !posFilter(ctx)) return Dbl.iter(0);

    // calculate weight
    final double d = checkDbl(ctx.iter(weight));
    if(d < 0 || d > 1000) Err.or(FTWEIGHT, d);
    return d != 1 ? Dbl.iter(s * d) : it.iter();
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @return result of check
   * @throws XQException query exception
   */
  private boolean posFilter(final XQContext ctx) throws XQException {
    if(!pos.valid()) return false;

    // ...distance?
    if(pos.dunit != null) {
      final long mn = checkItr(ctx.iter(dist[0]));
      final long mx = checkItr(ctx.iter(dist[1]));
      if(!pos.distance(mn, mx)) return false;
    }
    // ...window?
    if(pos.wunit != null) {
      final long c = checkItr(ctx.iter(window));
      if(!pos.window(c)) return false;
    }
    return true;
  }
  
  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    weight = weight.comp(ctx);
    return super.comp(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.startElement(this);
    if(pos.ordered) ser.attribute(Token.token(ORDERED), Token.TRUE);
    if(pos.start) ser.attribute(Token.token(START), Token.TRUE);
    if(pos.end) ser.attribute(Token.token(END), Token.TRUE);
    if(pos.content) ser.attribute(Token.token(CONTENT), Token.TRUE);
    ser.finishElement();
    expr.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr);
    if(pos.ordered) sb.append(" " + ORDERED);
    if(pos.start) sb.append(" " + AT + " " + START);
    if(pos.end) sb.append(" " + AT + " " + END);
    if(pos.content) sb.append(" " + ENTIRE + " " + CONTENT);
    if(pos.dunit != null) {
      sb.append(" distance(");
      sb.append(dist[0]);
      sb.append(",");
      sb.append(dist[1]);
      sb.append(")");
    }
    return sb.toString();
  }
}
