package org.basex.query.item;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Stores a sequence type definition.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public static final SeqType ITEM = new SeqType(Type.ITEM);
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = new SeqType(Type.ITEM, Occ.ZO);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = new SeqType(Type.ITEM, Occ.ZM);
  /** One or more items. */
  public static final SeqType ITEM_OM = new SeqType(Type.ITEM, Occ.OM);
  /** Single boolean. */
  public static final SeqType BLN = new SeqType(Type.BLN);
  /** Zero or one booleans. */
  public static final SeqType BLN_ZO = new SeqType(Type.BLN, Occ.ZO);
  /** Single Base64Binary. */
  public static final SeqType B64 = new SeqType(Type.B64);
  /** Double number. */
  public static final SeqType DBL = new SeqType(Type.DBL);
  /** Float number. */
  public static final SeqType FLT = new SeqType(Type.FLT);
  /** Single number; for simplicity, numbers are summarized by this type. */
  public static final SeqType ITR = new SeqType(Type.ITR);
  /** Zero or one number. */
  public static final SeqType ITR_ZO = new SeqType(Type.ITR, Occ.ZO);
  /** Zero or more numbers. */
  public static final SeqType ITR_ZM = new SeqType(Type.ITR, Occ.ZM);
  /** One or more numbers. */
  public static final SeqType ITR_OM = new SeqType(Type.ITR, Occ.OM);
  /** Single node. */
  public static final SeqType NOD = new SeqType(Type.NOD);
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(Type.NOD, Occ.ZO);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(Type.NOD, Occ.ZM);
  /** One or more nodes. */
  public static final SeqType NOD_OM = new SeqType(Type.NOD, Occ.OM);
  /** Single QName. */
  public static final SeqType QNM = new SeqType(Type.QNM);
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(Type.QNM, Occ.ZO);
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(Type.URI, Occ.ZO);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = new SeqType(Type.URI, Occ.ZM);
  /** Single URI. */
  public static final SeqType URI = new SeqType(Type.URI);
  /** Single string. */
  public static final SeqType STR = new SeqType(Type.STR);
  /** Zero or one strings. */
  public static final SeqType STR_ZO = new SeqType(Type.STR, Occ.ZO);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = new SeqType(Type.STR, Occ.ZM);
  /** Single date. */
  public static final SeqType DAT = new SeqType(Type.DAT);
  /** Zero or more dates. */
  public static final SeqType DAT_ZM = new SeqType(Type.DAT, Occ.ZM);
  /** Zero or more dates. */
  public static final SeqType BYT_ZM = new SeqType(Type.BYT, Occ.ZM);

  /** Sequence type. */
  public final Type type;
  /** Number of occurrences. */
  public final Occ occ;
  /** Extended type info. */
  public final QNm ext;

  /**
   * Private constructor.
   * @param t type
   * @param o occurrences
   */
  private SeqType(final Type t, final Occ o) {
    this(t, o, null);
  }

  /**
   * Private constructor.
   * @param t type
   * @param o occurrences
   * @param e extension
   */
  private SeqType(final Type t, final Occ o, final QNm e) {
    type = t;
    occ = t == Type.EMP ? Occ.Z : t == Type.SEQ ? Occ.OM : o;
    ext = e;
  }

  /**
   * Constructor. This one is only called by {@link Type#seq} to create
   * unique sequence type instances.
   * @param t type
   */
  SeqType(final Type t) {
    this(t, Occ.O);
  }

  /**
   * Returns a sequence type.
   * @param t type
   * @param o occurrences
   * @return sequence type
   */
  public static SeqType get(final Type t, final Occ o) {
    return get(t, o, null);
  }

  /**
   * Returns a sequence type.
   * @param t type
   * @param o number of occurrences
   * @return sequence type
   */
  public static SeqType get(final Type t, final long o) {
    return get(t, o == 0 ? Occ.Z : o == 1 ? Occ.O : o > 1 ? Occ.OM : Occ.ZM);
  }

  /**
   * Returns a sequence type.
   * @param t type
   * @param o occurrences
   * @param e extension
   * @return sequence type
   */
  public static SeqType get(final Type t, final Occ o, final QNm e) {
    return o == Occ.O && e == null ? t.seq() : new SeqType(t, o, e);
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
      checkExt(it);

    do {
      if(!it.type.instance(type) || !checkExt(it)) return false;
    } while((it = iter.next()) != null);
    return true;
  }

  /**
   * Casts the specified item.
   * @param cast expression to be cast
   * @param expr expression reference
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  public Item cast(final Expr cast, final ParseExpr expr,
      final QueryContext ctx) throws QueryException {

    final Item it = cast.item(ctx, expr.input);
    if(it == null) {
      if(occ == Occ.O) XPEMPTY.thrw(expr.input, expr.desc());
      return null;
    }
    return it.type == type ? it :
      check(type.e(it, ctx, expr.input), expr.input);
  }

  /**
   * Casts the specified value.
   * @param val value to be cast
   * @param ctx query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  public Value cast(final Value val, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    final Iter iter = val.iter(ctx);
    Item it = iter.next();
    if(it == null) {
      if(mayBeZero()) return Empty.SEQ;
      Err.cast(ii, type, val);
    }
    if(type == Type.EMP) Err.cast(ii, type, val);

    it = check(instance(it, ii) ? it : type.e(it, ctx, ii), ii);
    Item n = iter.next();
    if(zeroOrOne() && n != null) Err.cast(ii, type, val);

    final ItemCache ir = new ItemCache();
    ir.add(it);
    while(n != null) {
      ir.add(check(instance(n, ii) ? n : type.e(n, ctx, ii), ii));
      n = iter.next();
    }
    return ir.finish();
  }

  /**
   * Returns if item has already correct type.
   * @param it input item
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean instance(final Item it, final InputInfo ii)
      throws QueryException {
    final boolean ins = it.type.instance(type);
    if(!it.unt() && !ins &&
        // implicit type promotions
        (!it.num() || type != Type.FLT && type != Type.DBL) &&
        (it.type != Type.URI || type != Type.STR)) Err.cast(ii, type, it);
    return ins;
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
    if(!checkExt(it)) XPCAST.thrw(ii, it.type, ext);
    return it;
  }

  /**
   * Checks the sequence extension.
   * @param it item
   * @return same item
   */
  private boolean checkExt(final Item it) {
    return ext == null || ext.eq(((ANode) it).qname());
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
