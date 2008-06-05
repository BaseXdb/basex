package org.basex.query.xpath.expr;

import org.basex.index.Index;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;

/**
 * IndexRange, performing index-based numeric range queries.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IndexRange extends ArrayExpr {
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
  public IndexRange(final Expr[] e, final Item mn, final boolean mninclude,
      final Item mx, final boolean mxinclude) {
    exprs = e;
    min = mn; //.num();
    max = mx; //.num();
    mni = mninclude;
    mxi = mxinclude;
  }

  @Override
  public Expr compile(final XPContext ctx) {
    return this;
  }
  
  @Override
  public NodeSet eval(final XPContext ctx) {
    final int[] res = ctx.local.data.idRange(Index.TYPE.TXT,
        min.num(), mni, max.num(), mxi);
    return new NodeSet(res, ctx);
  }

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
