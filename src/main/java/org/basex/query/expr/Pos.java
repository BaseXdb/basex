package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CmpV.Op;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Pos expression.
 *
 * @author BaseX Team 2005-11, BSD License
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
   * @param ii input info
   */
  private Pos(final long mn, final long mx, final InputInfo ii) {
    super(ii);
    min = mn;
    max = mx;
    type = SeqType.BLN;
  }

  /**
   * Returns a position expression, or an optimized boolean item.
   * @param mn minimum value
   * @param mx minimum value
   * @param ii input info
   * @return expression
   */
  public static Expr get(final long mn, final long mx, final InputInfo ii) {
    // suppose that positions always fit in long values..
    return mn > mx || mx < 1 ? Bln.FALSE : mn <= 1 && mx == Long.MAX_VALUE ?
      Bln.TRUE : new Pos(mn, mx, ii);
  }

  /**
   * Returns an instance of this class, if possible, and the input expression
   * otherwise.
   * @param cmp comparator
   * @param a argument
   * @param o original expression
   * @param ii input info
   * @return resulting expression, or {@code null}
   * @throws QueryException query exception
   */
  public static Expr get(final Op cmp, final Expr a, final Expr o,
      final InputInfo ii) throws QueryException {

    if(a.isItem()) {
      final Item it = (Item) a;
      if(it.type.isNumber()) {
        final long p = it.itr(ii);
        final boolean ex = p == it.dbl(ii);
        switch(cmp) {
          case EQ: return ex ? get(p, p, ii) : Bln.FALSE;
          case GE: return get(ex ? p : p + 1, Long.MAX_VALUE, ii);
          case GT: return get(p + 1, Long.MAX_VALUE, ii);
          case LE: return get(1, p, ii);
          case LT: return get(1, ex ? p - 1 : p, ii);
          default:
        }
      }
    }
    return o;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    checkCtx(ctx);
    return Bln.get(ctx.pos >= min && ctx.pos <= max);
  }

  /**
   * Returns false if no more results can be expected.
   * @param ctx query context
   * @return result of check
   */
  public boolean skip(final QueryContext ctx) {
    return ctx.pos >= max;
  }

  /**
   * Creates an intersection of the existing and the specified position
   * expressions.
   * @param pos second position expression
   * @param ii input info
   * @return resulting expression
   */
  Expr intersect(final Pos pos, final InputInfo ii) {
    return get(Math.max(min, pos.min), Math.min(max, pos.max), ii);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.POS;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Pos)) return false;
    final Pos p = (Pos) cmp;
    return min == p.min && max == p.max;
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
