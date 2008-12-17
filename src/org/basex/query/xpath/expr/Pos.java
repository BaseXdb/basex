package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Comp;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Position test.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Pos extends Expr {
  /** Minimum value. */
  public int min;
  /** Maximum value. */
  public int max;

  /**
   * Constructor.
   * @param mn minimum
   * @param mx maximum
   */
  Pos(final int mn, final int mx) {
    min = mn;
    max = mx;
  }
  
  /**
   * Creates a position predicate, or a false predicate if the minimum
   * is greater than the maximum value.
   * @param mn minimum
   * @param mx maximum
   * @return predicate
   */
  private static Expr get(final double mn, final double mx) {
    return mx < mn ? Bln.FALSE : new Pos((int) mn, (int) mx);
  }
  
  /**
   * Creates a position predicate, a false predicate for impossible comparisons
   * or a <code>null</code> reference.
   * @param val position value
   * @param type comparison type
   * @return predicate
   */
  public static Expr get(final double val, final Comp type) {
    if(type == Comp.EQ) {
      if(val != (int) val || val < 1) return Bln.FALSE;
      return get(val, val);
    }
    if(type == Comp.GT) return get(val + 1, Integer.MAX_VALUE);
    if(type == Comp.GE) return get(Math.ceil(val), Integer.MAX_VALUE);
    if(type == Comp.LT) return get(1, Math.ceil(val) - 1);
    if(type == Comp.LE) return get(1, val);
    return null;
  }

  @Override
  public Expr comp(final XPContext ctx) {
    return this;
  }

  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    int pos = ctx.item.currPos;
    if(pos == 0) throw new QueryException(INVALIDPOS);
    return Bln.get(pos >= min && pos <= max);
  }

  @Override
  public boolean usesPos() {
    return true;
  }

  @Override
  public boolean usesSize() {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, Token.token("min"), Token.token(min),
        Token.token("max"), Token.token(max));
  }

  @Override
  public String toString() {
    return new TokenBuilder("pos ").add(min == max ? "= " + min :
      max == Integer.MAX_VALUE ? ">= " + min :
      "= " + min + "-" + max).toString();
  }
}
