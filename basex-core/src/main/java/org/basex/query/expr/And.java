package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * And expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
  public Expr optimize(final CompileContext cc) throws QueryException {
    return optimize(cc, true, (ex) -> new Or(info, ex));
  }

  @Override
  void simplify(final CompileContext cc, final ExprList list) {
    final int es = exprs.length;
    for(int e = 0; e < es; e++) {
      Expr expr = exprs[e];
      if(expr instanceof ItrPos) {
        // merge adjacent positional predicates
        while(e + 1 < es && exprs[e + 1] instanceof ItrPos) {
          expr = ((ItrPos) expr).intersect((ItrPos) exprs[e + 1], info);
          e++;
        }
      } else if(expr instanceof Pos) {
        // merge adjacent positional predicates
        while(e + 1 < es && exprs[e + 1] instanceof Pos) {
          final Expr tmp = ((Pos) expr).intersect((Pos) exprs[e + 1], info);
          if(tmp != null) {
            expr = tmp;
            e++;
          } else {
            break;
          }
        }
      } else if(expr instanceof CmpR) {
        // merge adjacent range comparisons
        while(e + 1 < es && exprs[e + 1] instanceof CmpR) {
          final Expr tmp = ((CmpR) expr).intersect((CmpR) exprs[e + 1]);
          if(tmp != null) {
            expr = tmp;
            e++;
          } else {
            break;
          }
        }
      } else if(expr instanceof CmpSR) {
        // merge adjacent string range comparisons
        while(e + 1 < es && exprs[e + 1] instanceof CmpSR) {
          final Expr tmp = ((CmpSR) expr).intersect((CmpSR) exprs[e + 1]);
          if(tmp != null) {
            expr = tmp;
            e++;
          } else {
            break;
          }
        }
      }
        if(exprs[e] != expr) cc.info(OPTSIMPLE_X, exprs[e]);
      if(!list.contains(expr) || expr.has(Flag.NDT) || expr.has(Flag.UPD)) list.add(expr);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // compute scoring
    if(qc.scoring) {
      double s = 0;
      for(final Expr expr : exprs) {
        final Item it = expr.ebv(qc, info);
        if(!it.bool(info)) return Bln.FALSE;
        s += it.score();
      }
      return Bln.get(true, Scoring.avg(s, exprs.length));
    }

    // standard evaluation
    for(final Expr expr : exprs) {
      if(!expr.ebv(qc, info).bool(info)) return Bln.FALSE;
    }
    return Bln.TRUE;
  }

  @Override
  public And copy(final CompileContext cc, final IntObjMap<Var> vars) {
    return new And(info, copyAll(cc, vars, exprs));
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
    ii.expr = new Intersect(info, ex);
    // use worst costs for estimation, as all index results may need to be scanned
    ii.costs = ics[ord[es - 1]];
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof And && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(' ' + AND + ' ');
  }
}
