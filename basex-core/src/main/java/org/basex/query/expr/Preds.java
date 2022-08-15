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
 * @author BaseX Team 2005-22, BSD License
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
        exprs[e] = cc.compileOrError(exprs[e], false);
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

    Expr expr = pred;

    // comparisons: E[position() = 1]  ->  E[1]
    if(expr instanceof CmpG || expr instanceof CmpV) expr = ((Cmp) expr).optPred(root, cc);

    // map operator: E[. ! ...]  ->  E[...], E[E ! ...]  ->  E[...]
    final SeqType rst = root.seqType();
    if(expr instanceof SimpleMap) {
      final SimpleMap map = (SimpleMap) expr;
      final Expr[] mexprs = map.exprs;
      final Expr first = mexprs[0], second = mexprs[1];
      if((first instanceof ContextValue || root.equals(first) && root.isSimple() && rst.one()) &&
          !second.has(Flag.POS)) {
        expr = SimpleMap.get(cc, map.info, Arrays.copyOfRange(mexprs, 1, mexprs.length));
      }
    }

    // paths: E[./...]  ->  E[...], E[E/...]  ->  E[...]
    if(expr instanceof Path && rst.type instanceof NodeType) {
      final Path path = (Path) expr;
      final Expr first = path.root;
      if((first instanceof ContextValue || root.equals(first) && root.isSimple() && rst.one()) &&
          !path.steps[0].has(Flag.POS)) {
        expr = Path.get(cc, path.info, null, path.steps);
      }
    }

    // inline root item (ignore nodes): 1[. = 1]  ->  1[1 = 1]
    if(root instanceof Item && !(rst.type instanceof NodeType)) {
      try {
        expr = new InlineContext(null, root, cc).inline(expr);
      } catch(final QueryException ex) {
        // replace original expression with error
        expr = cc.error(ex, expr);
      }
    }

    // E[exists(nodes)]  ->  E[nodes]
    // E[count(nodes)]  will not be rewritten
    expr = expr.simplifyFor(Simplify.PREDICATE, cc);

    // E[position()]  ->  E[true()]
    if(Function.POSITION.is(expr)) expr = Bln.TRUE;

    // positional tests: E[1]  ->  E[position() = 1]
    if(expr instanceof ANum) expr = ItrPos.get(((ANum) expr).dbl(), info);

    // merge node tests with steps; remove redundant node tests
    // child::node()[self:*]  ->  child::*
    if(expr instanceof SingleIterPath) {
      final Step predStep = (Step) ((Path) expr).steps[0];
      if(predStep.axis == Axis.SELF && !predStep.mayBePositional() && root instanceof Step &&
          !mayBePositional()) {
        final Step rootStep = (Step) root;
        final Test test = rootStep.test.intersect(predStep.test);
        if(test != null) {
          cc.info(OPTMERGE_X, predStep);
          rootStep.test = test;
          list.add(predStep.exprs);
          expr = Bln.TRUE;
        }
      }
    }

    // context value: E[.]  ->  E
    if(expr instanceof ContextValue && rst.type instanceof NodeType) {
      cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      expr = Bln.TRUE;
    }

    // positional tests:
    //   <a/>/.[position() = 1]  ->  <a/>/.[true()]
    //   $child/..[position() = 2]  ->  $child/..[false()]
    if(root instanceof Step && expr instanceof ItrPos) {
      final Axis axis = ((Step) root).axis;
      if(axis == Axis.SELF || axis == Axis.PARENT) expr = Bln.get(((ItrPos) expr).min == 1);
    }

    // positional comparisons: E[position() = last() - 1]  ->  E[last() - 1]
    if(expr instanceof Cmp) {
      final Cmp cmp = (Cmp) expr;
      final Expr ex = cmp.exprs[1];
      final SeqType st = ex.seqType();
      if(cmp.positional() && cmp.opV() == OpV.EQ && st.one()) {
        expr = new Cast(cc.sc(), info, ex, SeqType.NUMERIC_O).optimize(cc);
      }
    }

    // recursive optimization of AND expressions: E[A and [B and C]]  ->  E[A][B][C]
    if(expr instanceof And && !expr.has(Flag.POS)) {
      cc.info(OPTPRED_X, expr);
      for(final Expr ex : expr.args()) {
        final boolean numeric = ex.seqType().mayBeNumber();
        simplify(numeric ? cc.function(Function.BOOLEAN, info, ex) : ex, list, root, cc);
      }
      expr = Bln.TRUE;
    }

    // add predicate to list
    if(expr != Bln.TRUE) list.add(cc.simplify(pred, expr));
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
    final QueryBiFunction<Expr, Boolean, Expr> createExpr = (e, cmp) ->
        e instanceof ContextValue ? createRoot.apply(root) :
        e instanceof Path         ? Path.get(cc, info, createRoot.apply(root), e) :
        cmp                       ? SimpleMap.get(cc, info, createRoot.apply(root), e) : null;

    // rewrite to general comparison
    // a[. = 'x']           ->  a = 'x'
    // a[@id eq 'id1']      ->  a/@id = 'id1'
    // a[text() = data(.)]  ->  skip: right operand must not depend on context
    // a[b eq ('a', 'b')]   ->  skip: an error must be raised as right operand yields a sequence
    if(pred instanceof Cmp) {
      final Cmp cmp = (Cmp) pred;
      final Expr op1 = cmp.exprs[0], op2 = cmp.exprs[1];
      final SeqType st1 = op1.seqType(), st2 = op2.seqType();
      final Type type1 = st1.type, type2 = st2.type;
      if((cmp instanceof CmpG || cmp instanceof CmpV && st1.zeroOrOne() && st2.zeroOrOne() && (
        type1 == type2 || type1.isStringOrUntyped() && type2.isStringOrUntyped()
      )) && !op2.has(Flag.CTX)) {
        final Expr expr = createExpr.apply(op1, true);
        if(expr != null) {
          return new CmpG(expr, op2, cmp.opG(), cmp.coll, cmp.sc, cmp.info).optimize(cc);
        }
      }
    }

    // rewrite to contains text expression (right operand must not depend on context):
    // a[. contains text 'x']  ->  a contains text 'x'
    // a[text() contains text 'x']  ->  a/text() contains text 'x'
    if(pred instanceof FTContains) {
      final FTContains cmp = (FTContains) pred;
      final FTExpr ftexpr = cmp.ftexpr;
      if(!ftexpr.has(Flag.CTX)) {
        final Expr expr = createExpr.apply(cmp.expr, true);
        if(expr != null) return new FTContains(expr, ftexpr, cmp.info).optimize(cc);
      }
    }

    // rewrite to path: root[path]  ->  root/path
    // rewrite to path: root[data()]  ->  root/descendant::text()
    if(rst.type instanceof NodeType) {
      Expr expr = createExpr.apply(pred, false);
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
  public void toString(final QueryString qs) {
    for(final Expr expr : exprs) qs.bracket(expr);
  }
}
