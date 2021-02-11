package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.Function;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and {@link Step}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Preds extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param seqType sequence type
   * @param preds predicates
   */
  protected Preds(final InputInfo info, final SeqType seqType, final Expr... preds) {
    super(info, seqType, preds);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    // called at an early stage as it influences the optimization of predicates
    type(cc.qc.focus.value);

    final int el = exprs.length;
    if(el != 0) cc.get(this, () -> {
      final QueryFocus focus = cc.qc.focus;
      final Value init = focus.value;
      for(int e = 0; e < el; ++e) {
        try {
          exprs[e] = exprs[e].compile(cc);
        } catch(final QueryException ex) {
          // replace original expression with error
          exprs[e] = cc.error(ex, exprs[e]);
        }
      }
      focus.value = init;
      return null;
    });
    return optimize(cc);
  }

  /**
   * Assigns the expression type.
   * @param expr root expression
   */
  protected abstract void type(Expr expr);

  /**
   * Assigns the sequence type and result size.
   * @param root root expression
   * @return whether evaluation can be skipped
   */
  private boolean exprType(final Expr root) {
    long max = root.size();
    boolean exact = max != -1;
    if(!exact) max = Long.MAX_VALUE;

    // check for positional predicates
    for(final Expr expr : exprs) {
      if(Function.LAST.is(expr)) {
        // use minimum of old value and 1
        max = Math.min(max, 1);
      } else if(expr instanceof ItrPos) {
        final ItrPos pos = (ItrPos) expr;
        // subtract start position. example: ...[1 to 2][2]  ->  2  ->  1
        if(max != Long.MAX_VALUE) max = Math.max(0, max - pos.min + 1);
        // use minimum of old value and range. example: ...[1 to 5]  ->  5
        max = Math.min(max, pos.max - pos.min + 1);
      } else {
        // resulting size will be unknown for any other filter
        exact = false;
      }
    }
    exprType.assign(root.seqType().union(Occ.ZERO), exact || max == 0 ? max : -1);
    return max == 0;
  }

  /**
   * Checks if the specified item matches the predicates.
   * @param item item to be checked
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean match(final Item item, final QueryContext qc) throws QueryException {
    // set context value and position
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;
    qf.value = item;
    try {
      for(final Expr expr : exprs) {
        if(expr.test(qc, info) == null) return false;
      }
      return true;
    } finally {
      qf.value = cv;
    }
  }

  /**
   * Simplifies all predicates.
   * @param cc compilation context
   * @param root root expression
   * @return {@code true} if evaluation can be skipped
   * @throws QueryException query exception
   */
  protected final boolean simplify(final CompileContext cc, final Expr root) throws QueryException {
    return cc.ok(root, () -> {
      final ExprList list = new ExprList(exprs.length);
      for(final Expr expr : exprs) simplify(expr, list, root, cc);
      exprs = list.finish();
      return optimizeEbv(false, true, cc);
    }) || exprType(root);
  }

  /**
   * Simplifies a predicate.
   * @param pred predicate
   * @param list resulting predicates
   * @param root root expression
   * @param cc compilation context
   * @throws QueryException query exception
   */
  private void simplify(final Expr pred, final ExprList list, final Expr root,
      final CompileContext cc) throws QueryException {

    // AND expression
    if(pred instanceof And && !pred.has(Flag.POS)) {
      // E[A and B]  ->  E[A][B]
      cc.info(OPTPRED_X, pred);
      for(final Expr expr : pred.args()) {
        simplify(expr.seqType().mayBeNumber() ? cc.function(Function.BOOLEAN, info, expr) : expr,
          list, root, cc);
      }
      return;
    }

    // comparisons
    Expr expr = pred;
    if(expr instanceof CmpG || expr instanceof CmpV) {
      // E[position() = 1]  ->  E[1]
      expr = ((Cmp) expr).optPred(root, cc);
    }

    // map operator
    final SeqType rst = root.seqType();
    if(expr instanceof SimpleMap) {
      // E[. ! ...]  ->  E[...]
      // E[E ! ...]  ->  E[...]
      final SimpleMap map = (SimpleMap) expr;
      final Expr[] mexprs = map.exprs;
      final Expr first = mexprs[0], second = mexprs[1];
      if((first instanceof ContextValue || root.equals(first) && root.isSimple() && rst.one()) &&
          !second.has(Flag.POS)) {
        expr = SimpleMap.get(cc, map.info, Arrays.copyOfRange(mexprs, 1, mexprs.length));
      }
    }

    // paths
    if(expr instanceof Path && rst.type instanceof NodeType) {
      // E[./...]  ->  E[...]
      // E[E/...]  ->  E[...]
      final Path path = (Path) expr;
      final Expr first = path.root;
      if((first instanceof ContextValue || root.equals(first) && root.isSimple() && rst.one()) &&
          !path.steps[0].has(Flag.POS)) {
        expr = Path.get(cc, path.info, null, path.steps);
      }
    }

    // inline root item (ignore nodes)
    // 1[. = 1]  ->  1[1 = 1]
    if(root instanceof Item && !(rst.type instanceof NodeType)) {
      expr = new InlineContext(null, root, cc).inline(expr);
    }

    // E[exists(nodes)]  ->  E[nodes]
    // E[count(nodes)]  will not be rewritten
    expr = expr.simplifyFor(Simplify.PREDICATE, cc);

    // rewrite number to positional test
    if(expr instanceof ANum) expr = ItrPos.get(((ANum) expr).dbl(), info);

    // merge node tests with steps; remove redundant node tests
    if(expr instanceof SingleIterPath) {
      final Step predStep = (Step) ((Path) expr).steps[0];
      if(predStep.axis == Axis.SELF && !predStep.mayBePositional()) {
        if(root instanceof Step && !mayBePositional()) {
          final Step rootStep = (Step) root;
          final Test test = rootStep.test.intersect(predStep.test);
          if(test != null) {
            // child::node()[self:*]  ->  child::*
            cc.info(OPTMERGE_X, predStep);
            rootStep.test = test;
            list.add(predStep.exprs);
            return;
          }
        }
        if(predStep.test instanceof KindTest && predStep.exprs.length == 0 &&
            rst.type.instanceOf(predStep.test.type)) {
          // <a/>[self:*]  ->  <a/>
          cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
          return;
        }
      }
    }

    // context value
    if(expr instanceof ContextValue && rst.type instanceof NodeType) {
      // E[.]  ->  E
      cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      return;
    }

    // positional tests
    if(root instanceof Step && expr instanceof ItrPos) {
      // <a/>/.[1]  ->  <a/>/.[true()]
      // $child/..[2]  ->  $child/..[false()]
      final Axis axis = ((Step) root).axis;
      if(axis == Axis.SELF || axis == Axis.PARENT) expr = Bln.get(((ItrPos) expr).min == 1);
    }

    // positional tests with position()
    if(expr instanceof Cmp) {
      final Cmp cmp = (Cmp) expr;
      final Expr ex = cmp.exprs[1];
      final SeqType st = ex.seqType();
      if(cmp.positional() && cmp.opV() == OpV.EQ && st.one()) {
        // E[position() = last() - 1]  ->  E[last() - 1]
        expr = new Cast(cc.sc(), info, ex, SeqType.NUMERIC_O).optimize(cc);
      }
    }

    // E[position()]  ->  E
    if(Function.POSITION.is(expr)) expr = Bln.TRUE;

    list.add(cc.simplify(pred, expr));
  }

  /**
   * Flattens predicates for boolean evaluation.
   * Drops solitary context values, flattens nested predicates.
   * @param root root expression
   * @param ebv EBV check
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public final Expr flattenEbv(final Expr root, final boolean ebv, final CompileContext cc)
      throws QueryException {

    // only single predicate can be rewritten; root must yield nodes; no positional predicates
    final SeqType rst = root.seqType();
    final int el = exprs.length;
    if(el == 0 || mayBePositional() || ebv && !(rst.type instanceof NodeType)) return this;

    final Expr pred = exprs[el - 1];
    final QueryFunction<Expr, Expr> createRoot = r ->
      el == 1 ? r : Filter.get(cc, info, r, Arrays.copyOfRange(exprs, 0, el - 1));
    final QueryFunction<Expr, Expr> createExpr = e ->
      e instanceof ContextValue ? createRoot.apply(root) :
      e instanceof Path ? Path.get(cc, info, createRoot.apply(root), e) : null;

    // rewrite to general comparison (right operand must not depend on context):
    // a[. = 'x']  ->  a = 'x'
    // a[text() = 'x']  ->  a/text() = 'x'
    if(pred instanceof CmpG) {
      // not applicable to value/node comparisons, as cardinality of expression might change
      final CmpG cmp = (CmpG) pred;
      final Expr expr1 = createExpr.apply(cmp.exprs[0]), expr2 = cmp.exprs[1];
      // right operand must not depend on context
      if(expr1 != null && !expr2.has(Flag.CTX)) {
        return new CmpG(expr1, expr2, cmp.op, cmp.coll, cmp.sc, cmp.info).optimize(cc);
      }
    }

    // rewrite to contains text expression (right operand must not depend on context):
    // a[. contains text 'x']  ->  a contains text 'x'
    // a[text() contains text 'x']  ->  a/text() contains text 'x'
    if(pred instanceof FTContains) {
      final FTContains cmp = (FTContains) pred;
      final Expr expr = createExpr.apply(cmp.expr);
      final FTExpr ftexpr = cmp.ftexpr;
      if(expr != null && !ftexpr.has(Flag.CTX)) {
        return new FTContains(expr, ftexpr, cmp.info).optimize(cc);
      }
    }

    // rewrite to path: root[path]  ->  root/path
    // rewrite to path: root[data()]  ->  root/descendant::text()
    if(rst.type instanceof NodeType) {
      Expr expr = createExpr.apply(pred);
      if(expr == null && (Function.DATA.is(pred) || Function.STRING.is(pred))) {
        final ContextFn func = (ContextFn) pred;
        if(func.contextAccess()) expr = func.simplifyEbv(root, cc);
      }
      if(expr != null) return expr;
    }

    // rewrite to simple map: $node[string()]  ->  $node ! string()
    if(rst.zeroOrOne()) return SimpleMap.get(cc, info, createRoot.apply(root), pred);

    return this;
  }

  /**
   * Checks if the specified expression returns an empty sequence or a deterministic numeric value.
   * @param expr expression
   * @return result of check
   */
  protected static boolean numeric(final Expr expr) {
    final SeqType st = expr.seqType();
    return st.type.isNumber() && st.zeroOrOne() && expr.isSimple();
  }

  /**
   * Checks if at least one of the predicates may be positional.
   * @return result of check
   */
  public boolean mayBePositional() {
    for(final Expr expr : exprs) {
      if(mayBePositional(expr)) return true;
    }
    return false;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    if(ic.expr instanceof ContextValue && ic.var != null) {
      for(final Expr expr : exprs) {
        if(expr.uses(ic.var)) return false;
      }
    }
    return true;
  }

  @Override
  public void plan(final QueryString qs) {
    for(final Expr expr : exprs) qs.bracket(expr);
  }
}
