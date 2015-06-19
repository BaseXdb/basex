package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract axis path expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class AxisPath extends Path {
  /** Caching states. */
  private enum Caching {
    /** Caching is disabled. */ DISABLED,
    /** Caching is possible. */ ENABLED,
    /** Ready to cache.      */ READY,
    /** Results are cached.  */ CACHED;
  };
  /** Current state. */
  private Caching state;

  /** Cached result. */
  private Value cached;
  /** Cached context value. */
  private Value cvalue;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  AxisPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
    // cache values if expression has no free variables, is deterministic and performs no updates
    state = !hasFreeVars() && !has(Flag.NDT) && !has(Flag.UPD) && !has(Flag.HOF) ?
      Caching.ENABLED : Caching.DISABLED;
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    if(state == Caching.CACHED) {
      // last result was cached
      if(!sameContext(qc)) {
        // disable caching if context has changed (expected to change frequently)
        cached = null;
        state = Caching.DISABLED;
      }
    } else if (state == Caching.READY) {
      // values are ready to cache
      if(sameContext(qc)) {
        cached = nodeIter(qc).value();
        state = Caching.CACHED;
      } else {
        // disable caching if context has changed (expected to change frequently)
        state = Caching.DISABLED;
      }
    } else if(state == Caching.ENABLED) {
      // caching is possible: remember context value
      cvalue = qc.value instanceof DBNode ? ((DBNode) qc.value).copy() : qc.value;
      state = Caching.READY;
    }
    // return new iterator or cached values
    return cached == null ? nodeIter(qc) : cached.iter();
  }

  /**
   * Checks if the specified context value is different to the cached one.
   * @param qc query context
   * @return result of check
   */
  private boolean sameContext(final QueryContext qc) {
    final Value cv = qc.value;
    // context value has not changed...
    if(cv == cvalue && (cv == null || cv.sameAs(cvalue))) return true;
    // otherwise, if path starts with root node, compare roots of cached and new context value
    return root instanceof Root && cv instanceof ANode && cvalue instanceof ANode &&
        ((ANode) cv).root().sameAs(((ANode) cv).root());
  }

  /**
   * Returns a node iterator.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  protected abstract NodeIter nodeIter(final QueryContext qc) throws QueryException;

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
