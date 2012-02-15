package org.basex.query.item;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.map.Map;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Stores a sequence type definition.
 *
 * @author BaseX Team 2005-12, BSD License
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

    /**
     * Checks if the given cardinality is supported by this type.
     * @param c cardinality
     * @return result of check
     */
    public boolean check(final long c) {
      return min <= c && c <= max;
    }

    @Override
    public String toString() {
      return str;
    }
  }

  /** Zero items. */
  public static final SeqType EMP = new SeqType(AtomType.EMP, Occ.Z);
  /** Single item. */
  public static final SeqType ITEM = AtomType.ITEM.seqType();
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = new SeqType(AtomType.ITEM, Occ.ZO);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = new SeqType(AtomType.ITEM, Occ.ZM);
  /** One or more items. */
  public static final SeqType ITEM_OM = new SeqType(AtomType.ITEM, Occ.OM);
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT = AtomType.AAT.seqType();
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT_ZO = new SeqType(AtomType.AAT, Occ.ZO);
  /** Zero or more xs:anyAtomicType. */
  public static final SeqType AAT_ZM = new SeqType(AtomType.AAT, Occ.ZM);
  /** Single xs:boolean. */
  public static final SeqType BLN = AtomType.BLN.seqType();
  /** Zero or one xs:boolean. */
  public static final SeqType BLN_ZO = new SeqType(AtomType.BLN, Occ.ZO);
  /** Double number. */
  public static final SeqType DBL = AtomType.DBL.seqType();
  /** Double number. */
  public static final SeqType DBL_ZM = new SeqType(AtomType.DBL, Occ.ZM);
  /** Zero or one double. */
  public static final SeqType DBL_ZO = new SeqType(AtomType.DBL, Occ.ZO);
  /** Zero or one decimal number. */
  public static final SeqType DEC_ZO = new SeqType(AtomType.DEC, Occ.ZO);
  /** Single number; for simplicity, numbers are summarized by this type. */
  public static final SeqType ITR = AtomType.ITR.seqType();
  /** Zero or one number. */
  public static final SeqType ITR_ZO = new SeqType(AtomType.ITR, Occ.ZO);
  /** Zero or more numbers. */
  public static final SeqType ITR_ZM = new SeqType(AtomType.ITR, Occ.ZM);
  /** One or more numbers. */
  public static final SeqType ITR_OM = new SeqType(AtomType.ITR, Occ.OM);
  /** Single node. */
  public static final SeqType NOD = NodeType.NOD.seqType();
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(NodeType.NOD, Occ.ZO);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(NodeType.NOD, Occ.ZM);
  /** One or more nodes. */
  public static final SeqType NOD_OM = new SeqType(NodeType.NOD, Occ.OM);
  /** Single QName. */
  public static final SeqType QNM = AtomType.QNM.seqType();
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(AtomType.QNM, Occ.ZO);
  /** Single URI. */
  public static final SeqType URI = AtomType.URI.seqType();
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(AtomType.URI, Occ.ZO);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = new SeqType(AtomType.URI, Occ.ZM);
  /** Single string. */
  public static final SeqType STR = AtomType.STR.seqType();
  /** Zero or one strings. */
  public static final SeqType STR_ZO = new SeqType(AtomType.STR, Occ.ZO);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = new SeqType(AtomType.STR, Occ.ZM);
  /** Zero or one NCName. */
  public static final SeqType NCN_ZO = new SeqType(AtomType.NCN, Occ.ZO);
  /** Single date. */
  public static final SeqType DAT = AtomType.DAT.seqType();
  /** Zero or one date. */
  public static final SeqType DAT_ZO = new SeqType(AtomType.DAT, Occ.ZO);
  /** One day-time-duration. */
  public static final SeqType DTD = AtomType.DTD.seqType();
  /** Zero or one day-time-duration. */
  public static final SeqType DTD_ZO = new SeqType(AtomType.DTD, Occ.ZO);
  /** One date-time. */
  public static final SeqType DTM = AtomType.DTM.seqType();
  /** Zero or one date-time. */
  public static final SeqType DTM_ZO = new SeqType(AtomType.DTM, Occ.ZO);
  /** One time. */
  public static final SeqType TIM = AtomType.TIM.seqType();
  /** Zero or one time. */
  public static final SeqType TIM_ZO = new SeqType(AtomType.TIM, Occ.ZO);
  /** Zero or one duration. */
  public static final SeqType DUR_ZO = new SeqType(AtomType.DUR, Occ.ZO);
  /** Single function. */
  public static final SeqType FUN_O = FuncType.ANY_FUN.seqType();
  /** Zero or more bytes. */
  public static final SeqType BYT_ZM = new SeqType(AtomType.BYT, Occ.ZM);
  /** One document node. */
  public static final SeqType DOC_O = NodeType.DOC.seqType();
  /** Zero or one document node. */
  public static final SeqType DOC_ZO = new SeqType(NodeType.DOC, Occ.ZO);
  /** Zero or more document node. */
  public static final SeqType DOC_ZM = new SeqType(NodeType.DOC, Occ.ZM);
  /** One element node. */
  public static final SeqType ELM = NodeType.ELM.seqType();
  /** Zero or more element nodes. */
  public static final SeqType ELM_ZM = new SeqType(NodeType.ELM, Occ.ZM);

  /** The general map type. */
  public static final MapType ANY_MAP = new MapType(AtomType.AAT, ITEM_ZM);
  /** Single function. */
  public static final SeqType MAP_ZM = new SeqType(ANY_MAP, Occ.ZM);
  /** Single function. */
  public static final SeqType MAP_O = new SeqType(
      MapType.get(AtomType.AAT, ITEM_ZM));
  /** One xs:hexBinary. */
  public static final SeqType HEX = AtomType.HEX.seqType();
  /** Single xs:base64Binary. */
  public static final SeqType B64 = AtomType.B64.seqType();

  /** Sequence type. */
  public final Type type;
  /** Number of occurrences. */
  public final Occ occ;
  /** Extended type info. */
  private final QNm ext;

  /**
   * Private constructor.
   * @param t type
   * @param o occurrences
   */
  private SeqType(final Type t, final Occ o) {
    this(t, o, null);
  }

  /**
   * Constructor. This one is only called by {@link Type#seqType} to create
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
    return o == Occ.O && e == null ? t.seqType() : new SeqType(t, o, e);
  }

  /**
   * Matches a value against this sequence type.
   * @param val value to be checked
   * @return result of check
   */
  public boolean instance(final Value val) {
    final long size = val.size();
    if(!occ.check(size)) return false;

    // the empty sequence has every type
    if(size == 0) return true;

    final MapType mt = type.isMap() ? (MapType) type : null;
    for(long i = 0; i < size; i++) {
      final Item it = val.itemAt(i);

      // maps don't have type information attached to them, you have to look...
      final Type ip = it.type;
      if(mt == null) {
        if(!(ip.instanceOf(type) && checkExt(it))) return false;
      } else {
        if(!(ip.isMap() && ((Map) it).hasType(mt))) return false;
      }
      if(i == 0 && val.homogenous()) break;
    }
    return true;
  }

  /**
   * Tries to promote the given item to this sequence type.
   * @param it item to promote
   * @param e producing expression
   * @param cast explicit cast flag
   * @param ctx query context
   * @param ii input info
   * @return promoted item
   * @throws QueryException query exception
   */
  public Item cast(final Item it, final Expr e, final boolean cast,
      final QueryContext ctx, final InputInfo ii) throws QueryException {

    if(it == null) {
      if(occ == Occ.O) XPEMPTY.thrw(ii, e.description());
      return null;
    }
    final boolean correct = cast ? it.type == type : instance(it, ii);
    return check(correct ? it : type.cast(it, ctx, ii), ii);
  }

  /**
   * Promotes the specified value.
   * @param val value to be cast
   * @param ctx query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  public Value promote(final Value val, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    final long size = val.size();
    if(!occ.check(size)) Err.promote(ii, type, val);

    // empty sequence has all types
    if(size == 0) return val;

    final Item f = val.itemAt(0);

    // take shortcut if it's a single item
    if(size == 1) return check(instance(f, ii) ? f : type.cast(f, ctx, ii), ii);

    // only cache if absolutely necessary
    if(val.homogenous() && instance(f, ii) && checkExt(f)) return val;

    // no way around it...
    final ItemCache ic = new ItemCache((int) size);
    for(long i = 0; i < size; i++) {
      final Item n = val.itemAt(i);
      ic.add(check(instance(n, ii) ? n : type.cast(n, ctx, ii), ii));
    }

    return ic.value();
  }

  /**
   * Returns whether item has already correct type.
   * @param it input item
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean instance(final Item it, final InputInfo ii)
      throws QueryException {

    final Type ip = it.type;
    final boolean ins = ip.instanceOf(type);
    if(!ins && !ip.isUntyped() && !ip.isFunction() &&
        // implicit type promotions:
        // xs:float -> xs:double
        (ip != AtomType.FLT || type != AtomType.DBL) &&
        // xs:anyUri -> xs:string
        (ip != AtomType.URI || type != AtomType.STR) &&
        // xs:decimal -> xs:float/xs:double
        (type != AtomType.FLT && type != AtomType.DBL ||
            !ip.instanceOf(AtomType.DEC)))
      Err.promote(ii, type, it);
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
   * Tests if the type may be numeric.
   * @return result of check
   */
  public boolean mayBeNumber() {
    return type.isNumber() || type == AtomType.ITEM;
  }

  /**
   * Checks the sequence extension.
   * @param it item
   * @param ii input info
   * @return same item
   * @throws QueryException query exception
   */
  private Item check(final Item it, final InputInfo ii) throws QueryException {
    if(!checkExt(it)) XPCAST.thrw(ii,
        it.type.toString().replaceAll("\\(|\\)", ""), ext);
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

  /**
   * Checks if the specified SeqType is an instance of the current SeqType.
   * @param t SeqType to check
   * @return result of check
   */
  public boolean instance(final SeqType t) {
    return type.instanceOf(t.type) && occ.instance(t.occ);
  }

  @Override
  public String toString() {
    final String str = type.toString();
    return (str.contains(" ") && occ != Occ.O ? '(' + str + ')' : str) + occ;
  }
}
