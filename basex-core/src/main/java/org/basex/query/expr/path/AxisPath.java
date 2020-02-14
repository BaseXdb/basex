package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.PathCache.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract axis path expression.
 *
 * @author BaseX Team 2005-20, BSD License
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

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Value result = cache(qc);
    return result != null ? result.iter() : iterator(qc);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Value result = cache(qc);
    return result != null ? result : nodes(qc);
  }

  /**
   * Updates the cache and returns a cached value.
   * @param qc query context
   * @return cached value or {@code null}
   * @throws QueryException query context
   */
  private Value cache(final QueryContext qc) throws QueryException {
    final PathCache cache = qc.threads.get(this).get();
    switch(cache.state) {
      case INIT:
        // first invocation: initialize caching flag
        cache.state = !hasFreeVars() && !has(Flag.NDT) ? State.ENABLED : State.DISABLED;
        return cache(qc);
      case ENABLED:
        // second invocation, caching is enabled: cache context value (copy light-weight db nodes)
        final Value value = qc.focus.value;
        cache.initial = value instanceof DBNode ? ((DBNode) value).finish() : value;
        cache.state = State.READY;
        break;
      case READY:
        // third invocation, ready for caching: cache result if context has not changed
        if(cache.sameContext(qc.focus.value, root)) {
          cache.result = iterator(qc).value(qc, this);
          cache.state = State.CACHED;
        } else {
          // disable caching if context has changed
          cache.state = State.DISABLED;
        }
        break;
      case CACHED:
        // further invocations, result is cached: disable caching if context has changed
        if(!cache.sameContext(qc.focus.value, root)) {
          cache.result = null;
          cache.state = State.DISABLED;
        }
        break;
      case DISABLED:
    }
    return cache.result;
  }

  /**
   * Returns a node iterator.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  protected abstract Iter iterator(QueryContext qc) throws QueryException;

  /**
   * Returns a node sequence.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  protected abstract Value nodes(QueryContext qc) throws QueryException;

  /**
   * Returns the specified axis step.
   * @param index index
   * @return step
   */
  public final Step step(final int index) {
    return (Step) steps[index];
  }

  /**
   * Adds predicates to the last step.
   * @param preds predicates to be added
   * @return resulting path instance
   */
  public final ParseExpr addPredicates(final Expr... preds) {
    final int sl = steps.length - 1;
    steps[sl] = step(sl).addPreds(preds);
    return copyType(get(info, root, steps));
  }

  @Override
  public final boolean ddo() {
    return true;
  }
}
