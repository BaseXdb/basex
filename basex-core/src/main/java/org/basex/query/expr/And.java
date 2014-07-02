package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * And expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class And extends Logical {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public And(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    // remove atomic values
    final Expr c = super.compile(ctx, scp);
    return c != this ? c : optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // merge predicates if possible
    final int es = exprs.length;
    final ExprList el = new ExprList(es);
    Pos ps = null;
    CmpR cr = null;
    CmpSR cs = null;
    for(final Expr e : exprs) {
      Expr tmp = null;
      if(e instanceof Pos) {
        // merge numeric predicates
        tmp = ps == null ? e : ps.intersect((Pos) e, info);
        if(!(tmp instanceof Pos)) return tmp;
        ps = (Pos) tmp;
      } else if(e instanceof CmpR) {
        // merge comparisons
        tmp = cr == null ? e : cr.intersect((CmpR) e);
        if(tmp instanceof CmpR) cr = (CmpR) tmp;
        else if(tmp != null) return tmp;
      } else if(e instanceof CmpSR) {
        // merge comparisons
        tmp = cs == null ? e : cs.intersect((CmpSR) e);
        if(tmp instanceof CmpSR) cs = (CmpSR) tmp;
        else if(tmp != null) return tmp;
      }
      // no optimization found; add original expression
      if(tmp == null && e != Bln.TRUE) {
        if(e == Bln.FALSE) return optPre(Bln.FALSE, ctx);
        el.add(e);
      }
    }
    if(ps != null) el.add(ps);
    if(cr != null) el.add(cr);
    if(cs != null) el.add(cs);

    // all arguments were true()
    if(el.isEmpty()) return optPre(Bln.TRUE, ctx);

    if(es != el.size()) ctx.compInfo(OPTWRITE, this);
    exprs = el.finish();
    compFlatten(ctx);

    boolean not = true;
    for(final Expr e : exprs) {
      if(!e.isFunction(Function.NOT)) {
        not = false;
        break;
      }
    }

    if(not) {
      ctx.compInfo(OPTWRITE, this);
      final Expr[] inner = new Expr[exprs.length];
      for(int i = 0; i < inner.length; i++) inner[i] = ((Arr) exprs[i]).exprs[0];
      final Expr or = new Or(info, inner).optimize(ctx, scp);
      return Function.NOT.get(null, or).optimize(ctx, scp);
    }

    // return single expression if it yields a boolean
    return exprs.length == 1 ? compBln(exprs[0], info) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    for(int i = 0; i < exprs.length - 1; i++)
      if(!exprs[i].ebv(ctx, info).bool(info)) return Bln.FALSE;
    final Expr last = exprs[exprs.length - 1];
    return tailCall ? last.item(ctx, ii) : last.ebv(ctx, ii).bool(ii) ? Bln.TRUE : Bln.FALSE;
  }

  @Override
  public And copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vars) {
    final int es = exprs.length;
    final Expr[] ex = new Expr[es];
    for(int i = 0; i < es; i++) ex[i] = exprs[i].copy(ctx, scp, vars);
    return new And(info, ex);
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) throws QueryException {
    int is = 0;
    final int es = exprs.length;
    final int[] ics = new int[es];
    boolean ia = true;
    for(int e = 0; e < es; ++e) {
      if(exprs[e].indexAccessible(ic) && !ic.seq) {
        // skip queries with no results
        if(ic.costs() == 0) return true;
        // summarize costs
        ics[e] = ic.costs();
        if(is == 0 || ic.costs() < is) is = ic.costs();
      } else {
        ia = false;
      }
    }

    if(ia) {
      // evaluate arguments with high selectivity first
      final int[] ord = Array.createOrder(ics, true);
      final Expr[] ex = new Expr[es];
      for(int e = 0; e < es; ++e) ex[e] = exprs[ord[e]];
      exprs = ex;
    }

    ic.costs(is);
    return ia;
  }

  @Override
  public Expr indexEquivalent(final IndexCosts ic) throws QueryException {
    super.indexEquivalent(ic);
    return new InterSect(info, exprs);
  }

  @Override
  public String toString() {
    return toString(' ' + AND + ' ');
  }
}
