package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Intersect expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Intersect extends Set {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Intersect(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);

    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // remove empty operands
      if(expr == Empty.VALUE) {
        // example: * intersect ()  ->  ()
        return cc.emptySeq(this);
      } else if(expr.seqType().instanceOf(SeqType.NOD_ZM) && !expr.has(Flag.CNS, Flag.NDT) &&
         ((Checks<Expr>) ex -> ex.equals(expr)).any(list)) {
        // remove duplicate
        // example: * intersect *  ->  *
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    exprs = list.finish();

    // ensure that results are always sorted
    switch(exprs.length) {
      case 0:  return Empty.VALUE;
      case 1:  return ddo ? exprs[0] : cc.function(Function._UTIL_DDO, info, exprs[0]);
      default: return this;
    }
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    ANodeBuilder nodes = new ANodeBuilder();
    Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) nodes.add(toNode(item));

    final int el = exprs.length;
    for(int e = 1; e < el && !nodes.isEmpty(); ++e) {
      nodes.ddo();
      final ANodeBuilder tmp = new ANodeBuilder();
      iter = exprs[e].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        final ANode node = toNode(item);
        if(nodes.contains(node)) tmp.add(node);
      }
      nodes = tmp;
    }
    return nodes.value(this);
  }

  @Override
  protected NodeIter iterate(final QueryContext qc) throws QueryException {
    return new SetIter(qc, iters(qc)) {
      @Override
      public ANode next() throws QueryException {
        final int irl = iter.length;
        if(nodes == null) nodes = new ANode[irl];

        for(int i = 0; i < irl; i++) {
          if(!next(i)) return null;
        }

        final int il = nodes.length;
        for(int i = 1; i < il;) {
          final int d = nodes[0].diff(nodes[i]);
          if(d > 0) {
            if(!next(i)) return null;
          } else if(d < 0) {
            if(!next(0)) return null;
            i = 1;
          } else {
            ++i;
          }
        }
        return nodes[0];
      }
    };
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Intersect is = new Intersect(info, copyAll(cc, vm, exprs));
    is.ddo = ddo;
    return copyType(is);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Intersect && super.equals(obj);
  }
}
