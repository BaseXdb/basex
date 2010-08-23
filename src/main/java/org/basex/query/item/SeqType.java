package org.basex.query.item;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

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
  }

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
  /** Single Base64Binary. */
  public static final SeqType B64 = new SeqType(Type.B6B, Occ.O);
  /** Double number. */
  public static final SeqType DBL = new SeqType(Type.DBL, Occ.O);
  /** Float number. */
  public static final SeqType FLT = new SeqType(Type.FLT, Occ.O);
  /** Single number; for simplicity, numbers are summarized by this type. */
  public static final SeqType ITR = new SeqType(Type.ITR, Occ.O);
  /** Zero or one number. */
  public static final SeqType ITR_ZO = new SeqType(Type.ITR, Occ.ZO);
  /** Zero or more numbers. */
  public static final SeqType ITR_ZM = new SeqType(Type.ITR, Occ.ZM);
  /** One or more numbers. */
  public static final SeqType ITR_OM = new SeqType(Type.ITR, Occ.OM);
  /** Single node. */
  public static final SeqType NOD = new SeqType(Type.NOD, Occ.O);
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(Type.NOD, Occ.ZO);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(Type.NOD, Occ.ZM);
  /** One or more nodes. */
  public static final SeqType NOD_OM = new SeqType(Type.NOD, Occ.OM);
  /** Single QName. */
  public static final SeqType QNM = new SeqType(Type.QNM, Occ.O);
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(Type.QNM, Occ.ZO);
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(Type.URI, Occ.ZO);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = new SeqType(Type.URI, Occ.ZM);
  /** Single URI. */
  public static final SeqType URI = new SeqType(Type.URI, Occ.O);
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
   * Constructor.
   * @param t type
   * @param o number of occurrences
   */
  public SeqType(final Type t, final long o) {
    type = t;
    occ = o == 0 ? Occ.Z : o == 1 ? Occ.O : o > 1 ? Occ.OM : Occ.ZM;
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
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  public Item cast(final Item it, final ParseExpr expr, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    if(it == null) {
      if(occ == Occ.O) Err.or(expr.input, XPEMPTY, expr.desc());
      return null;
    }
    return it.type == type ? it : check(type.e(it, ctx, expr.input), ii);
  }

  /**
   * Casts the specified item.
   * @param item item to be cast
   * @param ctx query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  public Value cast(final Value item, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    final Iter iter = item.iter(ctx);
    Item it = iter.next();
    if(it == null) {
      if(mayBeZero()) return Empty.SEQ;
      Err.cast(ii, type, item);
    }
    if(type == Type.EMP) Err.cast(ii, type, item);

    boolean ins = it.type.instance(type);
    if(!it.unt() && !ins &&
      // implicit type promotions
      (it.type != Type.DEC || type != Type.FLT && type != Type.DBL) &&
      (it.type != Type.URI || type != Type.STR)) Err.cast(ii, type, it);

    it = check(ins ? it : type.e(it, ctx, ii), ii);
    Item n = iter.next();
    if(zeroOrOne() && n != null) Err.cast(ii, type, item);

    final ItemIter ir = new ItemIter();
    ir.add(it);
    while(n != null) {
      ins = n.type.instance(type);
      if(!n.unt() && !ins) Err.cast(ii, type, n);
      ir.add(check(ins ? n : type.e(n, ctx, ii), ii));
      n = iter.next();
    }
    return ir.finish();
  }

  /**
   * Combine two sequence types.
   * @param t second type
   * @return resulting type
   */
  public SeqType intersect(final SeqType t) {
    final Type tp = type == t.type ? type : Type.ITEM;
    final Occ oc = occ == t.occ ? occ : zeroOrOne() && t.zeroOrOne() ?
        Occ.ZO : Occ.ZM;
    return new SeqType(tp, oc);
  }

  /**
   * Returns the number of occurrences, or {@code -1} if the number is unknown.
   * @return result of check
   */
  public long occ() {
    return occ == Occ.Z ? 0 : occ == Occ.O ? 1 : -1;
  }

  /**
   * Tests if the type yields at most item.
   * @return result of check
   */
  public boolean zeroOrOne() {
    return occ != Occ.ZM && occ != Occ.OM;
  }

  /**
   * Tests if the type exactly one item.
   * @return result of check
   */
  public boolean one() {
    return occ == Occ.O;
  }

  /**
   * Tests if the type may yield zero items.
   * @return result of check
   */
  public boolean mayBeZero() {
    return occ != Occ.O && occ != Occ.OM;
  }

  /**
   * Tests if the type is a single number.
   * @return result of check
   */
  public boolean num() {
    return one() && type.num;
  }

  /**
   * Tests if the type may be numeric.
   * @return result of check
   */
  public boolean mayBeNum() {
    return type.num || type == Type.ITEM;
  }

  /**
   * Checks the sequence extension.
   * @param it item
   * @param ii input info
   * @return same item
   * @throws QueryException query exception
   */
  private Item check(final Item it, final InputInfo ii) throws QueryException {
    if(!checkInstance(it)) Err.or(ii, XPCAST, it.type, ext);
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
      occ == Occ.ZM ? "*" : occ == Occ.OM ? "+" : "?");
  }
}
