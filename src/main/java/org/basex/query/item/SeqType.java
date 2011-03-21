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
      return min >= o.min && max <= o.max;
    }

    @Override
    public String toString() {
      return str;
    }
  }

  /** Zero items. */
  public static final SeqType ITEM_Z = new SeqType(AtomType.ITEM, Occ.Z);
  /** Single item. */
  public static final SeqType ITEM = AtomType.ITEM.seq();
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = new SeqType(AtomType.ITEM, Occ.ZO);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = new SeqType(AtomType.ITEM, Occ.ZM);
  /** One or more items. */
  public static final SeqType ITEM_OM = new SeqType(AtomType.ITEM, Occ.OM);
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT = AtomType.AAT.seq();
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT_ZO = new SeqType(AtomType.AAT, Occ.ZO);
  /** Zero or more xs:anyAtomicType. */
  public static final SeqType AAT_ZM = new SeqType(AtomType.AAT, Occ.ZM);
  /** Single boolean. */
  public static final SeqType BLN = AtomType.BLN.seq();
  /** Zero or one booleans. */
  public static final SeqType BLN_ZO = new SeqType(AtomType.BLN, Occ.ZO);
  /** Single Base64Binary. */
  public static final SeqType B64 = AtomType.B64.seq();
  /** Double number. */
  public static final SeqType DBL = AtomType.DBL.seq();
  /** Double number. */
  public static final SeqType DBL_ZM = new SeqType(AtomType.DBL, Occ.ZM);
  /** Float number. */
  public static final SeqType FLT = AtomType.FLT.seq();
  /** Zero or one double. */
  public static final SeqType DBL_ZO = new SeqType(AtomType.DBL, Occ.ZO);
  /** Zero or one decimal number. */
  public static final SeqType DEC_ZO = new SeqType(AtomType.DEC, Occ.ZO);
  /** Single number; for simplicity, numbers are summarized by this type. */
  public static final SeqType ITR = AtomType.ITR.seq();
  /** Zero or one number. */
  public static final SeqType ITR_ZO = new SeqType(AtomType.ITR, Occ.ZO);
  /** Zero or more numbers. */
  public static final SeqType ITR_ZM = new SeqType(AtomType.ITR, Occ.ZM);
  /** One or more numbers. */
  public static final SeqType ITR_OM = new SeqType(AtomType.ITR, Occ.OM);
  /** Single node. */
  public static final SeqType NOD = NodeType.NOD.seq();
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(NodeType.NOD, Occ.ZO);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(NodeType.NOD, Occ.ZM);
  /** One or more nodes. */
  public static final SeqType NOD_OM = new SeqType(NodeType.NOD, Occ.OM);
  /** Single QName. */
  public static final SeqType QNM = AtomType.QNM.seq();
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(AtomType.QNM, Occ.ZO);
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(AtomType.URI, Occ.ZO);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = new SeqType(AtomType.URI, Occ.ZM);
  /** Single URI. */
  public static final SeqType URI = AtomType.URI.seq();
  /** Single string. */
  public static final SeqType STR = AtomType.STR.seq();
  /** Zero or one strings. */
  public static final SeqType STR_ZO = new SeqType(AtomType.STR, Occ.ZO);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = new SeqType(AtomType.STR, Occ.ZM);
  /** Zero or one NCName. */
  public static final SeqType NCN_ZO = new SeqType(AtomType.NCN, Occ.ZO);
  /** One xs:hexBinary. */
  public static final SeqType HEX = AtomType.HEX.seq();
  /** Single date. */
  public static final SeqType DAT = AtomType.DAT.seq();
  /** Zero or one date. */
  public static final SeqType DAT_ZO = new SeqType(AtomType.DAT, Occ.ZO);
  /** Zero or more dates. */
  public static final SeqType DAT_ZM = new SeqType(AtomType.DAT, Occ.ZM);
  /** One day-time-duration. */
  public static final SeqType DTD = AtomType.DTD.seq();
  /** Zero or one day-time-duration. */
  public static final SeqType DTD_ZO = new SeqType(AtomType.DTD, Occ.ZO);
  /** One date-time. */
  public static final SeqType DTM = AtomType.DTM.seq();
  /** Zero or one date-time. */
  public static final SeqType DTM_ZO = new SeqType(AtomType.DTM, Occ.ZO);
  /** One time. */
  public static final SeqType TIM = AtomType.TIM.seq();
  /** Zero or one time. */
  public static final SeqType TIM_ZO = new SeqType(AtomType.TIM, Occ.ZO);
  /** One duration. */
  public static final SeqType DUR = AtomType.DUR.seq();
  /** Zero or one duration. */
  public static final SeqType DUR_ZO = new SeqType(AtomType.DUR, Occ.ZO);
  /** Single function. */
  public static final SeqType FUN_O = new SeqType(FunType.ANY, Occ.O);
  /** Zero or more bytes. */
  public static final SeqType BYT_ZM = new SeqType(AtomType.BYT, Occ.ZM);
  /** One document node. */
  public static final SeqType DOC_O = NodeType.DOC.seq();
  /** Zero or one document node. */
  public static final SeqType DOC_ZO = new SeqType(NodeType.DOC, Occ.ZO);
  /** One element node. */
  public static final SeqType ELM = NodeType.ELM.seq();
  /** Zero or one element node. */
  public static final SeqType ELM_ZO = new SeqType(NodeType.ELM, Occ.ZO);
  /** Zero or more element nodes. */
  public static final SeqType ELM_ZM = new SeqType(NodeType.ELM, Occ.ZM);

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
   * Constructor. This one is only called by {@link Type#seq} to create
   * unique sequence type instances.
   * @param t type
   */
  SeqType(final Type t) {
    this(t, Occ.O);
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

    final Iter iter = val.iter();
    Item it = iter.next();
    if(it == null) {
      if(mayBeZero()) return Empty.SEQ;
      Err.cast(ii, type, val);
    }
    if(type == AtomType.EMP) Err.cast(ii, type, val);

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
    if(!it.unt() && !ins && !it.func() &&
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
    final String str = type.toString();
    return (str.contains(" ") && occ != Occ.O ? '(' + str + ')' : str) + occ;
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
