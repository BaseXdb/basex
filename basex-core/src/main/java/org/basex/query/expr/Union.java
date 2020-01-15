package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
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
 * Union expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Union extends Set {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Union(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);

    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr == Empty.VALUE) {
        // remove empty operands
        // example: * union ()  ->  *
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else if(expr.seqType().instanceOf(SeqType.NOD_ZM) && !expr.has(Flag.CNS, Flag.NDT) &&
         ((Checks<Expr>) ex -> ex.equals(expr)).any(list)) {
        // remove duplicate
        // example: * union *  ->  *
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    exprs = list.finish();

    switch(exprs.length) {
      case 0:  return Empty.VALUE;
      case 1:  return ddo ? exprs[0] : cc.function(Function._UTIL_DDO, info, exprs[0]);
      default: return merge(cc) ? cc.replaceWith(this, exprs[0]) : this;
    }
  }

  /**
   * Tries to merge steps with identical axes and nodes types to a single step.
   * @param cc compilation context
   * @return success flag
   * @throws QueryException query exception
   */
  private boolean merge(final CompileContext cc) throws QueryException {
    Expr root = null;
    Axis axis = null;
    Expr[] preds = null;
    final ArrayList<Test> tests = new ArrayList<>();

    for(final Expr expr : exprs) {
      if(!(expr instanceof Path)) return false;
      final Path path = (Path) expr;
      if(path.steps.length > 1) return false;
      if(root != null ? !Objects.equals(root, path.root) :
        path.root != null && path.root.has(Flag.CNS, Flag.NDT, Flag.POS)) return false;

      root = path.root;

      final Step step = (Step) path.steps[0];
      if(axis != null && axis != step.axis || step.has(Flag.CNS, Flag.NDT, Flag.POS)) return false;
      if(preds != null && !Arrays.equals(preds, step.exprs)) return false;

      axis = step.axis;
      preds = step.exprs;
      tests.add(step.test);
    }

    final Test test = UnionTest.get(tests.toArray(new Test[0]));
    if(test == null) return false;

    exprs[0] = Path.get(info, root, Step.get(info, axis, test, preds)).optimize(cc);
    return true;
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    final ANodeBuilder nodes = new ANodeBuilder();
    for(final Expr expr : exprs) {
      final Iter iter = expr.iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) nodes.add(toNode(item));
    }
    return nodes.value(this);
  }

  @Override
  protected NodeIter iterate(final QueryContext qc) throws QueryException {
    return new SetIter(qc, iters(qc)) {
      @Override
      public ANode next() throws QueryException {
        if(nodes == null) {
          final int il = iter.length;
          nodes = new ANode[il];
          for(int i = 0; i < il; i++) next(i);
        }

        int m = -1;
        final int il = nodes.length;
        for(int i = 0; i < il; i++) {
          if(nodes[i] == null) continue;
          final int d = m == -1 ? 1 : nodes[m].diff(nodes[i]);
          if(d == 0) {
            next(i--);
          } else if(d > 0) {
            m = i;
          }
        }
        if(m == -1) return null;

        final ANode node = nodes[m];
        next(m);
        return node;
      }
    };
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Union un = new Union(info, copyAll(cc, vm, exprs));
    un.ddo = ddo;
    return copyType(un);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Union && super.equals(obj);
  }
}
