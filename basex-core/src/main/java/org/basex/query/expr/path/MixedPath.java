package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Mixed path expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
    super(info, AtomType.ITEM, root, steps);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value rt = root != null ? root.value(qc) : ctxValue(qc);
    Iter iter = rt.iter(qc);
    long size = iter.size();

    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;
    try {
      // loop through all expressions
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        // set context position and size
        qf.size = size;
        qf.pos = 0;

        // loop through all input items; cache nodes and items
        final ANodeBuilder nodes = new ANodeBuilder();
        final ValueBuilder items = new ValueBuilder(qc);
        final Expr step = steps[s];
        for(Item item; (item = iter.next()) != null;) {
          if(!(item instanceof ANode)) throw PATHNODE_X_X_X.get(info, step, item.type, item);
          qf.value = item;
          qf.pos++;
          // loop through all resulting items
          final Iter ir = step.iter(qc);
          for(Item it; (it = qc.next(ir)) != null;) {
            if(it instanceof ANode) nodes.add((ANode) it);
            else items.add(it);
          }
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
      qc.focus = focus;
    }
    return iter;
  }

  @Override
  public boolean ddo() {
    return seqType().type instanceof NodeType;
  }

  @Override
  public Data data() {
    return steps[steps.length - 1].data();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public boolean vacuous() {
    return steps[steps.length - 1].vacuous();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new MixedPath(info, root == null ? null : root.copy(cc, vm),
        Arr.copyAll(cc, vm, steps)));
  }
}
