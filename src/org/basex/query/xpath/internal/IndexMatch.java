package org.basex.query.xpath.internal;

import java.util.Arrays;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.TokenBuilder;

/**
 * This class is only internally used for index optimizations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public final class IndexMatch extends InternalExpr {
  /** LocationPath that contains the index reference nodes. */
  private final LocPath path;
  /** Expression that finds nodes (using the index). */
  private final Expr expr;
  /** Expression that matches the result set. */
  private final LocPath match;

  /**
   * Constructor.
   * @param p input path
   * @param exp expression that uses the index
   * @param m matching path
   */
  public IndexMatch(final LocPath p, final Expr exp, final LocPath m) {
    path = p;
    expr = exp;
    match = m;
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    // evaluate standard path
    final NodeSet loc = path.eval(ctx);
    if(loc.size == 0) return loc;

    // evaluate optimized expression
    final NodeBuilder result = new NodeBuilder();

    // match rest of path
    final NodeSet tmp = new NodeSet(ctx);
    for(final int res : ((NodeSet) ctx.eval(expr)).nodes) {
      tmp.set(res);
      if(found(loc.nodes, match.eval(ctx).nodes)) result.add(res);
    }
    return new NodeSet(result.finish(), ctx, ctx.local.ftidpos, 
        ctx.local.ftpointer);
  }

  /**
   * Returns true if one of the nodes in the nodesets is equal.
   * @param nodes first node set
   * @param loc second node set
   * @return result of check
   */
  private boolean found(final int[] nodes, final int[] loc) {
    for(final int node : nodes) {
      if(Arrays.binarySearch(loc, node) >= 0) return true;
    }
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    path.plan(ser);
    expr.plan(ser);
    match.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder();
    sb.add(name());
    sb.add("(");
    if(path.steps.size() != 0) sb.add(path + ", ");
    sb.add(expr + "[");
    sb.add(match.toString() + "])");
    return sb.toString();
  }
}
