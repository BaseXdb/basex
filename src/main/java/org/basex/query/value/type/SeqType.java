package org.basex.query.value.type;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Stores a sequence type definition.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Number of occurrences (cardinality). */
  public enum Occ {
    /** Zero.         */ ZERO(0, 0, ""),
    /** Zero or one.  */ ZERO_ONE(0, 1, "?"),
    /** Exactly one.  */ ONE(1, 1, ""),
    /** One or more.  */ ONE_MORE(1, Integer.MAX_VALUE, "+"),
    /** Zero or more. */ ZERO_MORE(0, Integer.MAX_VALUE, "*");

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
  public static final SeqType EMP = new SeqType(AtomType.ITEM, Occ.ZERO);
  /** Single item. */
  public static final SeqType ITEM = AtomType.ITEM.seqType();
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = new SeqType(AtomType.ITEM, Occ.ZERO_ONE);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = new SeqType(AtomType.ITEM, Occ.ZERO_MORE);
  /** One or more items. */
  public static final SeqType ITEM_OM = new SeqType(AtomType.ITEM, Occ.ONE_MORE);
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT = AtomType.AAT.seqType();
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT_ZO = new SeqType(AtomType.AAT, Occ.ZERO_ONE);
  /** Zero or more xs:anyAtomicType. */
  public static final SeqType AAT_ZM = new SeqType(AtomType.AAT, Occ.ZERO_MORE);
  /** Single xs:boolean. */
  public static final SeqType BLN = AtomType.BLN.seqType();
  /** Zero or one xs:boolean. */
  public static final SeqType BLN_ZO = new SeqType(AtomType.BLN, Occ.ZERO_ONE);
  /** Double number. */
  public static final SeqType DBL = AtomType.DBL.seqType();
  /** Double number. */
  public static final SeqType DBL_ZM = new SeqType(AtomType.DBL, Occ.ZERO_MORE);
  /** Zero or one double. */
  public static final SeqType DBL_ZO = new SeqType(AtomType.DBL, Occ.ZERO_ONE);
  /** Zero or one decimal number. */
  public static final SeqType DEC_ZO = new SeqType(AtomType.DEC, Occ.ZERO_ONE);
  /** Single integer; for simplicity, numbers are summarized by this type. */
  public static final SeqType ITR = AtomType.ITR.seqType();
  /** Zero or one integer. */
  public static final SeqType ITR_ZO = new SeqType(AtomType.ITR, Occ.ZERO_ONE);
  /** Zero or more integers. */
  public static final SeqType ITR_ZM = new SeqType(AtomType.ITR, Occ.ZERO_MORE);
  /** One or more integers. */
  public static final SeqType ITR_OM = new SeqType(AtomType.ITR, Occ.ONE_MORE);
  /** One or more bytes. */
  public static final SeqType BYT_OM = new SeqType(AtomType.BYT, Occ.ONE_MORE);
  /** Single node. */
  public static final SeqType NOD = NodeType.NOD.seqType();
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(NodeType.NOD, Occ.ZERO_ONE);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(NodeType.NOD, Occ.ZERO_MORE);
  /** Single QName. */
  public static final SeqType QNM = AtomType.QNM.seqType();
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(AtomType.QNM, Occ.ZERO_ONE);
  /** Single URI. */
  public static final SeqType URI = AtomType.URI.seqType();
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(AtomType.URI, Occ.ZERO_ONE);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = new SeqType(AtomType.URI, Occ.ZERO_MORE);
  /** Single string. */
  public static final SeqType STR = AtomType.STR.seqType();
  /** Zero or one strings. */
  public static final SeqType STR_ZO = new SeqType(AtomType.STR, Occ.ZERO_ONE);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = new SeqType(AtomType.STR, Occ.ZERO_MORE);
  /** One or more strings. */
  public static final SeqType STR_OM = new SeqType(AtomType.STR, Occ.ONE_MORE);
  /** Zero or one NCName. */
  public static final SeqType NCN_ZO = new SeqType(AtomType.NCN, Occ.ZERO_ONE);
  /** Single date. */
  public static final SeqType DAT = AtomType.DAT.seqType();
  /** Zero or one date. */
  public static final SeqType DAT_ZO = new SeqType(AtomType.DAT, Occ.ZERO_ONE);
  /** One day-time-duration. */
  public static final SeqType DTD = AtomType.DTD.seqType();
  /** Zero or one day-time-duration. */
  public static final SeqType DTD_ZO = new SeqType(AtomType.DTD, Occ.ZERO_ONE);
  /** One date-time. */
  public static final SeqType DTM = AtomType.DTM.seqType();
  /** Zero or one date-time. */
  public static final SeqType DTM_ZO = new SeqType(AtomType.DTM, Occ.ZERO_ONE);
  /** One time. */
  public static final SeqType TIM = AtomType.TIM.seqType();
  /** Zero or one time. */
  public static final SeqType TIM_ZO = new SeqType(AtomType.TIM, Occ.ZERO_ONE);
  /** Zero or one duration. */
  public static final SeqType DUR_ZO = new SeqType(AtomType.DUR, Occ.ZERO_ONE);
  /** Zero of single function. */
  public static final SeqType FUN_OZ = new SeqType(FuncType.ANY_FUN, Occ.ZERO_ONE);
  /** Single function. */
  public static final SeqType FUN_O = FuncType.ANY_FUN.seqType();
  /** Zero or more bytes. */
  public static final SeqType BYT_ZM = new SeqType(AtomType.BYT, Occ.ZERO_MORE);
  /** One attribute node. */
  public static final SeqType ATT = NodeType.ATT.seqType();
  /** One comment node. */
  public static final SeqType COM = NodeType.COM.seqType();
  /** One document node. */
  public static final SeqType DOC_O = NodeType.DOC.seqType();
  /** Zero or one document node. */
  public static final SeqType DOC_ZO = new SeqType(NodeType.DOC, Occ.ZERO_ONE);
  /** One or more document node. */
  public static final SeqType DOC_OM = new SeqType(NodeType.DOC, Occ.ONE_MORE);
  /** Zero or more document node. */
  public static final SeqType DOC_ZM = new SeqType(NodeType.DOC, Occ.ZERO_MORE);
  /** One element node. */
  public static final SeqType ELM = NodeType.ELM.seqType();
  /** Zero or more element nodes. */
  public static final SeqType ELM_ZM = new SeqType(NodeType.ELM, Occ.ZERO_MORE);
  /** Namespace node. */
  public static final SeqType NSP = NodeType.NSP.seqType();
  /** Namespace node. */
  public static final SeqType PI = NodeType.PI.seqType();
  /** Namespace node. */
  public static final SeqType TXT_ZO = new SeqType(NodeType.TXT, Occ.ZERO_ONE);

  /** The general map type. */
  public static final MapType ANY_MAP = new MapType(AtomType.AAT, ITEM_ZM);
  /** Single function. */
  public static final SeqType MAP_ZM = new SeqType(ANY_MAP, Occ.ZERO_MORE);
  /** Single function. */
  public static final SeqType MAP_O = new SeqType(ANY_MAP);
  /** One xs:hexBinary. */
  public static final SeqType HEX = AtomType.HEX.seqType();
  /** Single xs:base64Binary. */
  public static final SeqType B64 = AtomType.B64.seqType();
  /** Zero or more xs:base64Binary. */
  public static final SeqType B64_ZM = new SeqType(AtomType.B64, Occ.ZERO_MORE);

  /** Single binary. */
  public static final SeqType BIN = AtomType.BIN.seqType();
  /** Zero or more binaries. */
  public static final SeqType BIN_ZM = new SeqType(AtomType.BIN, Occ.ZERO_MORE);

  /** Sequence type. */
  public final Type type;
  /** Number of occurrences. */
  public final Occ occ;
  /** Optional kind test. */
  private final Test kind;

  /**
   * Private constructor.
   * @param t type
   * @param o occurrences
   */
  private SeqType(final Type t, final Occ o) {
    this(t, o, null);
  }

  /**
   * Constructor. This one is called by {@link Type#seqType} to create
   * unique sequence type instances.
   * @param t type
   */
  SeqType(final Type t) {
    this(t, Occ.ONE);
  }

  /**
   * Private constructor.
   * @param t type
   * @param o occurrences
   * @param k kind test
   */
  private SeqType(final Type t, final Occ o, final Test k) {
    type = t;
    occ = o;
    kind = k;
  }

  /**
   * Returns a sequence type.
   * @param t type
   * @param o occurrences
   * @return sequence type
   */
  public static SeqType get(final Type t, final Occ o) {
    return o == Occ.ONE ? t.seqType() : new SeqType(t, o);
  }

  /**
   * Returns a sequence type.
   * @param t type
   * @param o number of occurrences
   * @return sequence type
   */
  public static SeqType get(final Type t, final long o) {
    return get(t, o == 0 ? Occ.ZERO : o == 1 ? Occ.ONE : o > 1 ?
        Occ.ONE_MORE : Occ.ZERO_MORE);
  }

  /**
   * Returns a sequence type.
   * @param t type
   * @param o occurrences
   * @param k kind test
   * @return sequence type
   */
  public static SeqType get(final Type t, final Occ o, final Test k) {
    return new SeqType(t, o, k);
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

    final MapType mt = type instanceof MapType ? (MapType) type : null;
    for(long i = 0; i < size; i++) {
      final Item it = val.itemAt(i);

      // maps don't have type information attached to them, you have to look...
      if(mt == null) {
        if(!(it.type.instanceOf(type) && checkKind(it))) return false;
      } else {
        if(!(it instanceof Map && ((Map) it).hasType(mt))) return false;
      }
      if(i == 0 && val.homogeneous()) break;
    }
    return true;
  }

  /**
   * Tries to cast the given item to this sequence type.
   * @param it item to cast
   * @param cast explicit cast flag
   * @param ctx query context
   * @param ii input info
   * @param e producing expression, used for error output
   * @return promoted item
   * @throws QueryException query exception
   */
  public Item cast(final Item it, final boolean cast, final QueryContext ctx,
      final InputInfo ii, final Expr e) throws QueryException {

    if(it == null) {
      if(occ == Occ.ONE) XPEMPTY.thrw(ii, e.description());
      return null;
    }
    final boolean correct = cast ? it.type == type : instance(it, ii);
    final Item i = correct ? it : type.cast(it, ctx, ii);
    if(!checkKind(i)) Err.cast(ii, type, i);
    return i;
  }

  /**
   * Treats the specified value as this sequence type.
   * @param val value to promote
   * @param ii input info
   * @throws QueryException query exception
   */
  public void treat(final Value val, final InputInfo ii) throws QueryException {
    final int size = (int) val.size();
    if(!occ.check(size)) Err.treat(ii, this, val);

    // empty sequence has all types
    if(size == 0) return;
    // check first item
    Item n = val.itemAt(0);
    boolean ins = n.type.instanceOf(type) && checkKind(n);

    // check heterogeneous sequences
    if(!val.homogeneous()) {
      for(int i = 1; ins && i < size; i++) {
        n = val.itemAt(i);
        ins = n.type.instanceOf(type) && checkKind(n);
      }
    }
    if(!ins) Err.treat(ii, this, val);
  }

  /**
   * Tries to promote the specified value to this sequence type.
   * @param val value to promote
   * @param ctx query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  public Value promote(final Value val, final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final int size = (int) val.size();
    if(!occ.check(size)) Err.treat(ii, this, val);

    // empty sequence has all types
    if(size == 0) return val;
    // check first item
    Item n = val.itemAt(0);
    final boolean in = instance(n, ii);
    if(!in) {
      if((n.type == NodeType.COM || n.type == NodeType.PI) && !instance(Str.ZERO, ii)) {
        Err.treat(ii, this, n);
      }
      n = type.cast(n, ctx, ii);
    }
    boolean ins = checkKind(n);

    // return original sequence if no casting is necessary
    if(in && ins && val.homogeneous()) return val;

    // check heterogeneous sequences; no way around it...
    final Item[] items = new Item[size];
    items[0] = n;
    for(int i = 1; ins && i < size; i++) {
      n = val.itemAt(i);
      if(!instance(n, ii)) n = type.cast(n, ctx, ii);
      ins = checkKind(n);
      items[i] = n;
    }
    if(!ins) Err.treat(ii, this, val);
    return Seq.get(items, size);
  }

  /**
   * Returns whether item has already correct type.
   * @param it input item
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean instance(final Item it, final InputInfo ii) throws QueryException {
    final Type ip = it.type;
    final boolean ins = ip.instanceOf(type);
    if(!ins && !ip.isUntyped() && !(it instanceof FItem) &&
        // implicit type promotions:
        // xs:float -> xs:double
        (ip != AtomType.FLT || type != AtomType.DBL) &&
        // xs:anyUri -> xs:string
        (ip != AtomType.URI || type != AtomType.STR) &&
        // xs:decimal -> xs:float/xs:double
        (type != AtomType.FLT && type != AtomType.DBL || !ip.instanceOf(AtomType.DEC)))
      Err.treat(ii, this, it);
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
        Occ.ZERO_ONE : Occ.ZERO_MORE;
    return new SeqType(tp, oc);
  }

  /**
   * Returns the number of occurrences, or {@code -1} if the number is unknown.
   * @return result of check
   */
  public long occ() {
    return occ == Occ.ZERO ? 0 : occ == Occ.ONE ? 1 : -1;
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
    return occ == Occ.ONE;
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
   * Checks the additional kind test.
   * @param it item
   * @return same item
   */
  private boolean checkKind(final Item it) {
    return kind == null || kind.eq(it);
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
    final StringBuilder sb = new StringBuilder();
    sb.append(occ == Occ.ZERO ? EMPTY_SEQUENCE + "()" : type);
    if(kind != null) sb.deleteCharAt(sb.length() - 1).append(kind).append(')');
    return sb.append(occ).toString();
  }
}
