package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.Function;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and {@link Step}.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class Preds extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param preds predicates
   */
  protected Preds(final InputInfo info, final SeqType seqType, final Expr... preds) {
    super(info, seqType, preds);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    // called at an early stage as it affects the optimization of predicates
    final Expr root = type(cc.qc.focus.value);
    final int el = exprs.length;
    if(el != 0) cc.get(this, () -> {
      if(root != null) {
        final long size = root.size();
        if(size != -1) cc.qc.focus.size = size;
      }
      for(int e = 0; e < el; ++e) {
        exprs[e] = cc.compileOrError(exprs[e], false);
        cc.qc.focus.size = 1;
      }
      return null;
    });
    return optimize(cc);
  }

  /**
   * Assigns the expression type.
   * @param expr root expression (can be {@code null})
   * @return actual root expression (can be {@code null})
   */
  protected abstract Expr type(Expr expr);

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
      if(expr instanceof Int || Function.LAST.is(expr)) {
        // use minimum of old value and 1
        max = Math.min(max, 1);
      } else if(expr instanceof IntPos) {
        final IntPos pos = (IntPos) expr;
        // subtract start position. example: ...[1 to 2][2]  ->  2  ->  1
        if(max != Long.MAX_VALUE) max = Math.max(0, max - pos.min + 1);
        // use minimum of old value and range. example: ...[1 to 5]  ->  5
        max = Math.min(max, pos.max - pos.min + 1);
      } else {
        // resulting size will be unknown for any other filter
        exact = false;
      }
    }

    final boolean empty = max == 0;
    SeqType st = root.seqType();
    st = max == 1 ? st.with(Occ.ZERO_OR_ONE) : st.union(Occ.ZERO);
    exprType.assign(st, exact || empty ? max : -1);
    return empty;
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
    final Value qv = qf.value;
    qf.value = item;
    try {
      for(final Expr expr : exprs) {
        if(!expr.test(qc, info)) return false;
      }
      return true;
    } finally {
      qf.value = qv;
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

    // E[exists(nodes)]  ->  E[nodes]
    // E[count(nodes)]  will not be rewritten
    Expr expr = pred.simplifyFor(Simplify.PREDICATE, cc);

    // map operator: E[. ! ...]  ->  E[...], E[E ! ...]  ->  E[...]
    final SeqType rst = root.seqType();
    final Predicate<Expr> first = f -> f instanceof ContextValue ||
        root.equals(f) && root.isSimple() && rst.one();
    if(expr instanceof SimpleMap) {
      final Expr[] mexprs = ((SimpleMap) expr).exprs;
      if(first.test(mexprs[0]) && !mexprs[1].has(Flag.POS)) {
        expr = SimpleMap.get(cc, expr.info(), Arrays.copyOfRange(mexprs, 1, mexprs.length));
      }
    }

    // paths: E[./...]  ->  E[...], E[E/...]  ->  E[...]
    if(expr instanceof Path && rst.type instanceof NodeType) {
      final Path path = (Path) expr;
      if(first.test(path.root) && !path.steps[0].has(Flag.POS)) {
        expr = Path.get(cc, expr.info(), null, path.steps);
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

    // merge node tests with steps; remove redundant node tests
    // child::node()[self::*]  ->  child::*
    if(expr instanceof SingleIterPath) {
      final Step predStep = (Step) ((Path) expr).steps[0];
      if(predStep.axis == Axis.SELF && !predStep.mayBePositional() && root instanceof Step &&
          !mayBePositional()) {
        final Step rootStep = (Step) root;
        final Test test = rootStep.test.intersect(predStep.test);
        if(test != null) {
          cc.info(OPTMERGE_X, predStep);
          rootStep.test = test;
          rootStep.exprType.assign(Step.seqType(rootStep.axis, rootStep.test, rootStep.exprs));
          list.add(predStep.exprs);
          expr = Bln.TRUE;
        }
      }
    }

    // positional tests:
    //   <a/>/.[position() = 1]  ->  <a/>/.[true()]
    //   $child/..[position() > 1]  ->  $child/..[false()]
    if(root instanceof Step) {
      final Axis axis = ((Step) root).axis;
      if(axis == Axis.SELF || axis == Axis.PARENT) {
        if(expr instanceof Int) {
          expr = Bln.get(((Int) expr).itr() == 1);
        } else if(expr instanceof IntPos) {
          expr = Bln.get(((IntPos) expr).min == 1);
        }
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
    if(expr != Bln.TRUE) list.add(cc.simplify(pred, expr, Simplify.PREDICATE));
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
    if(el == 0 || mayBePositional()) return this;

    final Expr last = exprs[el - 1];
    final QuerySupplier<Expr> createRoot = () ->
      root instanceof Path ? ((Path) root).removePredicate(cc) :
      el > 1 ? Filter.get(cc, info, root, Arrays.copyOfRange(exprs, 0, el - 1)) : root;
    final QueryFunction<Expr, Expr> createSimpleMap = rhs ->
      SimpleMap.get(cc, info, createRoot.get(), rhs);
    final QueryBiFunction<Expr, Boolean, Expr> createExpr = (rhs, compare) ->
      rhs instanceof ContextValue ? createRoot.get() :
      rhs instanceof Path         ? Path.get(cc, info, createRoot.get(), rhs) :
      compare                     ? createSimpleMap.apply(rhs) : this;

    if(rst.type instanceof NodeType) {
      // rewrite to path: root[path]  ->  root/path
      final Expr expr = createExpr.apply(last, false);
      if(expr != this) return expr;
    } else if(ebv) {
      return this;
    }

    // rewrite to general comparison
    // a[. = 'x']           ->  a = 'x'
    // a[@id eq 'id1']      ->  a/@id = 'id1'
    // a[text() = data(.)]  ->  skip: right operand must not depend on context
    // a[b eq ('a', 'b')]   ->  skip: an error must be raised as right operand yields a sequence
    if(last instanceof Cmp) {
      final Cmp cmp = (Cmp) last;
      final Expr op1 = cmp.exprs[0], op2 = cmp.exprs[1];
      final SeqType st1 = op1.seqType(), st2 = op2.seqType();
      final Type type1 = st1.type, type2 = st2.type;
      if((cmp instanceof CmpG || cmp instanceof CmpV && st1.zeroOrOne() && st2.zeroOrOne() && (
        type1 == type2 || type1.isStringOrUntyped() && type2.isStringOrUntyped()
      )) && !op2.has(Flag.CTX)) {
        final Expr expr = createExpr.apply(op1, true);
        if(expr != this) {
          return new CmpG(cmp.info, expr, op2, cmp.opG(), cmp.coll, cmp.sc).optimize(cc);
        }
      }
    }

    // rewrite to contains text expression (right operand must not depend on context):
    // a[. contains text 'x']  ->  a contains text 'x'
    // a[text() contains text 'x']  ->  a/text() contains text 'x'
    if(last instanceof FTContains) {
      final FTContains cmp = (FTContains) last;
      final FTExpr ftexpr = cmp.ftexpr;
      if(!ftexpr.has(Flag.CTX)) {
        final Expr expr = createExpr.apply(cmp.expr, true);
        if(expr != this) return new FTContains(expr, ftexpr, cmp.info).optimize(cc);
      }
    }

    // rewrite to simple map: $node[string()]  ->  $node ! string()
    if(rst.zeroOrOne()) return createSimpleMap.apply(last);

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
