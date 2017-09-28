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
 * @author BaseX Team 2005-17, BSD License
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
  public boolean isVacuous() {
    return steps[steps.length - 1].isVacuous();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    Iter iter;
    long sz;
    if(root != null) {
      final Iter rt = qc.iter(root);
      final long s = rt.size();
      if(s >= 0) {
        iter = rt;
        sz = s;
      } else {
        final Value val = rt.value(qc);
        iter = val.iter();
        sz = val.size();
      }
    } else {
      final Value rt = ctxValue(qc);
      iter = rt.iter();
      sz = rt.size();
    }

    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    qc.focus = focus;
    try {
      // loop through all expressions
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        // set context position and size
        focus.size = sz;
        focus.pos = 1;

        // loop through all input items; cache nodes and items
        final ANodeBuilder nodes = new ANodeBuilder();
        final ItemList items = new ItemList();
        final Expr step = steps[s];
        for(Item it; (it = iter.next()) != null;) {
          if(!(it instanceof ANode)) throw PATHNODE_X_X_X.get(info, step, it.type, it);
          focus.value = it;

          // loop through all resulting items
          final Iter ir = qc.iter(step);
          for(Item i; (i = ir.next()) != null;) {
            qc.checkStop();
            if(i instanceof ANode) nodes.add((ANode) i);
            else items.add(i);
          }
          focus.pos++;
        }

        if(items.isEmpty()) {
          // all results are nodes: create new iterator
          iter = nodes.iter();
        } else {
          // raise error if this is not the final result
          if(s + 1 < sl)
            throw PATHNODE_X_X_X.get(info, steps[s + 1], items.get(0).type, items.get(0));
          // result contains non-nodes: raise error if result any contains nodes
          if(!nodes.isEmpty()) throw MIXEDRESULTS.get(info);
          iter = items.iter();
        }
        sz = iter.size();
      }
      return iter;
    } finally {
      qc.focus = qf;
    }
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new MixedPath(info, root == null ? null : root.copy(cc, vm),
        Arr.copyAll(cc, vm, steps));
  }
}
