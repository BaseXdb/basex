package org.basex.query.xpath.internal;

import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;

/**
 * IndexRange, performing numeric range queries.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Range extends InternalExpr {
  /** Expression. */
  final Expr expr;
  /** Minimum Value. */
  final double min;
  /** Maximum Value. */
  final double max;

  /**
   * Constructor.
   * @param e first expression
   * @param mn minimum value
   * @param mx maximum value
   */
  public Range(final Expr e, final Item mn,
      final Item mx) {
    expr = e;
    min = mn.num();
    max = mx.num();
  }

  @Override
  public Bool eval(final XPContext ctx)
      throws QueryException {

    final Item v = ctx.eval(expr);
    if(v.size() == 0) return Bool.FALSE;

    if(v instanceof NodeSet) {
      final NodeSet nodes = (NodeSet) v;
      final Data data = nodes.data;
      for(int n = 0; n < nodes.size; n++) {
        final double d = data.atomNum(nodes.nodes[n]);
        if(d >= min && d <= max) return Bool.TRUE;
      }
      return Bool.FALSE;
    }
    final double d = v.num();
    return Bool.get(d >= min && d <= max);
  }
  
  @Override
  public String toString() {
    return "Range(" + min + " <= " + expr + " <= " + max + ")";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, Token.token("min"), Token.token(min),
        Token.token("max"), Token.token(max));
    expr.plan(ser);
    ser.closeElement(this);
  }
}
