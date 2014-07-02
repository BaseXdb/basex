package org.basex.query.util;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.path.*;
import org.basex.query.path.Test.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class contains methods for analyzing the costs of index requests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IndexCosts {
  /** Query context. */
  public final QueryContext ctx;
  /** Index context. */
  public final IndexContext ictx;
  /** Step with predicate that can be rewritten for index access. */
  public final Step step;

  /** Costs of index access: smaller is better, 0 means no results. */
  private int costs;
  /** Flag for ftnot expressions. */
  public boolean not;
  /** Flag for sequential processing. */
  public boolean seq;

  /**
   * Constructor.
   * @param ictx index context
   * @param ctx query context
   * @param step index step
   */
  public IndexCosts(final IndexContext ictx, final QueryContext ctx, final Step step) {
    this.ctx = ctx;
    this.ictx = ictx;
    this.step = step;
  }

  /**
   * Rewrites the specified expression for index access.
   * @param expr expression to be rewritten ({@link Context} or {@link AxisPath} instance)
   * @param root new root expression
   * @param text text flag
   * @return index access
   */
  public ParseExpr invert(final Expr expr, final ParseExpr root, final boolean text) {
    // handle context node
    if(expr instanceof Context) {
      // add attribute step
      if(text || step.test.name == null) return root;
      final Step as = Step.get(step.info, Axis.SELF, step.test);
      return Path.get(root.info, root, as);
    }

    final AxisPath orig = (AxisPath) expr;
    final Path path = orig.invertPath(root, step);

    if(!text) {
      // add attribute step
      final Step attr = orig.step(orig.steps.length - 1);
      if(attr.test.name != null) {
        final ExprList steps = new ExprList(path.steps.length + 1);
        steps.add(Step.get(attr.info, Axis.SELF, attr.test)).add(path.steps);
        return Path.get(path.info, path.root, steps.finish());
      }
    }
    return path;
  }

  /**
   * Returns the step below which all steps need to be inverted.
   * @param expr expression
   * @return location step, or {@code null}
   */
  public Step indexStep(final Expr expr) {
    // context reference: return index step
    if(expr instanceof Context) return step;
    // check if index can be applied
    if(!(expr instanceof AxisPath)) return null;
    // accept only single axis steps as first expression
    final AxisPath path = (AxisPath) expr;
    // path must contain no root node
    if(path.root != null) return null;
    // return last step
    return path.step(path.steps.length - 1);
  }

  /**
   * Adds the estimated costs.
   * @param cost cost to be added
   */
  public void addCosts(final int cost) {
    costs = Math.max(1, costs + cost);
  }

  /**
   * Sets the estimated costs.
   * @param cost cost to be set
   */
  public void costs(final int cost) {
    costs = cost;
  }

  /**
   * Returns the estimated costs.
   * @return costs
   */
  public int costs() {
    return costs;
  }

  /*
  public final Expr elem(final Step s) {
    if(s.test.type == NodeType.ATT ||
        s.test.kind != Kind.NAME && s.test.kind != Kind.URI_NAME) return this;

    final Data data = ctx.data();
    if(data == null || !data.meta.uptodate) return this;

    final Stats stats = data.tagindex.stat(data.tagindex.id(s.test.name.local()));
    if(stats != null && stats.isLeaf()) {
      steps = Array.add(steps, Step.get(info, Axis.CHILD, Test.TXT));
      ctx.compInfo(OPTTEXT, this);
    }
    return this;
  }*/
}
