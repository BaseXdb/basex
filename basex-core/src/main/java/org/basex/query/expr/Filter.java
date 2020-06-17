package org.basex.query.expr;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param exprs predicate expressions
   */
  protected Filter(final InputInfo info, final Expr root, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
    this.root = root;
  }

  /**
   * Creates a new, optimized filter expression, or the root expression if no predicates exist.
   * @param cc compilation context
   * @param ii input info
   * @param root root expression
   * @param preds predicate expressions
   * @return filter root, path or filter expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo ii, final Expr root,
      final Expr... preds) throws QueryException {
    return preds.length == 0 ? root : new CachedFilter(ii, root, preds).optimize(cc);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    root = root.compile(cc);
    return super.compile(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // flatten nested filters
    if(root instanceof Filter) {
      final Filter filter = (Filter) root;
      root = filter.root;
      exprs = new ExprList().add(filter.exprs).add(exprs).finish();
    }

    // return empty root
    final SeqType st = root.seqType();
    if(st.zero()) return cc.replaceWith(this, root);

    // simplify predicates
    if(optimize(cc, root)) return cc.emptySeq(this);
    // no predicates: return root
    if(exprs.length == 0) return root;

    // no positional access..
    if(!mayBePositional()) {
      // convert to axis path: (//x)[text() = 'a']  ->  //x[text() = 'a']
      if(root instanceof AxisPath) return ((AxisPath) root).addPredicates(cc, exprs);

      // rewrite filter with document nodes to path; enables index rewritings
      // example: db:open('db')[.//text() = 'x']  ->  db:open('db')/.[.//text() = 'x']
      if(st.type == NodeType.DOC && root.ddo()) {
        final Expr step = new StepBuilder(info).preds(exprs).finish(cc, root);
        return cc.replaceWith(this, Path.get(cc, info, root, step));
      }

      // rewrite independent deterministic single filter to if expression:
      // example: (1 to 10)[$boolean]  ->  if($boolean) then (1 to 10) else ()
      final Expr expr = exprs[0];
      if(exprs.length == 1 && expr.isSimple() && !expr.seqType().mayBeNumber()) {
        final Expr iff = new If(info, expr, root).optimize(cc);
        return cc.replaceWith(this, iff);
      }

      // otherwise, return iterative filter
      return copyType(new IterFilter(info, root, exprs));
    }

    // rewrite positional predicates
    Expr expr = root;
    boolean opt = false;
    final ExprList preds = new ExprList(exprs.length);
    final QueryFunction<Expr, Expr> prepare = ex -> {
      return preds.isEmpty() ? ex : get(cc, info, ex, preds.next());
    };
    for(final Expr pred : exprs) {
      Expr ex = null;
      if(Function.LAST.is(pred)) {
        // rewrite positional predicate to util:last
        ex = cc.function(Function._UTIL_LAST, info, prepare.apply(expr));
      } else if(pred instanceof ItrPos) {
        final ItrPos pos = (ItrPos) pred;
        if(pos.min != pos.max) {
          // expr[min..max]  ->  util:range(expr, min, max)
          ex = cc.function(Function._UTIL_RANGE, info, prepare.apply(expr),
              Int.get(pos.min), Int.get(pos.max));
        } else if(pos.min == 1) {
          // expr[1]  ->  head(expr)
          ex = cc.function(Function.HEAD, info, prepare.apply(expr));
        } else {
          // expr[pos]  ->  util:item(expr, pos)
          ex = cc.function(Function._UTIL_ITEM, info, prepare.apply(expr), Int.get(pos.min));
        }
      } else if(pred instanceof Pos) {
        final Pos pos = (Pos) pred;
        if(pos.eq()) {
          // expr[pos]  ->  util:item(expr, pos.min)
          ex = cc.function(Function._UTIL_ITEM, info, prepare.apply(expr), pos.exprs[0]);
        } else {
          // expr[min..max]  ->  util:range(expr, pos.min, pos.max)
          ex = cc.function(Function._UTIL_RANGE, info, prepare.apply(expr),
              pos.exprs[0], pos.exprs[1]);
        }
      } else if(numeric(pred)) {
        /* - rewrite positional predicate to util:item
         *   expr[pos]  ->  util:item(expr, pos)
         * - only choose deterministic and context-independent offsets
         *   illegal examples: (1 to 10)[random:integer(10)]  or  (1 to 10)[.] */
        ex = cc.function(Function._UTIL_ITEM, info, prepare.apply(expr), pred);
      } else if(pred instanceof Cmp) {
        // rewrite positional predicate to fn:remove
        final Cmp cmp = (Cmp) pred;
        final OpV opV = cmp.opV();
        if(cmp.positional() && opV != null) {
          final Expr e = cmp.exprs[1];
          if((opV == OpV.LT || opV == OpV.NE) && Function.LAST.is(e)) {
            // expr[position() < last()]  ->  util:init(expr)
            ex = cc.function(Function._UTIL_INIT, info, prepare.apply(expr));
          } else if(opV == OpV.NE && e instanceof Int) {
            // expr[position() != INT]  ->  remove(expr, INT)
            ex = cc.function(Function.REMOVE, info, prepare.apply(expr), e);
          }
        }
      }
      // replace temporary result expression or add predicate to temporary list
      if(ex != null) {
        expr = ex;
        opt = true;
      } else {
        preds.add(pred);
      }
    }
    // return optimized expression
    if(opt) return cc.replaceWith(this, prepare.apply(expr));

    // choose best filter implementation
    return copyType(
      exprs.length == 1 && exprs[0].isSimple() ? new SimpleFilter(info, root, exprs) :
      new CachedFilter(info, root, exprs));
  }

  @Override
  protected final void type(final Expr expr, final CompileContext cc) {
    exprType.assign(root.seqType().type);
  }

  /**
   * Adds a predicate and returns the optimized expression.
   * This function is e.g. called by {@link For#addPredicate}.
   * @param cc compilation context
   * @param pred predicate to be added
   * @return new filter
   * @throws QueryException query exception
   */
  public final Expr addPredicate(final CompileContext cc, final Expr pred) throws QueryException {
    exprs = new ExprList(exprs.length + 1).add(exprs).add(pred).finish();
    return copyType(get(cc, info, root, exprs));
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    if(mode == Simplify.EBV || mode == Simplify.PREDICATE) {
      final Expr expr = simplifyEbv(root, cc);
      if(expr != this) return cc.simplify(this, expr);
    }
    return super.simplifyFor(mode, cc);
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
  public final Expr inline(final ExprInfo ei, final Expr ex, final CompileContext cc)
      throws QueryException {

    boolean changed = false;
    final Expr inlined = root.inline(ei, ex, cc);
    if(inlined != null) {
      root = inlined;
      changed = true;
    }
    changed |= ei != null && cc.ok(root, () -> inlineAll(ei, ex, exprs, cc));

    return changed ? optimize(cc) : null;
  }

  @Override
  public final Data data() {
    return root.data();
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
  public boolean ddo() {
    return root.ddo();
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
    return root + super.toString();
  }
}
