package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;

/**
 * Simple functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FNSimple extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case ONEORMORE:
        final Iter ir = SeqIter.get(ctx.iter(expr[0]));
        if(ir.size() < 1) Err.or(EXP1M);
        return ir;
      case UNORDER:
        return ctx.iter(expr[0]);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Expr e = expr.length == 1 ? expr[0] : null;
    switch(func) {
      case FALSE:   return Bln.FALSE;
      case TRUE:    return Bln.TRUE;
      case EMPTY:   return Bln.get(!e.i() && e.iter(ctx).next() == null);
      case EXISTS:  return Bln.get(e.i() || e.iter(ctx).next() != null);
      case BOOLEAN: return Bln.get(e.ebv(ctx).bool());
      case NOT:     return Bln.get(!e.ebv(ctx).bool());
      case ZEROORONE:
        Iter iter = e.iter(ctx);
        Item it = iter.next();
        if(it == null) return null;
        if(iter.next() != null) Err.or(EXP01);
        return it;
      case EXACTLYONE:
        iter = e.iter(ctx);
        it = iter.next();
        if(it == null || iter.next() != null) Err.or(EXP1);
        return it;
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    final SeqType s = expr.length == 1 ? expr[0].returned(ctx) : null;
    
    switch(func) {
      case FALSE:
      case TRUE:
        return atomic(ctx);
      case EMPTY:
      case EXISTS:
      case BOOLEAN:
        return expr[0].e() || expr[0].i() ? atomic(ctx) : this;
      case NOT:
        if(expr[0].i()) return atomic(ctx);
        if(expr[0] instanceof Fun) {
          final Fun fs = (Fun) expr[0];
          if(fs.func == FunDef.EMPTY) {
            expr = fs.expr;
            func = FunDef.EXISTS;
          } else if(fs.func == FunDef.EXISTS) {
            expr = fs.expr;
            func = FunDef.EMPTY;
          }
        }
        return this;
      case ZEROORONE:
        return s.single() ? expr[0] : this;
      case EXACTLYONE:
        return s.occ == SeqType.OCC_1 ? expr[0] : this;
      case ONEORMORE:
        return s.occ == SeqType.OCC_1 || s.occ == SeqType.OCC_1M ?
            expr[0] : this;
      case UNORDER:
        return expr[0];
      default:
        return this;
    }
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    final Type t = expr.length == 1 ? expr[0].returned(ctx).type : null;
    if(func == FunDef.ZEROORONE)  return new SeqType(t, SeqType.OCC_01);
    if(func == FunDef.EXACTLYONE) return new SeqType(t, SeqType.OCC_1);
    if(func == FunDef.ONEORMORE)  return new SeqType(t, SeqType.OCC_1M);
    return super.returned(ctx);
  }
}
