package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;

/**
 * Simple functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FNSimple extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final Iter iter = arg.length != 0 ? arg[0] : null;
    
    switch(func) {
      case TRUE:    return Bln.TRUE.iter();
      case FALSE:   return Bln.FALSE.iter();
      case BOOL:    return Bln.get(iter.ebv().bool()).iter();
      case NOT:     return Bln.get(!iter.ebv().bool()).iter();
      case EMPTY:   return Bln.get(iter.next() == null).iter();
      case EXISTS:  return Bln.get(iter.next() != null).iter();
      case ZEROONE:
        Item it = iter.next();
        if(it == null) return Iter.EMPTY;
        if(iter.next() != null) Err.or(ZEROONE);
        return it.iter();
      case EXONE:
        it = iter.next();
        if(it == null || iter.next() != null) Err.or(EXONE);
        return it.iter();
      case ONEMORE:
        final SeqIter seq = new SeqIter(iter);
        if(seq.size < 1) Err.or(ONEMORE);
        return seq;
      case UNORDER:
        return iter;
      default: throw new RuntimeException("Not defined: " + func);
    }
  }

  @Override
  public Expr comp(final XQContext ctx, final Expr[] arg) {
    switch(func) {
      case TRUE:  return Bln.TRUE;
      case FALSE: return Bln.FALSE;
      case BOOL:  return arg[0] instanceof Bln ? arg[0] : this;
      case NOT:
        if(arg[0] instanceof Fun) {
          Fun fs = (Fun) arg[0];
          if(fs.func == FunDef.EMPTY) {
            Fun f = new FNSimple();
            f.args = fs.args;
            f.func = FunDef.EXISTS;
            return f;
          }
        }
        return this;
      default: return this;
    }
  }
}
