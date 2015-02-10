package org.basex.query.expr;

import static org.basex.query.expr.path.Axis.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicates
   */
  Filter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, preds);
    this.root = root;
  }

  /**
   * Creates a filter or path expression for the given root and predicates.
   * @param info input info
   * @param root root expression
   * @param preds predicate expressions
   * @return filter expression
   */
  public static Expr get(final InputInfo info, final Expr root, final Expr... preds) {
    final Expr p = path(root, preds);
    return p == null ? new CachedFilter(info, root, preds) : p;
  }

  /**
   * Checks if the specified filter input can be rewritten to an axis path.
   * @param root root expression
   * @param preds predicate expressions
   * @return filter expression
   */
  private static Expr path(final Expr root, final Expr... preds) {
    // no predicates: return root
    if(preds.length == 0) return root;
    // axis path
    if(root instanceof AxisPath) {
      // predicate must not be numeric
      for(final Expr pred : preds) {
        if(pred.seqType().mayBeNumber() || pred.has(Flag.FCS)) return null;
      }
      return ((AxisPath) root).addPreds(preds);
    }
    return null;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    root = root.compile(qc, scp);
    // invalidate current context value (will be overwritten by filter)
    final Value init = qc.value;
    qc.value = Path.initial(qc, root);
    try {
      super.compile(qc, scp);
    } finally {
      qc.value = init;
    }
    return optimize(qc, scp);
  }

  @Override
  public final Expr optimizeEbv(final QueryContext qc, final VarScope scp) throws QueryException {
    final Expr e = merge(root, qc, scp);
    if(e != this) {
      qc.compInfo(QueryText.OPTWRITE, this);
      return e;
    }
    return super.optimizeEbv(qc, scp);
  }

  /**
   * Adds a predicate to the filter.
   * This function is e.g. called by {@link For#addPredicate}.
   * @param p predicate to be added
   * @return self reference
   */
  public Expr addPred(final Expr p) {
    preds = Array.add(preds, new Expr[preds.length + 1], p);
    return new CachedFilter(info, root, preds);
  }

  @Override
  public final Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // return empty root
    if(root.isEmpty()) return optPre(qc);

    // remember current context value (will be temporarily overwritten)
    final Value cv = qc.value;
    try {
      qc.value = Path.initial(qc, root);
      final Expr e = super.optimize(qc, scp);
      if(e != this) return e;
    } finally {
      qc.value = cv;
    }

    // determine type and number of results
    final SeqType st = root.seqType();
    // -1: number of results can only be guessed
    long max = root.size();
    boolean exact = max != -1;
    if(!exact) max = Long.MAX_VALUE;

    // evaluate positional predicates
    for(final Expr pred : preds) {
      if(pred.isFunction(Function.LAST)) {
        // use minimum of old value and 1
        max = Math.min(max, 1);
      } else if(pred instanceof Pos) {
        final Pos pos = (Pos) pred;
        // subtract start position. example: ...[1 to 2][2] -> (2 ->) 1
        if(max != Long.MAX_VALUE) max = Math.max(0, max - pos.min + 1);
        // use minimum of old value and range. example: ...[1 to 5] -> 5
        max = Math.min(max, pos.max - pos.min + 1);
      } else {
        // resulting size will be unknown for any other filter
        exact = false;
      }
    }
    // no results will remain: return empty sequence
    if(max == 0) return optPre(qc);

    if(exact) {
      seqType = st.withSize(size);
      size = max;
    } else {
      // we only know if there will be at most 1 result
      seqType = st.withOcc(max == 1 ? Occ.ZERO_ONE : Occ.ZERO_MORE);
      size = -1;
    }

    // if possible, convert filter to path
    final Expr ex = path(root, preds);
    if(ex != null) return ex.optimize(qc, scp);

    // try to rewrite filter to index access
    if(root instanceof Context || root instanceof Value && root.data() != null) {
      final Path ip = Path.get(info, root, Step.get(info, SELF, Test.NOD, preds));
      final Expr ie = ip.index(qc, Path.initial(qc, root));
      if(ie != ip) return ie;
    }

    // no numeric predicates.. use simple iterator
    if(!super.has(Flag.FCS)) return copyType(new IterFilter(info, root, preds));

    // evaluate positional predicates
    Expr e = root;
    boolean opt = false;
    final int pl = preds.length;
    for(int p = 0; p < pl; p++) {
      final Expr pred = preds[p];
      final Pos pos = pred instanceof Pos ? (Pos) pred : null;
      final boolean last = pred.isFunction(Function.LAST);

      if(last) {
        if(e.isValue()) {
          // return sub-sequence
          e = SubSeq.get((Value) e, e.size() - 1, 1);
        } else {
          // rewrite positional predicate to basex:last-from
          e = Function._BASEX_LAST_FROM.get(null, info, e);
        }
        opt = true;
      } else if(pos != null) {
        if(e.isValue()) {
          // return sub-sequence
          e = SubSeq.get((Value) e, pos.min - 1, pos.max - pos.min + 1);
        } else if(pos.min == pos.max) {
          // example: expr[pos] -> basex:item-at(expr, pos.min)
          e = Function._BASEX_ITEM_AT.get(null, info, e, Int.get(pos.min));
        } else {
          // example: expr[pos] -> basex:item-range(expr, pos.min, pos.max)
          e = Function._BASEX_ITEM_RANGE.get(null, info, e, Int.get(pos.min), Int.get(pos.max));
        }
        opt = true;
      } else if(num(pred)) {
        /* - rewrite positional predicate to basex:item-at
         *   example: expr[pos] -> basex:item-at(expr, pos)
         * - only choose deterministic and context-independent offsets.
         *   example: (1 to 10)[random:integer(10)]  or  (1 to 10)[.] */
        e = Function._BASEX_ITEM_AT.get(null, info, e, pred);
        opt = true;
      } else {
        // rebuild filter if no optimization can be applied
        e = e instanceof Filter ? ((Filter) e).addPred(pred) : Filter.get(info, e, pred);
      }
    }

    if(opt) {
      qc.compInfo(QueryText.OPTWRITE, this);
      return e.optimize(qc, scp);
    }

    // standard iterator
    return get(info, root, preds);
  }

  /**
   * Checks if the specified expression returns a deterministic numeric value.
   * @param expr expression
   * @return result of check
   */
  private static boolean num(final Expr expr) {
    final SeqType st = expr.seqType();
    return st.type.isNumber() && st.zeroOrOne() && !expr.has(Flag.CTX) && !expr.has(Flag.NDT);
  }

  @Override
  public final boolean has(final Flag flag) {
    return root.has(flag) || flag != Flag.CTX && super.has(flag);
  }

  @Override
  public final boolean removable(final Var var) {
    return root.removable(var) && super.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    final VarUsage inPreds = super.count(var), inRoot = root.count(var);
    if(inPreds == VarUsage.NEVER) return inRoot;
    final long sz = root.size();
    return sz >= 0 && sz <= 1 || root.seqType().zeroOrOne() ? inRoot.plus(inPreds) :
      VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {

    final Expr rt = root == null ? null : root.inline(qc, scp, var, ex);
    if(rt != null) root = rt;

    final boolean pr = inlineAll(qc, scp, preds, var, ex);
    return pr || rt != null ? optimize(qc, scp) : null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Expr e : preds) {
      visitor.enterFocus();
      if(!e.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return root.accept(visitor);
  }

  @Override
  public final int exprSize() {
    int sz = 1;
    for(final Expr e : preds) sz += e.exprSize();
    return sz + root.exprSize();
  }

  @Override
  public final String toString() {
    return "(" + root + ")" + super.toString();
  }
}
