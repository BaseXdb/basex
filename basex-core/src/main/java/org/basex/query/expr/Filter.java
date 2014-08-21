package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.gflwor.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
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
  protected Filter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, preds);
    this.root = root;
  }

  /**
   * Creates a filter expression for the given root and predicates.
   * @param ii input info
   * @param r root expression
   * @param p predicate expressions
   * @return filter expression
   */
  public static Filter get(final InputInfo ii, final Expr r, final Expr... p) {
    return new CachedFilter(ii, r, p);
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
    final Value cv = qc.value;
    try {
      qc.value = null;
      super.compile(qc, scp);
    } finally {
      qc.value = cv;
    }
    return optimize(qc, scp);
  }

  @Override
  public final Expr optimizeEbv(final QueryContext qc, final VarScope scp) throws QueryException {
    Expr e = merge(root, qc, scp);
    if(e != this) qc.compInfo(QueryText.OPTWRITE, this);
    return e;
  }


  /**
   * Adds a predicate to the filter.
   * This function is e.g. called by {@link For#addPredicate}.
   * @param qc query context
   * @param scp variable scope
   * @param p predicate to be added
   * @return self reference
   * @throws QueryException query exception
   */
  public abstract Filter addPred(final QueryContext qc, final VarScope scp, final Expr p)
      throws QueryException;

  @Override
  public final Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // return empty root
    if(root.isEmpty()) return optPre(qc);
    // convert filters without numeric predicates to axis paths
    if(root instanceof AxisPath && !super.has(Flag.FCS))
      return ((Path) root.copy(qc, scp)).addPreds(qc, scp, preds).compile(qc, scp);

    // invalidate current context value (will be overwritten by filter)
    final Value cv = qc.value;
    try {
      qc.value = null;

      final Expr e = super.optimize(qc, scp);
      if(e != this) return e;

      // no predicates.. return root; otherwise, do some advanced compilations
      if(preds.length == 0) return root;

      // evaluate return type
      final SeqType st = root.seqType();

      // determine number of results and type
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
      if(!super.has(Flag.FCS)) return new IterFilter(this);

      // pre-evaluate if root is value and if one single position() or last() function is specified
      final boolean iter = posIterator();
      if(preds.length == 1 && root.isValue()) {
        final Value v = (Value) root;
        if(last) return optPre(SubSeq.get(v, v.size() - 1, 1), qc);
        if(pos != null) return optPre(SubSeq.get(v, pos.min - 1, pos.max - pos.min + 1), qc);
      }

      // only choose deterministic and context-independent offsets; e.g., skip:
      // (1 to 10)[random:integer(10)]  or  (1 to 10)[.]
      boolean off = false;
      if(preds.length == 1) {
        final Expr p = preds[0];
        final SeqType pt = p.seqType();
        off = pt.type.isNumber() && pt.zeroOrOne() && !p.has(Flag.CTX) && !p.has(Flag.NDT);
        if(off) seqType = SeqType.get(seqType.type, Occ.ZERO_ONE);
      }

      // iterator for simple numeric predicate
      return off || iter ? new IterPosFilter(this, off) : this;
    } finally {
      qc.value = cv;
    }
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

    final boolean pr = super.inline(qc, scp, var, ex) != null;
    final Expr rt = root == null ? null : root.inline(qc, scp, var, ex);
    if(rt != null) root = rt;
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
  public final void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, root);
    super.plan(el);
  }

  @Override
  public final String toString() {
    return root + super.toString();
  }
}
