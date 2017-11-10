package org.basex.query.expr;

import static org.basex.query.expr.path.Axis.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param exprs predicates
   */
  protected Filter(final InputInfo info, final Expr root, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
    this.root = root;
  }

  /**
   * Creates a filter or path expression for the given root and predicates.
   * @param info input info
   * @param root root expression
   * @param exprs predicate expressions
   * @return filter expression
   */
  public static Expr get(final InputInfo info, final Expr root, final Expr... exprs) {
    final Expr expr = simplify(root, exprs);
    if(expr != null) return expr;

    // use simple filter for single deterministic predicate
    final Expr pred = exprs[0];
    if(exprs.length == 1 && pred.isSimple()) return new SimpleFilter(info, root, exprs);

    return new CachedFilter(info, root, exprs);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    root = root.compile(cc);
    // invalidate current context value (will be overwritten by filter)
    cc.pushFocus(cc.contextItem(root));
    try {
      return super.compile(cc);
    } finally {
      cc.popFocus();
    }
  }

  /**
   * Adds a predicate to the filter.
   * This function is e.g. called by {@link For#addPredicate}.
   * @param p predicate to be added
   * @return new filter
   */
  public final CachedFilter addPred(final Expr p) {
    exprs = new ExprList(exprs.length + 1).add(exprs).add(p).finish();
    return new CachedFilter(info, root, exprs);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // return empty root
    if(root == Empty.SEQ) return cc.emptySeq(this);

    // simplify predicates
    simplify(cc, root);

    // remember current context value (will be temporarily overwritten)
    cc.pushFocus(cc.contextItem(root));
    try {
      final Expr ex = super.optimize(cc);
      if(ex != this) return ex;
    } finally {
      cc.popFocus();
    }

    // check result size
    seqType(root.seqType(), root.size());
    if(size == 0) return cc.emptySeq(this);

    // if possible, convert filter to root or path expression
    Expr ex = simplify(root, exprs);
    if(ex != null) return ex.optimize(cc);

    // try to rewrite filter to index access
    if(root instanceof ContextValue || root instanceof Value && root.data() != null) {
      final Path ip = Path.get(info, root, Step.get(info, SELF, KindTest.NOD, exprs));
      final Expr ie = ip.index(cc, cc.contextValue(root));
      if(ie != ip) return ie;
    }

    // no numeric predicates.. use simple iterator
    if(!positional()) return copyType(new IterFilter(info, root, exprs));

    // evaluate positional predicates
    Expr rt = root;
    boolean opt = false;
    for(final Expr expr : exprs) {
      ex = null;
      if(expr.isFunction(Function.LAST)) {
        if(rt.isValue()) {
          // return sub-sequence
          ex = FnSubsequence.eval((Value) rt, rt.size(), 1);
        } else {
          // rewrite positional predicate to util:last-from
          ex = cc.function(Function._UTIL_LAST_FROM, info, rt);
        }
      } else if(expr instanceof ItrPos) {
        final ItrPos pos = (ItrPos) expr;
        if(rt.isValue()) {
          // return sub-sequence
          ex = FnSubsequence.eval((Value) rt, pos.min, pos.max - pos.min + 1);
        } else if(pos.min == pos.max) {
          // example: expr[pos] -> util:item-at(expr, pos.min)
          ex = cc.function(Function._UTIL_ITEM_AT, info, rt, Int.get(pos.min));
        } else {
          // example: expr[pos] -> util:item-range(expr, pos.min, pos.max)
          ex = cc.function(Function._UTIL_ITEM_RANGE, info, rt, Int.get(pos.min),
              Int.get(pos.max));
        }
      } else if(expr instanceof Pos) {
        final Pos pos = (Pos) expr;
        if(pos.exprs[0] == pos.exprs[1]) {
          // example: expr[pos] -> util:item-at(expr, pos.min)
          ex = cc.function(Function._UTIL_ITEM_AT, info, rt, pos.exprs[0]);
        } else {
          // example: expr[pos] -> util:item-range(expr, pos.min, pos.max)
          ex = cc.function(Function._UTIL_ITEM_RANGE, info, rt, pos.exprs[0], pos.exprs[1]);
        }
      } else if(num(expr)) {
        /* - rewrite positional predicate to util:item-at
         *   example: expr[pos] -> util:item-at(expr, pos)
         * - only choose deterministic and context-independent offsets
         *   illegal examples: (1 to 10)[random:integer(10)]  or  (1 to 10)[.] */
        ex = cc.function(Function._UTIL_ITEM_AT, info, rt, expr);
      }

      if(ex != null) {
        rt = ex;
        opt = true;
      } else {
        // rebuild filter if no optimization can be applied
        rt = rt instanceof Filter ? ((Filter) rt).addPred(expr) : get(info, rt, expr);
      }
    }

    // return optimized expression or standard iterator
    return opt ? cc.replaceWith(this, rt) : get(info, root, exprs);
  }

  @Override
  public final Expr optimizeEbv(final CompileContext cc) throws QueryException {
    final Expr ex = optimizeEbv(root, cc);
    return ex == this ? super.optimizeEbv(cc) : cc.replaceEbv(this, ex);
  }

  /**
   * Checks if the specified filter input can be rewritten to the root or an axis path.
   * @param root root expression
   * @param exprs predicate expressions
   * @return filter expression, or {@code null}
   */
  private static Expr simplify(final Expr root, final Expr... exprs) {
    // no predicates: return root
    if(exprs.length == 0) return root;
    // axis path: attach non-positional predicates to last step: (//x)[1] != //x[1]
    if(root instanceof AxisPath && !positional(exprs)) return ((AxisPath) root).addPreds(exprs);
    return null;
  }

  @Override
  public final boolean has(final Flag... flags) {
    if(root.has(flags)) return true;
    final Flag[] flgs = Flag.POS.remove(Flag.CTX.remove(flags));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public final boolean removable(final Var var) {
    return root.removable(var) && super.removable(var);
  }

  @Override
  public final VarUsage count(final Var var) {
    final VarUsage inPreds = super.count(var), inRoot = root.count(var);
    if(inPreds == VarUsage.NEVER) return inRoot;
    final long sz = root.size();
    return sz == 0 || sz == 1 || root.seqType().zeroOrOne() ? inRoot.plus(inPreds) :
      VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {

    final Expr rt = root == null ? null : root.inline(var, ex, cc);
    if(rt != null) root = rt;
    final boolean pr = inlineAll(exprs, var, ex, cc);
    return pr || rt != null ? optimize(cc) : null;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    for(final Expr expr : exprs) {
      visitor.enterFocus();
      if(!expr.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return root.accept(visitor);
  }

  @Override
  public final int exprSize() {
    int sz = 1;
    for(final Expr expr : exprs) sz += expr.exprSize();
    return sz + root.exprSize();
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof Filter && root.equals(((Filter) obj).root) &&
        super.equals(obj);
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(), root, exprs);
  }

  @Override
  public final String toString() {
    return "(" + root + ')' + super.toString();
  }
}
