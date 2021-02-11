package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Set expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class Set extends Arr {
  /** Flag for iterative evaluation. */
  boolean iterative;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  Set(final InputInfo info, final Expr[] exprs) {
    super(info, SeqType.NODE_ZM, exprs);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    Expr expr = opt(cc);
    if(expr == null) {
      final int el = exprs.length;
      if(el == 0) return Empty.VALUE;
      if(el == 1) return cc.function(Function._UTIL_DDO, info, exprs[0]);
      // try to merge operands
      expr = mergePaths(cc);
      if(expr == null) expr = mergeFilters(cc);
    }
    if(expr != null) return cc.replaceWith(this, expr);

    iterative = ((Checks<Expr>) Expr::ddo).all(exprs);
    return this;
  }

  /**
   * Performs function specific optimizations.
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  abstract Expr opt(CompileContext cc) throws QueryException;

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return iterative ? iterate(qc) : nodes(qc).iter();
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iterative ? iterate(qc).value(qc, this) : nodes(qc);
  }

  /**
   * Creates iterators for all expressions.
   * @param qc query context
   * @return iterators
   * @throws QueryException query exception
   */
  final Iter[] iters(final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    final Iter[] iters = new Iter[el];
    for(int e = 0; e < el; e++) iters[e] = exprs[e].iter(qc);
    return iters;
  }

  /**
   * Evaluates the specified iterators.
   * @param qc query context
   * @return resulting node list
   * @throws QueryException query exception
   */
  abstract Value nodes(QueryContext qc) throws QueryException;

  /**
   * Evaluates the specified iterators in an iterative manner.
   * @param qc query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  abstract Iter iterate(QueryContext qc) throws QueryException;

  /**
   * Tries to merge paths.
   * @param cc compilation context
   * @return merged expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr mergePaths(final CompileContext cc) throws QueryException {
    Expr root = null;
    Axis axis = null;
    Test test = null;
    Expr[] preds = null;
    final ArrayList<Step> steps = new ArrayList<>(exprs.length);

    // collect common root, common axis and steps
    for(final Expr expr : exprs) {
      if(!(expr instanceof Path)) return null;
      final Path path = (Path) expr;
      if(path.steps.length != 1 || !(path.steps[0] instanceof Step)) return null;
      final Step step = (Step) path.steps[0];
      if(steps.isEmpty()) {
        root = path.root;
        axis = step.axis;
        if(root != null && root.has(Flag.CNS, Flag.NDT)) return null;
      } else if(!Objects.equals(root, path.root) || axis != step.axis) {
        // further operands: abort if root or axis differs
        return null;
      }
      steps.add(step);
    }
    final int sl = steps.size();

    // try to merge node tests
    if(this instanceof Union) {
      final ArrayList<Test> tests = new ArrayList<>(sl);
      int s = -1;
      while(++s < sl) {
        final Step step = steps.get(s);
        if(step.mayBePositional()) break;
        if(preds == null) {
          preds = step.exprs;
        } else if(!Arrays.equals(preds, step.exprs)) {
          break;
        }
        tests.add(step.test);
      }
      // all steps were parsed. try to merge tests
      if(s == sl) test = Test.get(tests.toArray(new Test[0]));
    }

    // try to merge first predicates of all steps
    if(test == null) {
      final ExprList list = new ExprList(sl);
      int s = -1;
      while(++s < sl) {
        final Step step = steps.get(s);
        if(step.mayBePositional()) break;
        if(test == null) {
          test = step.test;
        } else if(!test.equals(step.test)) {
          break;
        }
        list.add(newPredicate(step.exprs, cc));
      }
      preds = s == sl ? new Expr[] { mergePredicates(list.finish(), cc).optimize(cc) } : null;
    }
    if(test == null || preds == null) return null;

    final Expr step = Step.get(cc, root, info, axis, test, preds);
    return Path.get(cc, info, root, step);
  }

  /**
   * Tries to merge the predicates of all operands (filters and other expressions).
   * @param cc compilation context
   * @return merged expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr mergeFilters(final CompileContext cc) throws QueryException {
    Expr root = null;
    final ExprList list = new ExprList();

    for(final Expr expr : exprs) {
      Expr rt = expr;
      Expr[] preds = {};
      if(expr instanceof Filter) {
        final Filter filter = (Filter) expr;
        if(filter.mayBePositional()) return null;
        rt = filter.root;
        preds = filter.exprs;
      }
      if(root == null) {
        root = rt;
        if(root.has(Flag.CNS, Flag.NDT) || !(root.seqType().type instanceof NodeType)) return null;
      } else if(!root.equals(rt)) {
        return null;
      }
      list.add(newPredicate(preds, cc));
    }
    final Expr pred = mergePredicates(list.finish(), cc).optimize(cc);
    return Filter.get(cc, info, root, pred);
  }

  /**
   * Creates a new predicate.
   * @param preds predicate expressions
   * @param cc compilation context
   * @return predicate expressions
   * @throws QueryException query exception
   */
  private Expr newPredicate(final Expr[] preds, final CompileContext cc) throws QueryException {
    final int el = preds.length;
    if(el == 0) return Bln.TRUE;
    if(el == 1) return preds[0];
    return new And(info, preds).optimize(cc);
  }

  /**
   * Creates a merged filter predicate.
   * @param preds predicate expressions (at least two)
   * @param cc compilation context
   * @return new predicate expression
   * @throws QueryException query exception
   */
  abstract Logical mergePredicates(Expr[] preds, CompileContext cc) throws QueryException;

  @Override
  public final boolean ddo() {
    return true;
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this, ITERATIVE, iterative), exprs);
  }

  @Override
  public final void plan(final QueryString qs) {
    qs.tokens(exprs, ' ' + Util.className(this).toLowerCase(Locale.ENGLISH) + ' ', true);
  }

  /**
   * Abstract set iterator.
   */
  abstract class SetIter extends NodeIter {
    /** Query context. */
    private final QueryContext qc;
    /** Iterator. */
    final Iter[] iter;
    /** Items. */
    ANode[] nodes;

    /**
     * Constructor.
     * @param qc query context
     * @param iter iterator
     */
    SetIter(final QueryContext qc, final Iter[] iter) {
      this.qc = qc;
      this.iter = iter;
    }

    /**
     * Sets the next iterator item.
     * @param i index
     * @return true if another item was found
     * @throws QueryException query exception
     */
    final boolean next(final int i) throws QueryException {
      final Item item = qc.next(iter[i]);
      if(item == null) {
        nodes[i] = null;
        return false;
      }
      nodes[i] = toNode(item);
      return true;
    }
  }
}
