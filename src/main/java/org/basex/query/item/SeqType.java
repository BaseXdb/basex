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
  /** Occurrence: exactly one. */
  public static final byte OCC_1 = 0;
  /** Occurrence: zero or one. */
  public static final byte OCC_01 = 1;
  /** Occurrence: one or more. */
  public static final byte OCC_1M = 2;
  /** Occurrence: zero or more. */
  public static final byte OCC_0M = 3;

  /** Zero or one items. */
  public static final SeqType ITEM = new SeqType(Type.ITEM, OCC_01);
  /** Zero or more items. */
  public static final SeqType ITEM_0M = new SeqType(Type.ITEM, OCC_0M);
  /** One or more items. */
  public static final SeqType ITEM_1M = new SeqType(Type.ITEM, OCC_1M);
  /** Single boolean. */
  public static final SeqType BLN = new SeqType(Type.BLN, OCC_1);
  /** Zero or one booleans. */
  public static final SeqType BLN_01 = new SeqType(Type.BLN, OCC_01);
  /** Zero or more booleans. */
  public static final SeqType BLN_0M = new SeqType(Type.BLN, OCC_0M);
  /** Single number. */
  public static final SeqType ITR = new SeqType(Type.ITR, OCC_1);
  /** Zero or one number. */
  public static final SeqType ITR_01 = new SeqType(Type.ITR, OCC_01);
  /** Zero or more numbers. */
  public static final SeqType ITR_0M = new SeqType(Type.ITR, OCC_0M);
  /** Single node. */
  public static final SeqType NOD = new SeqType(Type.NOD, OCC_1);
  /** Zero or one nodes. */
  public static final SeqType NOD_01 = new SeqType(Type.NOD, OCC_01);
  /** Single QName. */
  public static final SeqType QNM = new SeqType(Type.QNM, OCC_1);
  /** Zero or one QNames. */
  public static final SeqType QNM_01 = new SeqType(Type.QNM, OCC_01);
  /** Zero or one URIs. */
  public static final SeqType URI_01 = new SeqType(Type.URI, OCC_01);
  /** Single URI. */
  public static final SeqType URI = new SeqType(Type.URI, OCC_1);
  /** Zero or more nodes. */
  public static final SeqType NOD_0M = new SeqType(Type.NOD, OCC_0M);
  /** Single string. */
  public static final SeqType STR = new SeqType(Type.STR, OCC_1);
  /** Zero or one strings. */
  public static final SeqType STR_01 = new SeqType(Type.STR, OCC_01);
  /** Zero or more strings. */
  public static final SeqType STR_0M = new SeqType(Type.STR, OCC_0M);
  /** Single date. */
  public static final SeqType DAT = new SeqType(Type.DAT, OCC_1);
  /** Zero or more dates. */
  public static final SeqType DAT_01 = new SeqType(Type.DAT, OCC_01);

  /** Sequence type. */
  public final Type type;
  /** Number of occurrences. */
  public final byte occ;
  /** Extended type info. */
  public QNm ext;

  /**
   * Constructor.
   * @param t type
   * @param o occurrences
   */
  public SeqType(final Type t, final byte o) {
    type = t;
    occ = o;
  }

  /**
   * Constructor.
   * @param name sequence type
   * @param o occurrences
   * @param e extended info
   */
  public SeqType(final QNm name, final byte o, final boolean e) {
    this(Type.find(name, e), o);
  }

  /**
   * Checks the instance of the specified iterator.
   * @param iter iteration to be checked
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean instance(final Iter iter) throws QueryException {
    Item it = iter.next();
    if(it == null) return type == Type.EMP || mayBeZero();
    if(single()) return iter.next() == null && it.type.instance(type) &&
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
      if(occ == OCC_1) Err.empty(expr);
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
      if(type == Type.EMP || mayBeZero()) return Seq.EMPTY;
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
    if(single() && n != null) Err.cast(type, item);

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
  public boolean single() {
    return occ < 2;
  }

  /**
   * Returns if the type may occur 0 times.
   * @return result of check
   */
  public boolean mayBeZero() {
    return occ % 2 != 0;
  }

  /**
   * Returns if the type represents a single number.
   * @return result of check
   */
  public boolean num() {
    return type.num && occ == OCC_1;
  }

  /**
   * Returns if the type represents no number.
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
    return type + (occ == OCC_1 ? "" : occ == OCC_01 ? "?" :
      occ == OCC_1M ? "+" : "*");
  }
}
