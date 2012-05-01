package org.basex.query.util;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.path.*;
import org.basex.util.*;

/**
 * Container for all information needed to determine whether an index is
 * accessible or not.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class IndexContext {
  /** Query context. */
  public final QueryContext ctx;
  /** Data reference. */
  public final Data data;
  /** Index Step. */
  public final AxisStep step;
  /** Flag for iterative evaluation. */
  public final boolean iterable;

  /** Costs of index access: smaller is better, 0 means no results. */
  private int costs;
  /** Flag for ftnot expressions. */
  public boolean not;
  /** Flag for sequential processing. */
  public boolean seq;

  /**
   * Constructor.
   * @param c query context
   * @param d data reference
   * @param s index step
   * @param i iterable flag
   */
  public IndexContext(final QueryContext c, final Data d, final AxisStep s,
      final boolean i) {
    ctx = c;
    data = d;
    step = s;
    iterable = i;
  }

  /**
   * Rewrites the specified expression for index access.
   * @param ex expression to be rewritten
   * @param root new root expression
   * @param text text flag
   * @return index access
   */
  public Expr invert(final Expr ex, final ParseExpr root, final boolean text) {
    // handle context node
    if(ex instanceof Context) {
      if(text) return root;
      // add attribute step
      if(step.test.name == null) return root;
      return Path.get(root.info, root, AxisStep.get(step.info, Axis.SELF, step.test));
    }

    final AxisPath orig = (AxisPath) ex;
    final AxisPath path = orig.invertPath(root, step);
    if(!text) {
      // add attribute step
      final AxisStep s = orig.step(orig.steps.length - 1);
      if(s.test.name != null) {
        Expr[] steps = { AxisStep.get(s.info, Axis.SELF, s.test) };
        for(final Expr e : path.steps) steps = Array.add(steps, e);
        path.steps = steps;
      }
    }
    return path;
  }

  /**
   * Adds the estimated costs.
   * @param c cost to be added
   */
  public void addCosts(final int c) {
    costs = Math.max(1, costs + c);
  }

  /**
   * Sets the estimated costs.
   * @param c cost to be added
   */
  public void costs(final int c) {
    costs = c;
  }

  /**
   * Returns the estimated costs.
   * @return costs
   */
  public int costs() {
    return costs;
  }
}
