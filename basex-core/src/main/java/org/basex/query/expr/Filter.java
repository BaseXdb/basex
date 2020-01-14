package org.basex.query.expr;

import static org.basex.query.expr.path.Axis.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @param ii input info
   * @param root root expression
   * @param exprs predicate expressions
   * @return filter root, path or filter expression
   */
  public static Expr get(final InputInfo ii, final Expr root, final Expr... exprs) {
    // no predicates: return root
    if(exprs.length == 0) return root;
    // return axis path
    if(root instanceof AxisPath && !positional(exprs))
      return ((AxisPath) root).addPredicates(exprs);

    // use simple filter for single deterministic predicate
    final Expr pred = exprs[0];
    if(exprs.length == 1 && pred.isSimple()) return new SimpleFilter(ii, root, exprs);

    return new CachedFilter(ii, root, exprs);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    root = root.compile(cc);
    cc.pushFocus(root);
    try {
      super.compile(cc);
    } finally {
      cc.removeFocus();
    }
    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // return empty root
    final SeqType rt = root.seqType();
    if(rt.zero()) return cc.replaceWith(this, root);

    // simplify predicates
    if(!optimize(cc, root)) return cc.emptySeq(this);
    // no predicates: return root
    if(exprs.length == 0) return root;

    // no positional access..
    if(!positional()) {
      // convert to axis path: (//x)[text() = 'a'] -> //x[text() = 'a']
      if(root instanceof AxisPath) return ((AxisPath) root).addPredicates(exprs).optimize(cc);

      // rewrite filter with document nodes to path; enables index rewritings
      // example: db:open('db')[.//text() = 'x'] -> db:open('db')/.[.//text() = 'x']
      if(root instanceof Value && root.data() != null && rt.type == NodeType.DOC) {
        final Expr path = Path.get(info, root, Step.get(info, SELF, KindTest.NOD, exprs));
        return cc.replaceWith(this, path.optimize(cc));
      }

      // rewrite independent deterministic single filter to if expression:
      // example: (1 to 10)[$boolean] -> if($boolean) then (1 to 10) else ()
      final Expr expr = exprs[0];
      if(exprs.length == 1 && expr.isSimple() && !expr.seqType().mayBeNumber()) {
        final Expr iff = new If(info, expr, root).optimize(cc);
        return cc.replaceWith(this, iff);
      }

      // otherwise, return iterative filter
      return copyType(new IterFilter(info, root, exprs));
    }

    // evaluate positional predicates: build new root expression
    Expr r = root;
    boolean opt = false;
    for(final Expr pred : exprs) {
      Expr expr = null;
      if(Function.LAST.is(pred)) {
        if(r instanceof Value) {
          // value: replace with last item
          expr = ((Value) r).itemAt(r.size() - 1);
        } else {
          // rewrite positional predicate to util:last
          expr = cc.function(Function._UTIL_LAST, info, r);
        }
      } else if(pred instanceof ItrPos) {
        final ItrPos pos = (ItrPos) pred;
        if(r instanceof Value) {
          // value: replace with subsequence
          final long size = pos.min - 1, len = Math.min(pos.max, r.size()) - size;
          expr = len <= 0 ? Empty.VALUE : ((Value) r).subSequence(size, len, cc.qc);
        } else if(pos.min == pos.max) {
          // expr[pos] -> util:item(expr, pos)
          expr = pos.min == 1 ? cc.function(Function.HEAD, info, r) :
            cc.function(Function._UTIL_ITEM, info, r, Int.get(pos.min));
        } else {
          // expr[min..max] -> util:range(expr, min, max)
          expr = cc.function(Function._UTIL_RANGE, info, r, Int.get(pos.min), Int.get(pos.max));
        }
      } else if(pred instanceof Pos) {
        final Pos pos = (Pos) pred;
        if(pos.eq()) {
          // expr[pos] -> util:item(expr, pos.min)
          expr = cc.function(Function._UTIL_ITEM, info, r, pos.exprs[0]);
        } else {
          // expr[min..max] -> util:range(expr, pos.min, pos.max)
          expr = cc.function(Function._UTIL_RANGE, info, r, pos.exprs[0], pos.exprs[1]);
        }
      } else if(numeric(pred)) {
        /* - rewrite positional predicate to util:item
         *   expr[pos] -> util:item(expr, pos)
         * - only choose deterministic and context-independent offsets
         *   illegal examples: (1 to 10)[random:integer(10)]  or  (1 to 10)[.] */
        expr = cc.function(Function._UTIL_ITEM, info, r, pred);
      } else if(pred instanceof Cmp) {
        // rewrite positional predicate to fn:remove
        final Cmp cmp = (Cmp) pred;
        final OpV opV = cmp.opV();
        if(cmp.positional() && opV != null) {
          final Expr ex = cmp.exprs[1];
          if(opV == OpV.LT || opV == OpV.NE && Function.LAST.is(ex)) {
            // expr[position() < last()] -> util:init(expr)
            expr = cc.function(Function._UTIL_INIT, info, r);
          } else if(opV == OpV.NE && ex instanceof Int) {
            // expr[position() != INT] -> remove(expr, INT)
            expr = cc.function(Function.REMOVE, info, r, ex);
          }
        }
      }

      if(expr != null) {
        // predicate was optimized: replace old with new expression
        r = expr;
        opt = true;
      } else if(r != root && r instanceof Filter) {
        // otherwise, if root has changed: add predicate to temporary filter
        r = ((Filter) r).addPredicate(pred);
      } else {
        // otherwise, create new filter expression
        r = get(info, r, pred);
      }
    }

    // return optimized expression or standard iterator
    if(opt) return cc.replaceWith(this, r);

    final Expr expr = get(info, root, exprs);
    return expr instanceof ParseExpr ? copyType((ParseExpr) expr) : expr;
  }

  /**
   * Adds a predicate to the filter.
   * This function is e.g. called by {@link For#addPredicate}.
   * @param pred predicate to be added
   * @return new filter
   */
  public final CachedFilter addPredicate(final Expr pred) {
    exprs = new ExprList(exprs.length + 1).add(exprs).add(pred).finish();
    return copyType(new CachedFilter(info, root, exprs));
  }

  @Override
  public final Expr simplify(final CompileContext cc, final Simplify simplify)
      throws QueryException {

    if(simplify == Simplify.EBV) {
      final Expr expr = simplify(root, cc);
      if(expr != this) return cc.simplify(this, expr);
    }
    return super.simplify(cc, simplify);
  }

  @Override
  public final boolean has(final Flag... flags) {
    if(root.has(flags)) return true;
    final Flag[] flgs = Flag.POS.remove(Flag.CTX.remove(flags));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public final boolean inlineable(final Var var) {
    return root.inlineable(var) && super.inlineable(var);
  }

  @Override
  public final VarUsage count(final Var var) {
    final VarUsage inPreds = super.count(var), inRoot = root.count(var);
    return inPreds == VarUsage.NEVER ? inRoot :
      root.seqType().zeroOrOne() ? inRoot.plus(inPreds) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {

    boolean changed = false;
    final Expr rt = root.inline(var, ex, cc);
    if(rt != null) {
      root = rt;
      changed = true;
    }
    if(var != null) {
      cc.pushFocus(root);
      try {
        if(inlineAll(var, ex, exprs, cc)) changed = true;
      } finally {
        cc.removeFocus();
      }
    }
    return changed ? optimize(cc) : null;
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
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size + root.exprSize();
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof Filter && root.equals(((Filter) obj).root) &&
        super.equals(obj);
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this), root, exprs);
  }

  @Override
  public final String toString() {
    return "(" + root + ')' + super.toString();
  }
}
