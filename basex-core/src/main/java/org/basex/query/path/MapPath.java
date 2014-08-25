package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative path expression for map paths that do not access the context position or size.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class MapPath extends Path {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param steps axis steps
   */
  MapPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      final boolean r = root != null;
      final int sz = steps.length + (r ? 1 : 0);
      final Expr[] expr = r ? new ExprList(sz).add(root).add(steps).finish() : steps;
      final Iter[] iter = new Iter[sz];
      final Value[] values = new Value[sz];
      int pos = -1;

      @Override
      public Item next() throws QueryException {
        final Value cv = qc.value;
        // local copy of variable (faster)
        int p = pos;
        if(p == -1) {
          values[++p] = cv;
          iter[p] = qc.iter(expr[0]);
        }

        try {
          qc.value = values[p];
          while(true) {
            final Item it = iter[p].next();
            if(it == null) {
              iter[p] = null;
              if(--p == -1) {
                pos = p;
                return null;
              }
            } else if(p < sz - 1) {
              qc.value = it;
              values[++p] = it;
              iter[p] = qc.iter(expr[p]);
            } else {
              pos = p;
              return it;
            }
          }
        } finally {
          qc.value = cv;
        }
      }

      @Override
      public boolean reset() {
        pos = -1;
        return true;
      }
    };
  }

  @Override
  public MapPath copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr rt = root == null ? null : root.copy(qc, scp, vs);
    return copyType(new MapPath(info, rt,  Arr.copyAll(qc, scp, vs, steps)));
  }
}
