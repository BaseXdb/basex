package org.basex.query.xquery.func;

import org.basex.BaseX;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Atm;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;

/**
 * Generating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNGen extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final Iter iter = arg.length != 0 ? arg[0] : null;

    switch(func) {
      case DATA:
        final SeqIter seq = new SeqIter();
        Item it;
        while((it = iter.next()) != null) seq.add(atom(it));
        return seq;
      case COLLECT:
        it = iter != null ? iter.next() : null;
        if(iter != null && it == null) Err.empty(this);
        return ctx.coll(iter == null ? null : checkStr(it));
      case DOC:
        it = iter.next();
        if(it == null) return Iter.EMPTY;
        if(it.type == Type.DOC) return it.iter();
        return ctx.doc(checkStr(it), false).iter();
      case DOCAVAIL:
        it = iter.next();
        if(it == null) return Bln.FALSE.iter();
        final byte[] file = checkStr(it);
        try {
          ctx.doc(file, false);
          return Bln.TRUE.iter();
        } catch(final XQException e) {
          return Bln.FALSE.iter();
        }
      default:
        BaseX.notexpected(func); return null;
    }
  }

  @Override
  public Expr c(final XQContext ctx) throws XQException {
    // ..slows it down if the document will not be used, 
    // but allows more optimizations
    if(func == FunDef.DOC) {
      if(!args[0].i()) return this;
      final Item it = (Item) args[0];
      return it.type == Type.DOC ? it : ctx.doc(checkStr(it), false);
    }
    return this;
  }

  /**
   * Atomizes the specified item.
   * @param it input item
   * @return atomized item
   */
  static Item atom(final Item it) {
    return it.node() ? it.type == Type.PI || it.type == Type.COM ?
        Str.get(it.str()) : new Atm(it.str()) : it;
  }
}
