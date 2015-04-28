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
 * @author BaseX Team 2005-15, BSD License
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
        final Value val = rt.value();
        iter = val.iter();
        sz = val.size();
      }
    } else {
      final Value rt = ctxValue(qc);
      iter = rt.iter();
      sz = rt.size();
    }

    final Value cv = qc.value;
    final long cs = qc.size;
    final long cp = qc.pos;
    try {
      // loop through all expressions
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr step = steps[s];
        final ItemList cache = new ItemList();

        // map operator: don't remove duplicates and check for nodes
        qc.size = sz;
        qc.pos = 1;

        // loop through all input items
        long nodes = 0;
        for(Item it; (it = iter.next()) != null;) {
          if(!(it instanceof ANode)) throw PATHNODE_X_X_X.get(info, step, it.type, it);
          qc.value = it;

          // loop through all resulting items
          final Iter ir = qc.iter(step);
          for(Item i; (i = ir.next()) != null;) {
            if(i instanceof ANode) nodes++;
            cache.add(i);
          }
          qc.pos++;
        }

        final long vs = cache.size();
        if(nodes < vs) {
          // check if both nodes and atomic values occur in last result
          if(nodes > 0) throw EVALNODESVALS.get(info);
          // check if input for next axis step consists items other than nodes
          if(s + 1 < sl) {
            final Item it = cache.get(0);
            throw PATHNODE_X_X_X.get(info, steps[s + 1], it.type, it);
          }
        }

        if(nodes == vs) {
          // remove potential duplicates from node sets
          final ANodeList list = new ANodeList().check();
          for(final Item nd : cache) list.add((ANode) nd);
          iter = list.iter();
        } else {
          iter = cache.iter();
        }
      }
      return iter;
    } finally {
      qc.value = cv;
      qc.size = cs;
      qc.pos = cp;
    }
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new MixedPath(info, root == null ? null : root.copy(qc, scp, vs),
        Arr.copyAll(qc, scp, vs, steps));
  }
}
