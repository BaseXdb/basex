package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Mixed path expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class MixedPath extends Path {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  MixedPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Expr rt = root != null ? root : ctxValue(qc);
    Iter iter = rt.iter(qc);
    long size = iter.size();
    // if result size is unknown, root is fully evaluated (required for requests on query focus)
    if(size < 0) {
      iter = iter.value(qc, rt).iter();
      size = iter.size();
    }

    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    qc.focus = focus;
    try {
      // loop through all expressions
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        // set context position and size
        focus.size = size;
        focus.pos = 1;

        // loop through all input items; cache nodes and items
        final ANodeBuilder nodes = new ANodeBuilder();
        final ValueBuilder items = new ValueBuilder(qc);
        final Expr step = steps[s];
        for(Item item; (item = iter.next()) != null;) {
          if(!(item instanceof ANode)) throw PATHNODE_X_X_X.get(info, step, item.type, item);
          focus.value = item;

          // loop through all resulting items
          final Iter ir = step.iter(qc);
          for(Item it; (it = qc.next(ir)) != null;) {
            if(it instanceof ANode) nodes.add((ANode) it);
            else items.add(it);
          }
          focus.pos++;
        }

        final Value value = items.value(step);
        if(value.isEmpty()) {
          // all results are nodes: create new iterator
          iter = nodes.value(step).iter();
        } else {
          // raise error if this is not the final result
          if(s + 1 < sl)
            throw PATHNODE_X_X_X.get(info, steps[s + 1], value.itemAt(0).type, value.itemAt(0));
          // result contains non-nodes: raise error if result any contains nodes
          if(!nodes.isEmpty()) throw MIXEDRESULTS.get(info);
          iter = value.iter();
        }
        size = iter.size();
      }
    } finally {
      qc.focus = qf;
    }
    return iter;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public boolean isVacuous() {
    return steps[steps.length - 1].isVacuous();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new MixedPath(info, root == null ? null : root.copy(cc, vm),
        Arr.copyAll(cc, vm, steps)));
  }
}
