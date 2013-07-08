package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.path.*;
import org.basex.util.*;

/**
 * This class contains methods for analyzing the costs of index requests.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class IndexCosts {
  /** Query context. */
  public final QueryContext ctx;
  /** Index context. */
  public final IndexContext ictx;
  /** Index Step. */
  public final Step step;

  /** Costs of index access: smaller is better, 0 means no results. */
  private int costs;
  /** Flag for ftnot expressions. */
  public boolean not;
  /** Flag for sequential processing. */
  public boolean seq;

  /**
   * Constructor.
   * @param ic index context
   * @param c query context
   * @param s index step
   */
  public IndexCosts(final IndexContext ic, final QueryContext c, final Step s) {
    ctx = c;
    ictx = ic;
    step = s;
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
      final Step as = Step.get(step.info, Axis.SELF, step.test);
      return Path.get(root.info, root, as);
    }

    final AxisPath orig = (AxisPath) ex;
    final AxisPath path = orig.invertPath(root, step);
    if(!text) {
      // add attribute step
      final Step s = orig.step(orig.steps.length - 1);
      if(s.test.name != null) {
        Expr[] steps = { Step.get(s.info, Axis.SELF, s.test) };
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
