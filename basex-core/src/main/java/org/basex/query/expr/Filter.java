package org.basex.query.expr;

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
 * @author BaseX Team 2005-14, BSD License
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
    return path(root, preds) ? ((AxisPath) root).addPreds(preds) :
      new CachedFilter(info, root, preds);
  }

  /**
   * Checks if the specified filter input can be rewritten to a path.
   * @param root root expression
   * @param preds predicate expressions
   * @return filter expression
   */
  private static boolean path(final Expr root, final Expr... preds) {
    if(!(root instanceof AxisPath)) return false;
    // predicate must not be numeric
    for(final Expr pred : preds) {
      if(pred.seqType().mayBeNumber() || pred.has(Flag.FCS)) return false;
    }
    return true;
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
    if(e != this) qc.compInfo(QueryText.OPTWRITE, this);
    return e;
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

    // invalidate current context value (will be overwritten by filter)
    final Value cv = qc.value;
    try {
      qc.value = Path.initial(qc, root);
      final Expr e = super.optimize(qc, scp);
      if(e != this) return e;
    } finally {
      qc.value = cv;
    }

    // no predicates.. return root; otherwise, do some advanced compilations
    if(preds.length == 0) return root;

    // convert filter to path
    if(path(root, preds)) return ((AxisPath) root).addPreds(preds).optimize(qc, scp);

    // determine type and number of results
    final SeqType st = root.seqType();
    final long s = root.size();
    if(s == -1) {
      seqType = SeqType.get(st.type, st.zeroOrOne() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
    } else {
      if(pos != null) {
        size = Math.max(0, s + 1 - pos.min) - Math.max(0, s - pos.max);
      } else if(last) {
        size = s > 0 ? 1 : 0;
      }
      // no results will remain: return empty sequence
      if(size == 0) return optPre(qc);
      seqType = SeqType.get(st.type, size);
    }

    // no numeric predicates.. use simple iterator
    if(!super.has(Flag.FCS)) return copy(new IterFilter(info, root, preds));

    boolean iter = false, index = false;
    if(preds.length == 1) {
      // pre-evaluate if root is value and if one single position() or last() function is specified
      iter = posIterator();
      if(root.isValue()) {
        final Value v = (Value) root;
        if(last) return optPre(SubSeq.get(v, v.size() - 1, 1), qc);
        if(pos != null) return optPre(SubSeq.get(v, pos.min - 1, pos.max - pos.min + 1), qc);
      }

      final Expr pred = preds[0];
      if(num(pred)) {
        // only choose deterministic and context-independent offsets; e.g., skip:
        // (1 to 10)[random:integer(10)]  or  (1 to 10)[.]
        seqType = SeqType.get(seqType.type, Occ.ZERO_ONE);
        index = true;
      } else if(pred instanceof CmpG) {
        // rewrite positional range predicate to fn:subsequence(root, start, end, true())
        final CmpG cmp = (CmpG) pred;
        if(cmp.exprs[0].isFunction(Function.POSITION)) {
          final Expr e2 = cmp.exprs[1];
          if(e2 instanceof Range) {
            final Range r = (Range) e2;
            if(num(r.exprs[0]) && num(r.exprs[1])) {
              qc.compInfo(QueryText.OPTWRITE, this);
              final Expr[] args = { root, r.exprs[0], r.exprs[1], Bln.TRUE };
              return Function.SUBSEQUENCE.get(null, info, args);
            }
          }
        }
      }
    }

    // evaluation of positional predicates
    if(index || iter) return copy(new IterPosFilter(info, index, root, preds));

    // standard iterator
    return get(info, root, preds);
  }

  /**
   * Checks if the specified expression returns a deterministic numeric value.
   * @param expr expression
   * @return result of check
   */
  private static boolean num(final Expr expr) {
    final SeqType pt = expr.seqType();
    return pt.type.isNumber() && pt.zeroOrOne() && !expr.has(Flag.CTX) && !expr.has(Flag.NDT);
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
