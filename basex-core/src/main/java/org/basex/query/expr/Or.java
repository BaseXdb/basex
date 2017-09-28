package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * Or expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Or extends Logical {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Or(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr c = super.optimize(cc);
    if(c != this) return c;

    final int es = exprs.length;
    final ExprList list = new ExprList(es);
    for(int e = 0; e < es; e++) {
      // skip identical expressions
      Expr expr = exprs[e];
      if(expr instanceof CmpG) {
        // merge adjacent comparisons
        while(e + 1 < es && exprs[e + 1] instanceof CmpG) {
          final Expr tmp = ((CmpG) expr).union((CmpG) exprs[e + 1], cc);
          if(tmp != null) {
            expr = tmp;
            e++;
          } else {
            break;
          }
        }
      }
      // expression will always return true
      if(expr == Bln.TRUE) return cc.replaceWith(this, Bln.TRUE);
      // skip expression yielding false
      if(expr != Bln.FALSE && !list.contains(expr)) list.add(expr);
    }

    // all arguments return false
    if(list.isEmpty()) return cc.replaceWith(this, Bln.FALSE);

    if(es != list.size()) {
      cc.info(OPTSIMPLE_X, this);
      exprs = list.finish();
    }
    compFlatten(cc);

    boolean not = true;
    for(final Expr expr : exprs) {
      if(!expr.isFunction(Function.NOT)) {
        not = false;
        break;
      }
    }

    if(not) {
      final int el = exprs.length;
      final Expr[] inner = new Expr[el];
      for(int e = 0; e < el; e++) inner[e] = ((Arr) exprs[e]).exprs[0];
      final Expr ex = new And(info, inner).optimize(cc);
      return cc.replaceWith(this, cc.function(Function.NOT, info, ex));
    }

    // return single expression if it yields a boolean
    return exprs.length == 1 ? cc.replaceWith(this, compBln(exprs[0], info, cc.sc())) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // compute scoring
    if(qc.scoring) {
      double s = 0;
      boolean f = false;
      for(final Expr expr : exprs) {
        final Item it = expr.ebv(qc, info);
        f |= it.bool(info);
        s += it.score();
      }
      return Bln.get(f, Scoring.avg(s, exprs.length));
    }

    // standard evaluation
    for(final Expr expr : exprs) {
      if(expr.ebv(qc, info).bool(info)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  @Override
  public Or copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Or(info, copyAll(cc, vm, exprs));
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    int costs = 0;
    final ExprList el = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii)) return false;
      // skip expressions without results
      if(ii.costs == 0) continue;
      costs += ii.costs;
      el.add(ii.expr);
    }
    // use summarized costs for estimation
    ii.costs = costs;
    // no expressions means no costs: expression will later be ignored
    ii.expr = el.size() == 1 ? el.get(0) : new Union(info, el.finish());
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Or && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(' ' + OR + ' ');
  }
}
