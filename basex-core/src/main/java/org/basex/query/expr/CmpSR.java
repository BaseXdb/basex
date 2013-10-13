package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CmpSR extends Single {
  /** Minimum. */
  private final byte[] min;
  /** Include minimum value. */
  private final boolean mni;
  /** Maximum. */
  private final byte[] max;
  /** Include maximum value. */
  private final boolean mxi;
  /** Index container. */
  private StringRange rt;
  /** Flag for atomic evaluation. */
  private final boolean atomic;

  /**
   * Constructor.
   * @param e (compiled) expression
   * @param mn minimum value
   * @param in include minimum value
   * @param mx maximum value
   * @param ix include maximum value
   * @param ii input info
   */
  private CmpSR(final Expr e, final byte[] mn, final boolean in, final byte[] mx,
      final boolean ix, final InputInfo ii) {

    super(ii, e);
    min = mn;
    mni = in;
    max = mx;
    mxi = ix;
    type = SeqType.BLN;
    atomic = e.type().zeroOrOne();
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param ex expression to be converted
   * @return new or original expression
   * @throws QueryException query exception
   */
  static Expr get(final CmpG ex) throws QueryException {
    if(!(ex.expr[1] instanceof AStr)) return ex;
    final byte[] d = ((AStr) ex.expr[1]).string(ex.info);
    final Expr e = ex.expr[0];
    switch(ex.op.op) {
      case GE: return new CmpSR(e, d, true, null, true, ex.info);
      case GT: return new CmpSR(e, d, false, null, true, ex.info);
      case LE: return new CmpSR(e, null, true, d, true, ex.info);
      case LT: return new CmpSR(e, null, true, d, false, ex.info);
      default: return ex;
    }
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it = expr.item(ctx, info);
      return Bln.get(it != null && eval(it));
    }

    // iterative evaluation
    final Iter ir = ctx.iter(expr);
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
    if(!it.type.isStringOrUntyped()) INVTYPECMP.thrw(info, it.type, AtomType.STR);
    final byte[] s = it.string(info);
    final int mn = min == null ? 1 : Token.diff(s, min);
    final int mx = max == null ? -1 : Token.diff(s, max);
    return (mni ? mn >= 0 : mn > 0) && (mxi ? mx <= 0 : mx < 0);
  }

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param c range comparison
   * @return resulting expression or {@code null}
   */
  Expr intersect(final CmpSR c) {
    // skip intersection if expressions to be compared are different
    if(!c.expr.sameAs(expr)) return null;

    // find common minimum and maximum value
    final byte[] mn = min == null ? c.min : c.min == null ? min : Token.max(min, c.min);
    final byte[] mx = max == null ? c.max : c.max == null ? max : Token.min(max, c.max);

    if(mn != null && mx != null) {
      final int d = Token.diff(mn, mx);
      // remove comparisons that will never yield results
      if(d > 0) return Bln.FALSE;
      if(d == 0) {
        // return simplified comparison for exact hit, or false if value is not included
        return mni && mxi ? new CmpG(expr, Str.get(mn), CmpG.OpG.EQ, info) : Bln.FALSE;
      }
    }
    return new CmpSR(c.expr, mn, mni && c.mni, mx, mxi && c.mxi, info);
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) {
    // accept only location path, string and equality expressions
    final Step s = CmpG.indexStep(expr);
    // no range index support in main-memory index structures
    final Data data = ic.ictx.data;
    if(s == null || data.inMemory()) return false;

    // check which index applies
    final boolean text = s.test.type == NodeType.TXT && data.meta.textindex;
    final boolean attr = s.test.type == NodeType.ATT && data.meta.attrindex;
    if(!text && !attr || min == null || max == null) return false;

    // create range access
    rt = new StringRange(text ? IndexType.TEXT : IndexType.ATTRIBUTE, min, mni, max, mxi);
    ic.costs(Math.max(1, data.meta.size / 10));
    return true;
  }

  @Override
  public Expr indexEquivalent(final IndexCosts ic) {
    final boolean text = rt.type() == IndexType.TEXT;
    ic.ctx.compInfo(OPTSRNGINDEX);
    return ic.invert(expr, new StringRangeAccess(info, rt, ic.ictx), text);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final CmpSR res = new CmpSR(expr.copy(ctx, scp, vs), min, mni, max, mxi, info);
    res.rt = rt;
    return res;
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
