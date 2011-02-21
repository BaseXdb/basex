package org.basex.query.item;

import static org.basex.query.util.Err.*;
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
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Number of occurrences (cardinality). */
  public enum Occ {
    /** Zero.         */  Z(0, 0, ""),
    /** Zero or one.  */ ZO(0, 1, "?"),
    /** Exactly one.  */  O(1, 1, ""),
    /** One or more.  */ OM(1, Integer.MAX_VALUE, "+"),
    /** Zero or more. */ ZM(0, Integer.MAX_VALUE, "*");

    /** String representation. */
    private final String str;
    /** Minimal number of occurrences. */
    public final int min;
    /** Maximal number of occurrences. */
    public final int max;

    /**
     * Constructor.
     * @param mn minimal number of occurrences
     * @param mx maximal number of occurrences
     * @param s string representation
     */
    Occ(final int mn, final int mx, final String s) {
      min = mn;
      max = mx;
      str = s;
    }

    /**
     * Checks if the specified occurrence indicator is an instance of the
     * current occurrence indicator.
     * @param o occurrence indicator to check
     * @return result of check
     */
    public boolean instance(final Occ o) {
      return min <= o.min && max >= o.max;
    }

    @Override
    public String toString() {
      return str;
    }
  }

  /** Zero items. */
  public static final SeqType ITEM_Z = new SeqType(AtomType.ITEM, Occ.Z);
  /** Single item. */
  public static final SeqType ITEM = new SeqType(AtomType.ITEM);
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = new SeqType(AtomType.ITEM, Occ.ZO);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = new SeqType(AtomType.ITEM, Occ.ZM);
  /** One or more items. */
  public static final SeqType ITEM_OM = new SeqType(AtomType.ITEM, Occ.OM);
  /** Single boolean. */
  public static final SeqType BLN = new SeqType(AtomType.BLN);
  /** Zero or one booleans. */
  public static final SeqType BLN_ZO = new SeqType(AtomType.BLN, Occ.ZO);
  /** Single Base64Binary. */
  public static final SeqType B64 = new SeqType(AtomType.B6B);
  /** Double number. */
  public static final SeqType DBL = new SeqType(AtomType.DBL);
  /** Float number. */
  public static final SeqType FLT = new SeqType(AtomType.FLT);
  /** Single number; for simplicity, numbers are summarized by this type. */
  public static final SeqType ITR = new SeqType(AtomType.ITR);
  /** Zero or one number. */
  public static final SeqType ITR_ZO = new SeqType(AtomType.ITR, Occ.ZO);
  /** Zero or more numbers. */
  public static final SeqType ITR_ZM = new SeqType(AtomType.ITR, Occ.ZM);
  /** One or more numbers. */
  public static final SeqType ITR_OM = new SeqType(AtomType.ITR, Occ.OM);
  /** Single node. */
  public static final SeqType NOD = new SeqType(NodeType.NOD);
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(NodeType.NOD, Occ.ZO);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(NodeType.NOD, Occ.ZM);
  /** One or more nodes. */
  public static final SeqType NOD_OM = new SeqType(NodeType.NOD, Occ.OM);
  /** Single QName. */
  public static final SeqType QNM = new SeqType(AtomType.QNM);
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(AtomType.QNM, Occ.ZO);
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(AtomType.URI, Occ.ZO);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = new SeqType(AtomType.URI, Occ.ZM);
  /** Single URI. */
  public static final SeqType URI = new SeqType(AtomType.URI);
  /** Single string. */
  public static final SeqType STR = new SeqType(AtomType.STR);
  /** Zero or one strings. */
  public static final SeqType STR_ZO = new SeqType(AtomType.STR, Occ.ZO);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = new SeqType(AtomType.STR, Occ.ZM);
  /** Single date. */
  public static final SeqType DAT = new SeqType(AtomType.DAT);
  /** Zero or more dates. */
  public static final SeqType DAT_ZM = new SeqType(AtomType.DAT, Occ.ZO);

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
    occ = t == AtomType.EMP ? Occ.Z : t == AtomType.SEQ ? Occ.OM : o;
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
      checkExtension(it);

    do {
      if(!it.type.instance(type) || !checkExtension(it)) return false;
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
      if(occ == Occ.O) XPEMPTY.thrw(expr.input, expr.desc());
      return null;
    }
    return it.type == type ? it : check(type.e(it, ctx, expr.input), ii);
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
    if(type == AtomType.EMP) Err.cast(ii, type, val);

    it = check(instance(it, ii) ? it : type.e(it, ctx, ii), ii);
    Item n = iter.next();
    if(zeroOrOne() && n != null) Err.cast(ii, type, val);

    final ItemIter ir = new ItemIter();
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
        (!it.num() || type != AtomType.FLT && type != AtomType.DBL) &&
        (it.type != AtomType.URI || type != AtomType.STR))
      Err.cast(ii, type, it);
    return ins;
  }

  /**
   * Combine two sequence types.
   * @param t second type
   * @return resulting type
   */
  public SeqType intersect(final SeqType t) {
    final Type tp = type == t.type ? type : AtomType.ITEM;
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
   * Tests if the type yields at most one item.
   * @return result of check
   */
  public boolean zeroOrOne() {
    return occ.max <= 1;
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
    return occ.min == 0;
  }

  /**
   * Tests if the type is a single number.
   * @return result of check
   */
  public boolean num() {
    return one() && type.num();
  }

  /**
   * Tests if the type may be numeric.
   * @return result of check
   */
  public boolean mayBeNum() {
    return type.num() || type == AtomType.ITEM;
  }

  /**
   * Checks the sequence extension.
   * @param it item
   * @param ii input info
   * @return same item
   * @throws QueryException query exception
   */
  private Item check(final Item it, final InputInfo ii) throws QueryException {
    if(!checkExtension(it)) XPCAST.thrw(ii, it.type, ext);
    return it;
  }

  /**
   * Checks the sequence extension.
   * @param it item
   * @return same item
   */
  private boolean checkExtension(final Item it) {
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
    return type.toString() + occ.toString();
  }

  /**
   * Checks if the specified SeqType is an instance of the current SeqType.
   * @param t SeqType to check
   * @return result of check
   */
  public boolean instance(final SeqType t) {
    return type.instance(t.type) && occ.instance(t.occ);
  }
}
