package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CmpV.Comp;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.util.Token;

/**
 * Pos expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Pos extends Simple {
  /** Minimum position. */
  final long min;
  /** Maximum position. */
  final long max;

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
  static Expr get(final long mn, final long mx) {
    // suppose that positions always fit in int values..
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
   * @throws QueryException query exception
   */
  static Expr get(final Expr expr, final Comp cmp, final Expr arg)
      throws QueryException {

    if(!arg.item()) return expr;

    final Item it = (Item) arg;
    if(it.num()) {
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

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {
    checkCtx(ctx);
    return Bln.get(ctx.pos >= min && ctx.pos <= max);
  }

  /**
   * Returns false if no more results can be expected.
   * @param ctx query context
   * @return result of check
   */
  public boolean last(final QueryContext ctx) {
    return ctx.pos >= max;
  }

  /**
   * Creates an intersection of the existing and the specified position
   * expressions.
   * @param pos second position expression
   * @return resulting expression
   */
  Expr intersect(final Pos pos) {
    return get(Math.max(min, pos.min), Math.min(max, pos.max));
  }

  /**
   * Creates a union of the existing and the specified position expressions.
   * @param pos second position expression
   * @return resulting expression
   */
  Expr union(final Pos pos) {
    return get(Math.min(min, pos.min), Math.max(max, pos.max));
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.POS || u == Use.ELM;
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, MIN, Token.token(min), MAX,
        max == Long.MAX_VALUE ? INF : Token.token(max));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("position() ");
    if(max == Long.MAX_VALUE) sb.append('>');
    sb.append("= " + min);
    if(max != Long.MAX_VALUE && min != max) sb.append(" to " + max);
    return sb.toString();
  }
}
