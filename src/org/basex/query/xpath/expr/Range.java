package org.basex.query.xpath.expr;


import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Array;

/**
 * RangeExpr Able to perform numeric range queries.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Range extends ArrayExpr {
  /** Expression. */
  //final Expr expr;
  /** Minimum Value. */
  final Item min;
  /** Maximum Value. */
  final Item max;
  /** Minimum Value included in range. */
  final boolean mni;
  /** Minimum Value included in range. */
  final boolean mxi;

  /**
   * Constructor.
   * @param e first expression
   * @param mn minimum value
   * @param mninclude minimum included in range
   * @param mx maximum value
   * @param mxinclude maximum included in range
   */
  public Range(final Expr[] e, final Item mn, final boolean mninclude,
      final Item mx, final boolean mxinclude) {
    exprs = e;
    min = mn; //.num();
    max = mx; //.num();
    mni = mninclude;
    mxi = mxinclude;
  }

  @Override
  public Expr compile(final XPContext ctx) {
    /*for(int i = 0; i != exprs.length; i++) {
      exprs[i] = exprs[i].compile(ctx);
    }*/
    return this;
  }
  
  @Override
  public NodeSet eval(final XPContext ctx) {
    int[][] res = ctx.local.data.ftIDRange(min.str(), mni, max.str(), mxi);
    if (res != null) {
      return new NodeSet(Array.extractIDsFromData(res), ctx, res);
    } else {
      return null;
    }

    /*
    final Item v = ctx.eval(exprs[0]);
    if(v.size() == 0) return Bool.FALSE;

    if(v instanceof NodeSet) {
      final NodeSet nodes = (NodeSet) v;
      final Data data = nodes.data;
      for(int n = 0; n < nodes.size; n++) {
        final double d = data.atomNum(nodes.nodes[n]);
        
        if(((d >= min && mni) || (d > min && !mni)) && 
            ((d <= max && mxi) || (d < max && !mxi))) return Bool.TRUE;
      }
      return Bool.FALSE;
    }
    final double d = v.num();
    return Bool.get(d >= min && d <= max);*/
  }

  /*
  @Override
  public String toString() {
    return "Range(" + min + (mni ? " <= " : "<") + expr 
      + (mxi ? " >= " : ">")  + max + ")";
  }*/

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr) {
    final LocPath path = (LocPath) ((Comparison) exprs[0]).expr1;
    return new Path(this, path.invertPath(curr));
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int mini) {
    return 1;
  }
}
