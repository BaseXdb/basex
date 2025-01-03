package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract axis path expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class AxisPath extends Path {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  AxisPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, NodeType.NODE, root, steps);
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Value cached = cache(qc);
    return cached != null ? cached.iter() : iterator(qc);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Value cached = cache(qc);
    return cached != null ? cached : nodes(qc);
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Value cached = cache(qc);
    return cached != null ? !cached.isEmpty() : iterator(qc).next() != null;
  }

  /**
   * Updates the cache and returns a cached value.
   * @param qc query context
   * @return cached value or {@code null}
   * @throws QueryException query context
   */
  private Value cache(final QueryContext qc) throws QueryException {
    final Value value = qc.focus.value;
    if(root == null && value != null && value.isEmpty()) return value;

    final PathCache cache = qc.threads.get(this).get();
    switch(cache.state) {
      case INIT:
        // first invocation: find out if caching is possible
        cache.init(value, this);
        break;
      case ENABLED:
        // second invocation (ready for caching): cache result
        if(cache.valid(value)) {
          cache.cache(nodes(qc));
        } else {
          // disable caching otherwise (expected to change frequently)
          cache.disable();
        }
        break;
      case CACHED:
        // further invocations (result is cached): cache again if context has changed
        if(!cache.valid(value)) {
          cache.update(value, nodes(qc));
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
   * Adds predicates to the last step and returns the optimized expression.
   * @param cc compilation context
   * @param preds predicates to be added
   * @return new path
   * @throws QueryException query exception
   */
  public final Expr addPredicates(final CompileContext cc, final Expr... preds)
      throws QueryException {

    final ExprList list = new ExprList(steps);
    final Step step = ((Step) list.pop()).addPredicates(preds);
    list.add(cc.get(step, true, () -> step.optimize(root, cc)));

    exprType.assign(seqType().union(Occ.ZERO));
    return copyType(get(cc, info, root, list.finish()));
  }

  @Override
  public final Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {
    return or && expr instanceof AxisPath ? new Union(info, this, expr).optimize(cc) : null;
  }

  @Override
  public final boolean ddo() {
    return true;
  }
}
