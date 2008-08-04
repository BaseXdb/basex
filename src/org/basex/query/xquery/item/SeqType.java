package org.basex.query.xquery.item;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.SeqBuilder;

/**
 * Stores a sequence type definition.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Extended type info. */
  // [CG] XQuery/SeqType: process extended info (currently ignored).
  byte[] ext;
  /** Sequence type. */
  public Type type;
  /** Occurrence mode: 0 = default, 1 = "?", 2 = "+", 3 = "*". */
  public int occ;

  /**
   * Constructor.
   * @param name sequence type
   * @param o occurrences
   * @param e extended type info
   */
  public SeqType(final QNm name, final int o, final byte[] e) {
    type = Type.find(name, e != null);
    ext = e;
    occ = o;
  }

  /**
   * Checks the instance of the specified iterator.
   * @param iter iteration to be checked
   * @param f force instance check for untyped values
   * @return result of check
   * @throws XQException evaluation exception
   */
  public boolean instance(final Iter iter, final boolean f) throws XQException {
    Item it = iter.next();
    if(it == null) return type == Type.EMP || occ % 2 != 0;
    if(occ < 2) return iter.next() == null && (!f && it.u() && !type.node() ||
        it.type.instance(type));

    do {
      if((f || !it.u() || type.node()) && !it.type.instance(type)) return false;
    } while((it = iter.next()) != null);
    return true;
  }

  /**
   * Casts the specified iterator item.
   * @param iter iterator
   * @param expr expression reference
   * @param ctx xquery context
   * @return resulting item
   * @throws XQException evaluation exception
   */
  public Item cast(final Iter iter, final Expr expr, final XQContext ctx)
      throws XQException {

    final Item it = iter.atomic(expr, occ != 0);
    return it == null ? Seq.EMPTY : it.type == type ? it : type.e(it, ctx);
  }

  /**
   * Casts the specified item.
   * @param item item to be cast
   * @param ctx xquery context
   * @return resulting item
   * @throws XQException evaluation exception
   */
  public Item cast(final Item item, final XQContext ctx) throws XQException {
    Iter iter = item.iter();
    Item it = iter.next();
    if(it == null) {
      if(type != Type.EMP && occ % 2 == 0) Err.cast(type, Seq.EMPTY);
      return Seq.EMPTY;
    }

    boolean ins = it.type.instance(type);
    if(type == Type.EMP || !it.u() && !ins) Err.cast(type, it);

    it = ins ? it : type.e(it, ctx);
    Item n = iter.next();
    if(occ < 2 && n != null) Err.cast(type, item);

    final SeqBuilder sb = new SeqBuilder();
    sb.a(it);
    while(n != null) {
      ins = n.type.instance(type);
      if(!n.u() && !ins) Err.cast(type, n);
      sb.a(ins ? n : type.e(n, ctx));
      n = iter.next();
    }
    return sb.finish();
  }

  @Override
  public String toString() {
    return type + (occ == 0 ? "" : occ == 1 ? "?" : occ == 2 ? "+" : "*");
  }
}
