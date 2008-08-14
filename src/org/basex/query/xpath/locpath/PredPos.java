package org.basex.query.xpath.locpath;

import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;

/**
 * Position predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PredPos extends Pred {
  /** Minimum value. */
  final int min;
  /** Maximum value. */
  final int max;

  /**
   * Constructor.
   * @param mn minimum value
   * @param mx maximum value
   */
  PredPos(final int mn, final int mx) {
    min = mn;
    max = mx;
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
  boolean eval(final XPContext ctx, final NodeSet nodes,
      final int pos) {
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
  int posPred() {
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
  public Expr indexEquivalent(final XPContext ctx, final Step step, 
      final boolean seq) {
    return null;
  }

  @Override
  public int indexSizes(final XPContext r, final Step c, final int m) {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean sameAs(final Pred pred) {
    if(!(pred instanceof PredPos)) return false;
    final PredPos p = (PredPos) pred;
    return p.min == min && p.max == max;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("[pos ");
    sb.append(min != max ? max == Integer.MAX_VALUE ? "> 2" : "= " +
        min + " - " + max : "= " + min);
    return sb.append("]").toString();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, Token.token("min"), Token.token(min),
        Token.token("max"), Token.token(max));
  }
}
