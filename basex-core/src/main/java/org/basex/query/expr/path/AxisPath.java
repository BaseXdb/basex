package org.basex.query.expr.path;

import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Abstract axis path expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class AxisPath extends Path {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  AxisPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  /**
   * Inverts a location path.
   * @param rt new root node
   * @param curr current location step
   * @return inverted path
   */
  public final Path invertPath(final Expr rt, final Step curr) {
    // add predicates of last step to new root node
    int s = steps.length - 1;
    final Expr r = step(s).preds.length == 0 ? rt : Filter.get(info, rt, step(s).preds);

    // add inverted steps in a backward manner
    final ExprList stps = new ExprList();
    while(--s >= 0) {
      stps.add(Step.get(info, step(s + 1).axis.invert(), step(s).test, step(s).preds));
    }
    stps.add(Step.get(info, step(s + 1).axis.invert(), curr.test));
    return Path.get(info, r, stps.finish());
  }

  /**
   * Returns the specified axis step.
   * @param i index
   * @return step
   */
  public final Step step(final int i) {
    return (Step) steps[i];
  }

  @Override
  public final boolean iterable() {
    return true;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisPath)) return false;
    final AxisPath ap = (AxisPath) cmp;
    if((root == null || ap.root == null) && root != ap.root ||
        steps.length != ap.steps.length || root != null && !root.sameAs(ap.root)) return false;

    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      if(!steps[s].sameAs(ap.steps[s])) return false;
    }
    return true;
  }
}
