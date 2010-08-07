package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * FLWR clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FLWR extends FLWOR {
  /**
   * Constructor.
   * @param f variable inputs
   * @param w where clause
   * @param r return expression
   * @param ii input info
   */
  public FLWR(final ForLet[] f, final Expr w, final Expr r,
      final InputInfo ii) {
    super(f, w, null, r, ii);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final Expr ex = super.comp(ctx);
    if(ex != this) return ex;

    // add where clause to most inner FOR clause and remove variable calls
    if(where != null) {
      final ForLet f = fl[fl.length - 1];
      // WHERE results must not be numeric
      if(f instanceof For && f.simple() && where.removable(f.var, ctx) &&
          !where.returned(ctx).mayBeNum()) {

        // convert where clause to predicates
        ctx.compInfo(OPTWHERE);
        final Expr w = where.remove(f.var);
        if(f.expr instanceof AxisPath) {
          AxisPath ap = (AxisPath) f.expr;
          if(w instanceof And) {
            for(final Expr e : ((And) w).expr) ap = ap.addPred(e);
          } else {
            ap = ap.addPred(w);
          }
          f.expr = ap;
        } else {
          f.expr = new Filter(input, f.expr, w);
        }
        where = null;
      }
    }

    // remove LET clauses with static contents
    for(int f = 0; f != fl.length; f++) {
      if(fl[f].var.expr != null) {
        ctx.compInfo(OPTVAR, fl[f].var);
        fl = Array.delete(fl, f--);
      }
    }

    // no clauses left: simplify expression
    if(fl.length == 0) {
      // replace FLWR with IF clause or pass on return clause
      ctx.compInfo(OPTFLWOR);
      return where != null ? new If(input, where, ret, Seq.EMPTY) : ret;
    }

    // simplify basic FLWOR expression (for $i in E return $i -> E)
    if(fl.length == 1 && where == null && fl[0].simple() &&
        ret instanceof VarCall) {
      final Var v = ((VarCall) ret).var;
      if(v.type == null && fl[0].var.eq(v)) {
        ctx.compInfo(OPTFLWOR);
        return fl[0].expr;
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      private Iter[] iter;
      private long[] sizes;
      private Iter rtrn;
      private int p;

      @Override
      public Item next() throws QueryException {
        init();

        while(true) {
          if(rtrn != null) {
            final Item i = rtrn.next();
            if(i != null) return i;
            rtrn = null;
          } else {
            while(iter[p].next() != null) {
              if(p + 1 != fl.length) {
                p++;
              } else if(where == null || where.ebv(ctx, input).bool(input)) {
                rtrn = ctx.iter(ret);
                break;
              }
            }
            if(rtrn == null && p-- == 0) return null;
          }
        }
      }

      @Override
      public long size() throws QueryException {
        // expected: a single return item and a missing where clause
        if(where != null || !ret.returned(ctx).one()) return -1;

        // check if the number of returned items is known for all iterators
        init();
        long s = 1;
        sizes = new long[fl.length];
        for(int f = 0; f != fl.length; f++) {
          sizes[f] = iter[f].size();
          if(sizes[f] == -1) return -1;
          s *= sizes[f];
        }
        return s;
      }

      @Override
      public Item get(final long i) throws QueryException {
        // only called for a single return item and a missing where clause
        long s = 1;
        for(int f = 1; f != fl.length; f++) s *= sizes[f];

        // calculate variable positions and call iterators
        long o = i;
        for(int f = 0; f != fl.length; f++) {
          if(f != 0) s /= sizes[f];
          final long n = o / s;
          iter[f].get(n);
          o -= n * s;
        }
        return ret.atomic(ctx, input);
      }

      @Override
      public boolean reset() {
        if(iter != null) {
          for(final Iter i : iter) i.reset();
          sizes = null;
          iter = null;
          rtrn = null;
          p = 0;
        }
        return true;
      }

      /**
       * Initializes the iterator.
       */
      private void init() throws QueryException {
        if(iter != null) return;
        iter = new Iter[fl.length];
        for(int f = 0; f < fl.length; f++) iter[f] = ctx.iter(fl[f]);
      }
    };
  }
}
