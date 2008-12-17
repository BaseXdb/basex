package org.basex.query.xpath.path;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.Pos;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.item.NodeBuilder;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Position predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PredPos extends Pred {
  /** Minimum value. */
  int min;
  /** Maximum value. */
  int max;

  /**
   * Constructor.
   * @param mn minimum value
   * @param mx maximum value
   */
  public PredPos(final int mn, final int mx) {
    min = Math.max(1, mn);
    max = mx;
  }

  /**
   * Creates a position predicate or a <code>null</code> reference.
   * @param e expression to be tested
   * @return comparator
   */
  static Pred get(final Expr e) {
    if(!(e instanceof Pos)) return null;
    final int mn = ((Pos) e).min;
    final int mx = ((Pos) e).max;
    return mx < mn ? new PredSimple(Bln.FALSE) : new PredPos(mn, mx);
  }

  @Override
  NodeBuilder eval(final XPContext ctx, final NodeBuilder set) {
    final int[] nodes = set.nodes;
    final NodeBuilder tmp = new NodeBuilder();
    final int mn = Math.max(min - 1, 0);
    final int mx = Math.min(max, set.size);

    for(int n = mn; n < mx; n++) tmp.add(nodes[n]);
    return tmp;
  }

  @Override
  boolean eval(final XPContext ctx, final Nod nodes, final int pos) {
    more = pos < max;
    return pos >= min && pos <= max;
  }

  @Override
  boolean usesSize() {
    return false;
  }

  @Override
  boolean usesPos() {
    return true;
  }

  @Override
  double posPred() {
    return min <= 0 ? -1 : min != max ? 0 : min;
  }

  @Override
  Pred compile(final XPContext ctx) {
    return this;
  }

  @Override
  boolean alwaysFalse() {
    return false;
  }

  @Override
  boolean alwaysTrue() {
    return false;
  }

  @Override
  public boolean sameAs(final Pred pred) {
    if(!(pred instanceof PredPos)) return false;
    final PredPos p = (PredPos) pred;
    return p.min == min && p.max == max;
  }

  @Override
  public String toString() {
    return new TokenBuilder("[pos ").add(min == max ? "= " + min :
      max == Integer.MAX_VALUE ? ">= " + min :
      "= " + min + "-" + max).add("]").toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, Token.token("min"), Token.token(min),
        Token.token("max"), Token.token(max));
  }
}
