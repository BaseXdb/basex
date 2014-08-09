package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * String range expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CmpSR extends Single {
  /** Collation. */
  private final Collation coll;
  /** Minimum. */
  private final byte[] min;
  /** Include minimum value. */
  private final boolean mni;
  /** Maximum. */
  private final byte[] max;
  /** Include maximum value. */
  private final boolean mxi;
  /** Flag for atomic evaluation. */
  private final boolean atomic;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param mni include minimum value
   * @param max maximum value
   * @param mxi include maximum value
   * @param coll collation
   * @param info input info
   */
  private CmpSR(final Expr expr, final byte[] min, final boolean mni, final byte[] max,
      final boolean mxi, final Collation coll, final InputInfo info) {

    super(info, expr);
    this.coll = coll;
    this.min = min;
    this.mni = mni;
    this.max = max;
    this.mxi = mxi;
    seqType = SeqType.BLN;
    atomic = expr.seqType().zeroOrOne();
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cmp expression to be converted
   * @return new or original expression
   * @throws QueryException query exception
   */
  static Expr get(final CmpG cmp) throws QueryException {
    if(!(cmp.exprs[1] instanceof AStr)) return cmp;
    final byte[] d = ((Item) cmp.exprs[1]).string(cmp.info);
    final Expr e = cmp.exprs[0];
    if(e.seqType().mayBeArray()) return cmp;
    switch(cmp.op.op) {
      case GE: return new CmpSR(e, d,    true,  null, true,  cmp.coll, cmp.info);
      case GT: return new CmpSR(e, d,    false, null, true,  cmp.coll, cmp.info);
      case LE: return new CmpSR(e, null, true,  d,    true,  cmp.coll, cmp.info);
      case LT: return new CmpSR(e, null, true,  d,    false, cmp.coll, cmp.info);
      default: return cmp;
    }
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it = expr.item(qc, info);
      return Bln.get(it != null && eval(it));
    }

    // iterative evaluation
    final Iter ir = qc.iter(expr);
    for(Item it; (it = ir.next()) != null;) {
      if(eval(it)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Evaluates the range for the specified item.
   * @param it item to be evaluated
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean eval(final Item it) throws QueryException {
    if(!it.type.isStringOrUntyped()) throw diffError(info, it, it);
    final byte[] s = it.string(info);
    final int mn = min == null ?  1 :
      coll == null ? Token.diff(s, min) : coll.compare(s, min);
    final int mx = max == null ? -1 :
      coll == null ? Token.diff(s, max) : coll.compare(s, max);
    return (mni ? mn >= 0 : mn > 0) && (mxi ? mx <= 0 : mx < 0);
  }

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param c range comparison
   * @return resulting expression or {@code null}
   */
  Expr intersect(final CmpSR c) {
    // skip intersection if expressions to be compared are different
    if(!(coll == null && c.expr.sameAs(expr))) return null;

    // find common minimum and maximum value
    final byte[] mn = min == null ? c.min : c.min == null ? min : Token.max(min, c.min);
    final byte[] mx = max == null ? c.max : c.max == null ? max : Token.min(max, c.max);

    if(mn != null && mx != null) {
      final int d = Token.diff(mn, mx);
      // remove comparisons that will never yield results
      if(d > 0) return Bln.FALSE;
      if(d == 0) {
        // return simplified comparison for exact hit, or false if value is not included
        return mni && mxi ? new CmpG(expr, Str.get(mn), CmpG.OpG.EQ, null, info) : Bln.FALSE;
      }
    }
    return new CmpSR(c.expr, mn, mni && c.mni, mx, mxi && c.mxi, null, info);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
    // only default collation is supported, and min/max values are required
    if(coll != null || min == null || max == null) return false;

    // accept only location path, string and equality expressions
    final Data data = ii.ic.data;
    // no range index support in main-memory index structures
    if(data.inMemory() || !ii.check(expr, false)) return false;

    // create range access
    final StringRange sr = new StringRange(ii.text, min, mni, max, mxi);
    ii.costs = Math.max(1, data.meta.size / 10);
    final TokenBuilder tb = new TokenBuilder();
    tb.add(mni ? '[' : '(').addExt(min).add(',').addExt(max).add(mxi ? ']' : ')');
    ii.create(new StringRangeAccess(info, sr, ii.ic), info, Util.info(OPTSRNGINDEX, tb), true);
    return true;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpSR(expr.copy(qc, scp, vs), min, mni, max, mxi, coll, info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, min != null ? min : "",
      MAX, max != null ? max : ""), expr);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(min != null) tb.add('"').add(min).add('"').add(mni ? " <= " : " < ");
    tb.addExt(expr);
    if(max != null) tb.add(mxi ? " <= " : " < ").add('"').add(max).add('"');
    return tb.toString();
  }
}
