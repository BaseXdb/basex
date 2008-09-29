package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.SeqBuilder;
import org.basex.util.Token;

/**
 * Stores a sequence type definition.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Extended type info. */
  public QNm ext;
  /** Sequence type. */
  public Type type;
  /** Occurrence mode: 0 = default, 1 = "?", 2 = "+", 3 = "*". */
  public int occ;

  /**
   * Constructor.
   * @param name sequence type
   * @param o occurrences
   * @param e extended info
   */
  public SeqType(final QNm name, final int o, final boolean e) {
    type = Type.find(name, e);
    occ = o;
  }

  /**
   * Checks the instance of the specified iterator.
   * @param iter iteration to be checked
   * @return result of check
   * @throws XQException evaluation exception
   */
  public boolean instance(final Iter iter) throws XQException {
    Item it = iter.next();
    if(it == null) return type == Type.EMP || occ % 2 != 0;
    if(occ < 2) return iter.next() == null && it.type.instance(type);

    do {
      if(!it.type.instance(type)) return false;
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
    if(it == null) return Seq.EMPTY;
    // tiresome test to disallow "xs:QName(xs:string(...))"
    if(it.type == type) {
      if(it.type == Type.STR) ((Str) it).direct = false;
      return it;
    }
    return check(type.e(it, ctx));
  }

  /**
   * Casts the specified item.
   * @param item item to be cast
   * @param ctx xquery context
   * @return resulting item
   * @throws XQException evaluation exception
   */
  public Item cast(final Item item, final XQContext ctx) throws XQException {
    final Iter iter = item.iter();
    Item it = iter.next();
    if(it == null) {
      if(type == Type.EMP || occ % 2 != 0) return Seq.EMPTY;
      Err.cast(type, Seq.EMPTY);
    }

    boolean ins = it.type.instance(type);
    if(type == Type.EMP || !it.u() && !ins) Err.cast(type, it);

    it = check(ins ? it : type.e(it, ctx));
    Item n = iter.next();
    if(occ < 2 && n != null) Err.cast(type, item);

    final SeqBuilder sb = new SeqBuilder();
    sb.a(it);
    while(n != null) {
      ins = n.type.instance(type);
      if(!n.u() && !ins) Err.cast(type, n);
      sb.a(check(ins ? n : type.e(n, ctx)));
      n = iter.next();
    }
    return sb.finish();
  }
  
  /**
   * Checks the sequence extension. 
   * @param it item
   * @return same item
   * @throws XQException exception
   */
  private Item check(final Item it) throws XQException {
    if(ext != null) {
      switch(type) {
        case PI:
          if(!Token.eq(ext.str(), ((Nod) it).nname()))
              Err.or(XPCAST, it.type, ext);
          break;
        // [CG] check other types, similar to {@link Test} classes
        default:
      }
      //Err.cast(type, item);
    }
    return it;
  }

  @Override
  public String toString() {
    return type + (occ == 0 ? "" : occ == 1 ? "?" : occ == 2 ? "+" : "*");
  }
}
