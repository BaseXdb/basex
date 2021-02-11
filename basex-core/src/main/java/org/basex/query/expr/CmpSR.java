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
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * String range expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CmpSR extends Single {
  /** Collation (can be {@code null}). */
  private final Collation coll;
  /** Minimum (can be {@code null} if {@link #max} is assigned). */
  private final byte[] min;
  /** Include minimum value. */
  private final boolean mni;
  /** Maximum (can be {@code null} if {@link #min} is assigned). */
  private final byte[] max;
  /** Include maximum value. */
  private final boolean mxi;

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
   * @param info input info
   */
  private CmpSR(final Expr expr, final byte[] min, final boolean mni, final byte[] max,
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
   * @param cmp expression to be converted
   * @param cc compilation context
   * @return new or original expression
   * @throws QueryException query exception
   */
  static Expr get(final CmpG cmp, final CompileContext cc) throws QueryException {
    final Expr cmp1 = cmp.exprs[0], cmp2 = cmp.exprs[1];
    if(cmp1.has(Flag.NDT) || !(cmp2 instanceof AStr)) return cmp;

    final byte[] d = ((AStr) cmp2).string(cmp.info);
    ParseExpr expr = null;
    switch(cmp.op.opV) {
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
      return Bln.get(item != Empty.VALUE && eval(item));
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
    final int mn = min == null ?  1 :
      coll == null ? Token.diff(s, min) : coll.compare(s, min);
    final int mx = max == null ? -1 :
      coll == null ? Token.diff(s, max) : coll.compare(s, max);
    return (mni ? mn >= 0 : mn > 0) && (mxi ? mx <= 0 : mx < 0);
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc) {
    if(or || !(ex instanceof CmpSR)) return null;

    // skip intersection if expressions to be compared are different
    final CmpSR cmp = (CmpSR) ex;
    if(!(coll == null && expr.equals(cmp.expr))) return null;

    // find common minimum and maximum value
    final byte[] mn = min == null ? cmp.min : cmp.min == null ? min : Token.max(min, cmp.min);
    final byte[] mx = max == null ? cmp.max : cmp.max == null ? max : Token.min(max, cmp.max);

    if(mn != null && mx != null) {
      final int d = Token.diff(mn, mx);
      // remove comparisons that will never yield results
      if(d > 0) return Bln.FALSE;
      if(d == 0) {
        // return simplified comparison for exact hit, or false if value is not included
        return mni && mxi ? new CmpG(expr, Str.get(mn), OpG.EQ, null, null, info) : Bln.FALSE;
      }
    }
    return new CmpSR(cmp.expr, mn, mni && cmp.mni, mx, mxi && cmp.mxi, null, info);
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
    ii.costs = ii.costs(data, sr);
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
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max, INCLUDE_MIN, mni, INCLUDE_MAX, mxi,
        SINGLE, single), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    if(min != null) qs.token(expr).token(mni ? ">=" : ">").quoted(min);
    if(min != null && max != null) qs.token(AND);
    if(max != null) qs.token(expr).token(mxi ? "<=" : "<").quoted(max);
  }
}
