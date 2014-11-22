package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
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
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    // remove atomic values
    final Expr c = super.compile(qc, scp);
    return c != this ? c : optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final int es = exprs.length;
    final ExprList list = new ExprList(es);
    for(int i = 0; i < es; i++) {
      Expr e = exprs[i];
      if(e instanceof Pos) {
        // merge adjacent numeric predicates
        while(i + 1 < es && exprs[i + 1] instanceof Pos) {
          e = ((Pos) e).intersect((Pos) exprs[++i], info);
        }
      } else if(e instanceof CmpR) {
        // merge adjacent range comparisons
        while(i + 1 < es && exprs[i + 1] instanceof CmpR) {
          final Expr tmp = ((CmpR) e).intersect((CmpR) exprs[i + 1]);
          if(tmp != null) {
            e = tmp;
            i++;
          } else {
            break;
          }
        }
      } else if(e instanceof CmpSR) {
        // merge adjacent string range comparisons
        while(i + 1 < es && exprs[i + 1] instanceof CmpSR) {
          final Expr tmp = ((CmpSR) e).intersect((CmpSR) exprs[i + 1]);
          if(tmp != null) {
            e = tmp;
            i++;
          } else {
            break;
          }
        }
      }

      // expression will always return false
      if(e == Bln.FALSE) return optPre(Bln.FALSE, qc);
      // skip expression yielding true
      if(e != Bln.TRUE) list.add(e);
    }

    // all arguments return true
    if(list.isEmpty()) return optPre(Bln.TRUE, qc);

    if(es != list.size()) qc.compInfo(OPTWRITE, this);
    exprs = list.finish();
    compFlatten(qc);

    boolean not = true;
    for(final Expr e : exprs) {
      if(!e.isFunction(Function.NOT)) {
        not = false;
        break;
      }
    }

    if(not) {
      qc.compInfo(OPTWRITE, this);
      final int el = exprs.length;
      final Expr[] inner = new Expr[el];
      for(int e = 0; e < el; e++) inner[e] = ((Arr) exprs[e]).exprs[0];
      final Expr or = new Or(info, inner).optimize(qc, scp);
      return Function.NOT.get(null, info, or).optimize(qc, scp);
    }

    // return single expression if it yields a boolean
    return exprs.length == 1 ? compBln(exprs[0], info) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el - 1; e++) {
      if(!exprs[e].ebv(qc, info).bool(info)) return Bln.FALSE;
    }
    final Expr last = exprs[el - 1];
    return tailCall ? last.item(qc, ii) : last.ebv(qc, ii).bool(ii) ? Bln.TRUE : Bln.FALSE;
  }

  @Override
  public And copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vars) {
    return new And(info, copyAll(qc, scp, vars, exprs));
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    final int es = exprs.length;
    final int[] ics = new int[es];
    final Expr[] tmp = new Expr[es];
    for(int e = 0; e < es; e++) {
      final Expr expr = exprs[e];
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii)) return false;
      // skip queries with no results
      if(ii.costs == 0) return true;
      // summarize costs
      ics[e] = ii.costs;
      tmp[e] = ii.expr;
    }

    // evaluate arguments with higher selectivity first
    final int[] ord = Array.createOrder(ics, true);
    final Expr[] ex = new Expr[es];
    for(int e = 0; e < es; ++e) ex[e] = tmp[ord[e]];
    ii.expr = new InterSect(info, ex);
    // use worst costs for estimation, as all index results may need to be scanned
    ii.costs = ics[ord[es - 1]];
    return true;
  }

  @Override
  public String toString() {
    return toString(' ' + AND + ' ');
  }
}
