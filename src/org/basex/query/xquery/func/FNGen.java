package org.basex.query.xquery.func;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Atm;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.DNode;
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
  /** Document name. */
  private Item docName;
  /** Database instance. */
  private DNode doc;
  
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
        byte[] file = checkStr(it);
        if(docName != it) {
          docName = it;
          doc = ctx.doc(file);
        }
        return doc.iter();
      case DOCAVAIL:
        it = iter.next();
        if(it == null) return Bln.FALSE.iter();
        file = checkStr(it);
        try {
          ctx.doc(file);
          return Bln.TRUE.iter();
        } catch(final XQException e) {
          return Bln.FALSE.iter();
        }
      default:
        throw new RuntimeException("Not defined: " + func);
    }
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
