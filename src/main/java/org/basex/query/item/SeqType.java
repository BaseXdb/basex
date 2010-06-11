package org.basex.query.item;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;

/**
 * Stores a sequence type definition.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Number of occurrences (cardinality). */
  public enum Occ {
    /** Zero. */         Z,
    /** Zero or one. */  ZO,
    /** Exactly one. */  O,
    /** One or more. */  OM,
    /** Zero or more. */ ZM,
  };

  /** Zero items. */
  public static final SeqType ITEM_Z = new SeqType(Type.ITEM, Occ.Z);
  /** Single item. */
  public static final SeqType ITEM = new SeqType(Type.ITEM, Occ.O);
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = new SeqType(Type.ITEM, Occ.ZO);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = new SeqType(Type.ITEM, Occ.ZM);
  /** One or more items. */
  public static final SeqType ITEM_OM = new SeqType(Type.ITEM, Occ.OM);
  /** Single boolean. */
  public static final SeqType BLN = new SeqType(Type.BLN, Occ.O);
  /** Zero or one booleans. */
  public static final SeqType BLN_ZO = new SeqType(Type.BLN, Occ.ZO);
  /** Zero or more booleans. */
  public static final SeqType BLN_ZM = new SeqType(Type.BLN, Occ.ZM);
  /** Single number. */
  public static final SeqType ITR = new SeqType(Type.ITR, Occ.O);
  /** Zero or one number. */
  public static final SeqType ITR_ZO = new SeqType(Type.ITR, Occ.ZO);
  /** Zero or more numbers. */
  public static final SeqType ITR_ZM = new SeqType(Type.ITR, Occ.ZM);
  /** Single node. */
  public static final SeqType NOD = new SeqType(Type.NOD, Occ.O);
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(Type.NOD, Occ.ZO);
  /** Single QName. */
  public static final SeqType QNM = new SeqType(Type.QNM, Occ.O);
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(Type.QNM, Occ.ZO);
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(Type.URI, Occ.ZO);
  /** Single URI. */
  public static final SeqType URI = new SeqType(Type.URI, Occ.O);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(Type.NOD, Occ.ZM);
  /** Single string. */
  public static final SeqType STR = new SeqType(Type.STR, Occ.O);
  /** Zero or one strings. */
  public static final SeqType STR_ZO = new SeqType(Type.STR, Occ.ZO);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = new SeqType(Type.STR, Occ.ZM);
  /** Single date. */
  public static final SeqType DAT = new SeqType(Type.DAT, Occ.O);
  /** Zero or more dates. */
  public static final SeqType DAT_ZM = new SeqType(Type.DAT, Occ.ZO);

  /** Sequence type. */
  public final Type type;
  /** Number of occurrences. */
  public final Occ occ;
  /** Extended type info. */
  public QNm ext;

  /**
   * Constructor.
   * @param t type
   * @param o occurrences
   */
  public SeqType(final Type t, final Occ o) {
    type = t;
    occ = t == Type.EMP ? Occ.Z : o;
  }

  /**
   * Checks the instance of the specified iterator.
   * @param iter iteration to be checked
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean instance(final Iter iter) throws QueryException {
    Item it = iter.next();
    if(it == null) return mayBeZero();
    if(zeroOrOne()) return iter.next() == null && it.type.instance(type) &&
      checkInstance(it);

    do {
      if(!it.type.instance(type) || !checkInstance(it)) return false;
    } while((it = iter.next()) != null);
    return true;
  }

  /**
   * Casts the specified item.
   * @param it item
   * @param expr expression reference
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  public Item cast(final Item it, final Expr expr, final QueryContext ctx)
      throws QueryException {

    if(it == null) {
      if(occ == Occ.O) Err.empty(expr);
      return null;
    }
    // test to disallow "xs:QName(xs:string(...))"
    if(it.type == type) {
      if(it.type == Type.STR) ((Str) it).direct = false;
      return it;
    }
    return check(type.e(it, ctx));
  }

  /**
   * Casts the specified item.
   * @param item item to be cast
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  public Item cast(final Item item, final QueryContext ctx)
      throws QueryException {

    final Iter iter = item.iter();
    Item it = iter.next();
    if(it == null) {
      if(mayBeZero()) return Seq.EMPTY;
      Err.cast(type, item);
    }
    if(type == Type.EMP) Err.cast(type, item);

    boolean ins = it.type.instance(type);
    if(!it.u() && !ins &&
      // implicit type promotions
      (it.type != Type.DEC || type != Type.FLT && type != Type.DBL) &&
      (it.type != Type.URI || type != Type.STR)) Err.cast(type, it);

    it = check(ins ? it : type.e(it, ctx));
    Item n = iter.next();
    if(zeroOrOne() && n != null) Err.cast(type, item);

    final SeqIter si = new SeqIter();
    si.add(it);
    while(n != null) {
      ins = n.type.instance(type);
      if(!n.u() && !ins) Err.cast(type, n);
      si.add(check(ins ? n : type.e(n, ctx)));
      n = iter.next();
    }
    return si.finish();
  }

  /**
   * Returns if the type may occur at most once.
   * @return result of check
   */
  public boolean zeroOrOne() {
    return occ != Occ.ZM && occ != Occ.OM;
  }

  /**
   * Returns if the type may exactly once.
   * @return result of check
   */
  public boolean one() {
    return occ == Occ.O;
  }

  /**
   * Returns if the type may occur 0 times.
   * @return result of check
   */
  public boolean mayBeZero() {
    return occ != Occ.O && occ != Occ.OM;
  }

  /**
   * Returns if the type represents a single number.
   * @return result of check
   */
  public boolean num() {
    return one() && type.num;
  }

  /**
   * Returns if the type may be numeric.
   * @return result of check
   */
  public boolean mayBeNum() {
    return !(type.str || type.node() || type.dur || type.unt ||
        type == Type.BLN);
  }

  /**
   * Checks the sequence extension.
   * @param it item
   * @return same item
   * @throws QueryException query exception
   */
  private Item check(final Item it) throws QueryException {
    if(!checkInstance(it)) Err.or(XPCAST, it.type, ext);
    return it;
  }

  /**
   * Checks the sequence extension.
   * @param it item
   * @return same item
   */
  private boolean checkInstance(final Item it) {
    return ext == null || ext.eq(((Nod) it).qname());
  }

  /**
   * Checks the types for equality.
   * @param t type
   * @return result of check
   */
  public boolean eq(final SeqType t) {
    return type == t.type && occ == t.occ;
  }

  @Override
  public String toString() {
    return type + (occ == Occ.O || occ == Occ.Z ? "" :
      occ == Occ.ZM ? "?" : occ == Occ.OM ? "+" : "*");
  }
}
