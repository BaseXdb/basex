package org.basex.query.value.type;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
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
    public boolean instanceOf(final Occ o) {
      return min >= o.min && max <= o.max;
    }

    /**
     * Computes the intersection between this occurrence indicator and the given one.
     * If none exists (e.g. between {@link #ZERO} and {@link #ONE}), {@code null} is
     * returned.
     * @param other other occurrence indicator
     * @return intersection or {@code null}
     */
    public Occ intersect(final Occ other) {
      final int mn = Math.max(min, other.min), mx = Math.min(max, other.max);
      return mx < mn ? null : mx == 0 ? ZERO : mn == mx ? ONE : mx == 1 ? ZERO_ONE :
        mn == 0 ? ZERO_MORE : ONE_MORE;
    }

    /**
     * Computes the union between this occurrence indicator and the given one.
     * @param other other occurrence indicator
     * @return union
     */
    public Occ union(final Occ other) {
      final int mn = Math.min(min, other.min), mx = Math.max(max, other.max);
      return mx == 0 ? ZERO : mn == mx ? ONE : mx == 1 ? ZERO_ONE :
        mn == 0 ? ZERO_MORE : ONE_MORE;
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
   * Returns a version of this sequence type that is adapted to the given {@link Occ}.
   * @param o occurrence indicator
   * @return sequence type
   */
  public SeqType withOcc(final Occ o) {
    return o == occ ? this : get(type, o, kind);
  }

  /**
   * Matches a value against this sequence type.
   * @param val value to be checked
   * @return result of check
   */
  public boolean instance(final Value val) {
    final long size = val.size();
    if(!occ.check(size)) return false;
    for(long i = 0; i < size; i++) {
      // maps don't have type information attached to them, you have to look...
      if(!instance(val.itemAt(i), true)) return false;
      if(i == 0 && val.homogeneous()) break;
    }
    return true;
  }

  /**
   * Checks if an item can be part of a sequence that is instance of this type.
   * @param it item to check
   * @param knd check kind
   * @return result of check
   */
  private boolean instance(final Item it, final boolean knd) {
    // maps don't have type information attached to them, you have to look...
    return type instanceof MapType
      ? it instanceof Map && ((Map) it).hasType((MapType) type)
      : it.type.instanceOf(type) && (!knd || kind == null || kind.eq(it));
  }

  /**
   * Tries to cast the given item to this sequence type.
   * @param it item to cast
   * @param ctx query context
   * @param ii input info
   * @param e expression for error message
   * @return promoted item
   * @throws QueryException query exception
   */
  public Value cast(final Item it, final QueryContext ctx, final InputInfo ii,
      final ExprInfo e) throws QueryException {

    if(it == null) {
      if(!occ.check(0)) INVEMPTYEX.thrw(ii, e.description(), this);
      return Empty.SEQ;
    }

    if(!occ.check(1)) INVCAST.thrw(ii, it.type, this);
    final Value v = it.type.eq(type) ? it : type.cast(it, ctx, ii);
    if(kind != null) {
      for(final Item i : v) if(!kind.eq(it)) Err.cast(ii, type, i);
    }
    return v;
  }

  /**
   * Casts a sequence to this type.
   * @param val value to cast
   * @param ctx query context
   * @param ii input info
   * @param e expression
   * @return resulting value
   * @throws QueryException query exception
   */
  public Value cast(final Value val, final QueryContext ctx, final InputInfo ii,
      final ExprInfo e) throws QueryException {
    if(val.size() < 2) return cast(val.isEmpty() ? null : val.itemAt(0), ctx, ii, e);

    if(!occ.check(val.size())) INVCAST.thrw(ii, val.type(), this);
    final ValueBuilder vb = new ValueBuilder((int) val.size());
    for(int i = 0; i < val.size(); i++) vb.add(cast(val.itemAt(i), ctx, ii, e));
    return vb.value();
  }

  /**
   * Treats the specified value as this sequence type.
   * @param val value to promote
   * @param ii input info
   * @return the value
   * @throws QueryException query exception
   */
  public Value treat(final Value val, final InputInfo ii) throws QueryException {
    if(val.type().instanceOf(this)) return val;

    final int size = (int) val.size();
    if(!occ.check(size)) Err.treat(ii, this, val);

    // empty sequence has all types
    if(size == 0) return Empty.SEQ;
    // check first item
    boolean ins = instance(val.itemAt(0), true);

    // check heterogeneous sequences
    if(!val.homogeneous())
      for(int i = 1; ins && i < size; i++) ins = instance(val.itemAt(i), true);
    if(!ins) Err.treat(ii, this, val);
    return val;
  }

  /**
   * Tries to promote an item to this type's element type.
   * @param ctx query context
   * @param ii input info
   * @param it item to promote
   * @return promoted item
   * @throws QueryException query exception
   */
  private Value funcConv(final QueryContext ctx, final InputInfo ii,
      final Item it) throws QueryException {
    if(type instanceof AtomType) {
      final Item atom = StandardFunc.atom(it, ii);
      if(atom != it && atom.type.instanceOf(type)) return it;
      if(atom.type == AtomType.ATM) {
        if(type.nsSensitive()) {
          if(ctx.sc.xquery3()) NSSENS.thrw(ii, it.type, type);
          Err.treat(ii, withOcc(Occ.ONE), it);
        }
        return type.cast(atom, ctx, ii);
      }

      final Type at = atom.type, tt = type;
      if(tt == AtomType.DBL
          && (at.instanceOf(AtomType.FLT) || at.instanceOf(AtomType.DEC)))
        return Dbl.get(atom.dbl(ii));
      if(tt == AtomType.FLT && at.instanceOf(AtomType.DEC))
        return Flt.get(atom.flt(ii));
      if(tt == AtomType.STR && at.instanceOf(AtomType.URI))
        return Str.get(atom.string(ii));
    } else if(it instanceof FItem && type instanceof FuncType) {
      return ((FItem) it).coerceTo((FuncType) type, ctx, ii);
    }

    throw Err.treat(ii, withOcc(Occ.ONE), it);
  }

  /**
   * Performs function conversion on the given value.
   * @param ctx query context
   * @param ii input info
   * @param val value to convert
   * @return converted value
   * @throws QueryException if the conversion ws not possible
   */
  public Value funcConvert(final QueryContext ctx, final InputInfo ii,
      final Value val) throws QueryException {
    final long n = val.size();
    if(!occ.check(n)) throw Err.treat(ii, this, val);
    if(n == 0) return Empty.SEQ;
    if(val.isItem())
      return instance((Item) val, true) ? val : funcConv(ctx, ii, (Item) val);

    ValueBuilder vb = null;
    final Item fst = val.itemAt(0);
    if(!instance(fst, true)) {
      vb = new ValueBuilder(new Item[(int) val.size()], 0);
      vb.add(funcConv(ctx, ii, fst));
    } else if(val.homogeneous()) {
      return val;
    }

    for(int i = 1; i < n; i++) {
      final Item it = val.itemAt(i);
      if(vb != null) {
        vb.add(instance(it, true) ? it : funcConv(ctx, ii, it));
      } else if(!instance(it, true)) {
        vb = new ValueBuilder(new Item[(int) val.size()], 0);
        for(int j = 0; j < i; j++) vb.add(val.itemAt(j));
        vb.add(funcConv(ctx, ii, it));
      }
    }
    return vb != null ? vb.value() : val;
  }

  /**
   * Checks if this type could be converted to the given one by function conversion.
   * @param t type to convert to
   * @return result of check
   */
  public boolean convertibleTo(final SeqType t) {
    if(intersect(t) != null) return true;
    if(occ.intersect(t.occ) == null) return false;
    final Type to = t.type;
    if(to instanceof AtomType) {
      if(type.isUntyped()) return !to.nsSensitive();
      return to == AtomType.DBL && (couldBe(AtomType.FLT) || couldBe(AtomType.DEC))
          || to == AtomType.FLT && couldBe(AtomType.DEC)
          || to == AtomType.STR && couldBe(AtomType.URI);
    }
    return t.type instanceof FuncType && type instanceof FuncType;
  }

  /**
   * Checks if this type's item type could be instance of the given one.
   * @param o other type
   * @return result of check
   */
  private boolean couldBe(final Type o) {
    return type.intersect(o) != null;
  }

  /**
   * Computes the union of two sequence types, i.e. the lowest common ancestor of both
   * types.
   * @param t second type
   * @return resulting type
   */
  public SeqType union(final SeqType t) {
    return get(type == t.type ? type : type.union(t.type), occ.union(t.occ));
  }

  /**
   * Computes the intersection of two sequence types, i.e. the most general type that is
   * sub-type of both types. If no such type exists, {@code null} is returned
   * @param t second type
   * @return resulting type or {@code null}
   */
  public SeqType intersect(final SeqType t) {
    final Occ o = occ.intersect(t.occ);
    if(o == null) return null;
    final Type tp = type.intersect(t.type);
    if(tp == null) return null;
    if(kind == null || t.kind == null || kind.sameAs(t.kind))
      return get(tp, o, kind != null ? kind : t.kind);
    final Test k = kind.intersect(t.kind);
    return k == null ? null : get(tp, o, k);
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
   * Checks the types for equality.
   * @param t type
   * @return result of check
   */
  public boolean eq(final SeqType t) {
    return type.eq(t.type) && occ == t.occ &&
      (kind == null ? t.kind == null : t.kind != null && kind.sameAs(t.kind));
  }

  /**
   * Checks if the specified SeqType is an instance of the current SeqType.
   * @param t SeqType to check
   * @return result of check
   */
  public boolean instanceOf(final SeqType t) {
    return type.instanceOf(t.type) && occ.instanceOf(t.occ)
      // [LW] complete kind check
      && t.kind == null || kind != null && kind.intersect(t.kind) != null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(occ == Occ.ZERO ? EMPTY_SEQUENCE + "()" : type);
    if(kind != null) sb.deleteCharAt(sb.length() - 1).append(kind).append(')');
    if(!(type instanceof ListType)) sb.append(occ);
    return sb.toString();
  }
}
