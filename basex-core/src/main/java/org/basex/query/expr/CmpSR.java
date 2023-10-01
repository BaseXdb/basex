package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.index.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * String range expression.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CmpSR extends Single {
  /** Collation (can be {@code null}). */
  final Collation coll;
  /** Minimum (can be {@code null} if {@link #max} is assigned). */
  final byte[] min;
  /** Include minimum value. */
  final boolean mni;
  /** Maximum (can be {@code null} if {@link #min} is assigned). */
  final byte[] max;
  /** Include maximum value. */
  final boolean mxi;

  /** Flag for atomic evaluation. */
  private boolean single;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value (can be {@code null} if {@code max} is assigned)
   * @param mni include minimum value
   * @param max maximum value (can be {@code null} if {@code min} is assigned)
   * @param mxi include maximum value
   * @param coll collation (can be {@code null})
   * @param info input info (can be {@code null})
   */
  CmpSR(final Expr expr, final byte[] min, final boolean mni, final byte[] max,
      final boolean mxi, final Collation coll, final InputInfo info) {

    super(info, expr, SeqType.BOOLEAN_O);
    this.coll = coll;
    this.min = min;
    this.mni = mni;
    this.max = max;
    this.mxi = mxi;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.STRING, cc);

    final SeqType st = expr.seqType();
    single = st.zeroOrOne() && !st.mayBeArray();

    return expr instanceof Value ? cc.preEval(this) : this;
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cc compilation context
   * @param cmp expression to be converted
   * @return new or original expression
   * @throws QueryException query exception
   */
  static Expr get(final CompileContext cc, final CmpG cmp) throws QueryException {
    final Expr cmp1 = cmp.exprs[0], cmp2 = cmp.exprs[1];
    if(cmp1.has(Flag.NDT) || !(cmp2 instanceof AStr)) return cmp;

    final byte[] d = ((AStr) cmp2).string(cmp.info);
    ParseExpr expr = null;
    switch(cmp.op.value()) {
      case GE: expr = new CmpSR(cmp1, d,    true,  null, true,  cmp.coll, cmp.info); break;
      case GT: expr = new CmpSR(cmp1, d,    false, null, true,  cmp.coll, cmp.info); break;
      case LE: expr = new CmpSR(cmp1, null, true,  d,    true,  cmp.coll, cmp.info); break;
      case LT: expr = new CmpSR(cmp1, null, true,  d,    false, cmp.coll, cmp.info); break;
      default:
    }
    return expr != null ? expr.optimize(cc) : cmp;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(single) {
      final Item item = expr.item(qc, info);
      return Bln.get(!item.isEmpty() && eval(item));
    }

    // iterative evaluation
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(eval(item)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Evaluates the range for the specified item.
   * @param item item to be evaluated
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean eval(final Item item) throws QueryException {
    if(!item.type.isStringOrUntyped()) throw diffError(item, Str.EMPTY, info);
    final byte[] s = item.string(info);
    final int mn = min == null ?  1 : Token.diff(s, min, coll);
    final int mx = max == null ? -1 : Token.diff(s, max, coll);
    return (mni ? mn >= 0 : mn > 0) && (mxi ? mx <= 0 : mx < 0);
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {

    Collation cl = null;
    byte[] newMin = null, newMax = null;
    boolean newMni = true, newMxi = true;
    if(ex instanceof CmpSR) {
      final CmpSR cmp = (CmpSR) ex;
      newMin = cmp.min;
      newMax = cmp.max;
      newMni = cmp.mni;
      newMxi = cmp.mxi;
      cl = cmp.coll;
    } else if(ex instanceof CmpG) {
      final CmpG cmp = (CmpG) ex;
      if(cmp.op == OpG.EQ && cmp.exprs[1] instanceof Str) {
        newMin = ((Str) cmp.exprs[1]).string();
        newMax = newMin;
        cl = cmp.coll;
      }
    }
    if(newMin == null && newMax == null || !expr.equals(ex.arg(0)) ||
        cl != null || coll != null || or) return null;

    // determine common minimum and maximum value
    if(newMin == null) {
      newMin = min;
      newMni = mni;
    } else if(min != null) {
      newMin = Token.max(min, newMin);
      newMni = Token.eq(min, newMin) ? mni : newMni;
    }
    if(newMax == null) {
      newMax = max;
      newMxi = mxi;
    } else if(max != null) {
      newMax = Token.min(max, newMax);
      newMxi = Token.eq(max, newMax) ? mxi : newMxi;
    }

    if(newMin != null && newMax != null) {
      final int diff = Token.diff(newMin, newMax);
      // return comparison for exact hit
      if(diff == 0 && newMni && newMxi) return
        new CmpG(info, expr, Str.get(newMin), OpG.EQ, null, cc.sc()).optimize(cc);
      // remove comparisons that will never yield results
      if(diff >= 0) return Bln.FALSE;
    }
    return new CmpSR(expr, newMin, newMni, newMax, newMxi, null, info).optimize(cc);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // only default collation is supported, and min/max values are required
    if(coll != null || min == null || max == null) return false;

    // accept only location path, string and equality expressions
    final Data data = ii.db.data();
    // sequential main memory scan is usually faster than range index access
    if(data == null ? !ii.enforce() : data.inMemory()) return false;

    final IndexType type = ii.type(expr, null);
    if(type == null) return false;

    // create range access
    final StringRange sr = new StringRange(type, min, mni, max, mxi);
    ii.costs = IndexInfo.costs(data, sr);
    if(ii.costs == null) return false;

    final TokenBuilder tb = new TokenBuilder();
    tb.add(mni ? '[' : '(').add(min).add(',').add(max).add(mxi ? ']' : ')');
    ii.create(new StringRangeAccess(info, sr, ii.db), true,
        Util.info(OPTINDEX_X_X, type + " string range", tb), info);
    return true;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpSR cmp = new CmpSR(expr.copy(cc, vm), min, mni, max, mxi, coll, info);
    cmp.single = single;
    return copyType(cmp);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof CmpSR)) return false;
    final CmpSR c = (CmpSR) obj;
    return Token.eq(min, c.min) && mni == c.mni && Token.eq(max, c.max) && mxi && c.mxi &&
        Objects.equals(coll, c.coll) && super.equals(obj);
  }

  @Override
  public String description() {
    return "string range comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max, INCLUDE_MIN, mni, INCLUDE_MAX, mxi,
        SINGLE, single), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    if(min != null) qs.token(expr).token(mni ? ">= " : "> ").quoted(min);
    if(min != null && max != null) qs.token(AND);
    if(max != null) qs.token(expr).token(mxi ? "<= " : "< ").quoted(max);
  }
}
