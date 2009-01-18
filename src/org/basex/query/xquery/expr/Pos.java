package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.CmpV.Comp;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * Pos Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Pos extends Simple {
  /** Minimum position. */
  public long min;
  /** Maximum position. */
  public long max;
  
  /**
   * Constructor.
   * @param mn minimum value
   * @param mx minimum value
   */
  private Pos(final long mn, final long mx) {
    min = mn;
    max = mx;
  }
  
  /**
   * Returns an position or an optimized expression.
   * @param mn minimum value
   * @param mx minimum value
   * @return expression
   */
  public static Expr get(final long mn, final long mx) {
    return mn > mx || mx < 1 ? Bln.FALSE : mn <= 1 && mx == Long.MAX_VALUE ?
      Bln.TRUE : new Pos(mn, mx);
  }

  /**
   * Returns an instance of this class, if possible, and the input expression
   * otherwise.
   * @param expr calling expression
   * @param cmp comparator
   * @param arg argument
   * @return resulting expression
   * @throws XQException evaluation exception
   */
  public static Expr get(final Expr expr, final Comp cmp, final Expr arg)
      throws XQException {

    if(!arg.i()) return expr;
    Item it = (Item) arg;
    if(it.n()) {
      final long p = it.itr();
      final boolean ex = p == it.dbl();
      switch(cmp) {
        case EQ: return ex ? get(p, p) : Bln.FALSE;
        case GE: return get(ex ? p : p + 1, Long.MAX_VALUE);
        case GT: return get(p + 1, Long.MAX_VALUE);
        case LE: return get(1, p);
        case LT: return get(1, ex ? p - 1 : p);
        default:
      }
    }
    return expr;
  }

  /**
   * Creates an intersection of the existing and the specified position
   * expressions.
   * @param pos second position expression
   * @return resulting expression
   */
  public Expr intersect(final Pos pos) {
    return get(Math.max(min, pos.min), Math.min(max, pos.max));
  }

  /**
   * Creates a union of the existing and the specified position expressions.
   * @param pos second position expression
   * @return resulting expression
   */
  public Expr union(final Pos pos) {
    return get(Math.min(min, pos.min), Math.max(max, pos.max));
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    if(ctx.item == null) Err.or(XPNOCTX, this);
    return Bln.get(ctx.pos >= min && ctx.pos <= max).iter();
  }

  /**
   * Returns false if no more results can be expected.
   * @param ctx query context
   * @return result of check
   */
  public boolean more(final XQContext ctx) {
    return ctx.pos <= max;
  }
  
  @Override
  public boolean usesPos(final XQContext ctx) {
    return true;
  }
  
  @Override
  public Type returned(final XQContext ctx) {
    return Type.BLN;
  }

  @Override
  public String toString() {
    return "pos(" + min + (min == max ? "" : "-" +
        (max == Long.MAX_VALUE ? "" : max)) + ")";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, MIN, Token.token(min), MAX,
        max == Long.MAX_VALUE ? INF : Token.token(max));
  }
}
