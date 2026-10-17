package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Axis path expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CachedPath extends AxisPath {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  CachedPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  @Override
  protected Iter iterator(final QueryContext qc) throws QueryException {
    return nodes(qc).iter();
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    final QueryFocus focus = qc.focus;
    final Value rt = root != null ? root.value(qc) : focus.value;
    if(rt instanceof final DBNodeSeq seq) {
      final Step[] stps = parentSteps();
      final Value nodes = stps != null ? seq.steps(stps, this, qc) : null;
      if(nodes != null) return nodes;
    }

    final GNodeBuilder list = new GNodeBuilder();
    final QueryFocus qf = new QueryFocus();
    qc.focus = qf;
    try {
      if(rt != null) {
        final Iter iter = rt.iter(qc);
        for(Item item; (item = iter.next()) != null;) {
          qf.value = item;
          iterate(0, list, qc, qf);
        }
      } else {
        iterate(0, list, qc, qf);
      }
    } finally {
      qc.focus = focus;
    }
    return list.value(this);
  }

  /**
   * Returns the steps if they are self and parent steps without predicates.
   * @return steps, or {@code null} if other steps exist
   */
  private Step[] parentSteps() {
    final int sl = steps.length;
    final Step[] stps = new Step[sl];
    for(int s = 0; s < sl; s++) {
      if(!(steps[s] instanceof final Step step) || step.exprs.length != 0 ||
          step.selector != null || !step.axis.oneOf(Axis.SELF, Axis.PARENT)) return null;
      stps[s] = step;
    }
    return stps;
  }

  /**
   * Recursive step iterator.
   * @param step current step
   * @param list node cache
   * @param qc query context
   * @param qf query focus
   * @throws QueryException query exception
   */
  private void iterate(final int step, final GNodeBuilder list, final QueryContext qc,
      final QueryFocus qf) throws QueryException {

    // cast is safe (steps will always return a {@link NodeIter} instance)
    final NodeIter ni = (NodeIter) steps[step].iter(qc);
    if(step + 1 == steps.length) {
      for(GNode node; (node = ni.next()) != null;) {
        list.add(node);
      }
    } else {
      for(GNode node; (node = ni.next()) != null;) {
        qf.value = node;
        iterate(step + 1, list, qc, qf);
      }
    }
  }

  @Override
  public AxisPath copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyCache(new CachedPath(info, rt, Arr.copyAll(cc, vm, steps)));
  }
}
