package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Stores a sequence type definition.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Occurrence indicator (cardinality). */
  public enum Occ {
    /** Zero.         */ ZERO(0, 0, ""),
    /** Zero or one.  */ ZERO_ONE(0, 1, "?"),
    /** Exactly one.  */ ONE(1, 1, ""),
    /** One or more.  */ ONE_MORE(1, Integer.MAX_VALUE, "+"),
    /** Zero or more. */ ZERO_MORE(0, Integer.MAX_VALUE, "*");

    /** String representation. */
    private final String str;
    /** Minimal result size. */
    public final int min;
    /** Maximal result size. */
    public final int max;

    /**
     * Constructor.
     * @param min minimal result size
     * @param max maximal result size
     * @param str string representation
     */
    Occ(final int min, final int max, final String str) {
      this.min = min;
      this.max = max;
      this.str = str;
    }

    /**
     * Checks if the specified occurrence indicator is an instance of the current occurrence
     * indicator.
     * @param occ occurrence indicator to check
     * @return result of check
     */
    public boolean instanceOf(final Occ occ) {
      return min >= occ.min && max <= occ.max;
    }

    /**
     * Computes the intersection between this occurrence indicator and the given one.
     * If none exists (e.g. between {@link #ZERO} and {@link #ONE}), {@code null} is returned.
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
      return mx == 0 ? ZERO : mn == mx ? ONE : mx == 1 ? ZERO_ONE : mn == 0 ? ZERO_MORE : ONE_MORE;
    }

    /**
     * Adds two occurrence indicators.
     * @param other other occurrence indicator
     * @return union
     */
    public Occ add(final Occ other) {
      final long mn = (long) min + other.min, mx = (long) max + other.max;
      return mx == 0 ? ZERO : mx == 1 ? mn == 0 ? ZERO_ONE : ONE : mn == 0 ? ZERO_MORE : ONE_MORE;
    }

    /**
     * Checks if the given cardinality is supported by this type.
     * @param card cardinality
     * @return result of check
     */
    public boolean check(final long card) {
      return min <= card && card <= max;
    }

    @Override
    public String toString() {
      return str;
    }
  }

  /** Zero items (single instance). */
  public static final SeqType EMP = new SeqType(AtomType.ITEM, Occ.ZERO);
  /** Single item. */
  public static final SeqType ITEM_O = AtomType.ITEM.seqType();
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = new SeqType(AtomType.ITEM, Occ.ZERO_ONE);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = new SeqType(AtomType.ITEM, Occ.ZERO_MORE);
  /** One or more items. */
  public static final SeqType ITEM_OM = new SeqType(AtomType.ITEM, Occ.ONE_MORE);
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT_O = AtomType.AAT.seqType();
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT_ZO = new SeqType(AtomType.AAT, Occ.ZERO_ONE);
  /** Zero or more xs:anyAtomicType. */
  public static final SeqType AAT_ZM = new SeqType(AtomType.AAT, Occ.ZERO_MORE);
  /** Zero or one xs:numeric. */
  public static final SeqType NUM_O = AtomType.NUM.seqType();
  /** Zero or one xs:numeric. */
  public static final SeqType NUM_ZO = new SeqType(AtomType.NUM, Occ.ZERO_ONE);
  /** Single xs:boolean. */
  public static final SeqType BLN_O = AtomType.BLN.seqType();
  /** Zero or one xs:boolean. */
  public static final SeqType BLN_ZO = new SeqType(AtomType.BLN, Occ.ZERO_ONE);
  /** Double number. */
  public static final SeqType DBL_O = AtomType.DBL.seqType();
  /** Double number. */
  public static final SeqType DBL_ZM = new SeqType(AtomType.DBL, Occ.ZERO_MORE);
  /** Zero or one double. */
  public static final SeqType DBL_ZO = new SeqType(AtomType.DBL, Occ.ZERO_ONE);
  /** Float number. */
  public static final SeqType FLT_O = AtomType.FLT.seqType();
  /** Zero or one decimal number. */
  public static final SeqType DEC_ZO = new SeqType(AtomType.DEC, Occ.ZERO_ONE);
  /** Single integer. */
  public static final SeqType ITR_O = AtomType.ITR.seqType();
  /** Zero or one integer. */
  public static final SeqType ITR_ZO = new SeqType(AtomType.ITR, Occ.ZERO_ONE);
  /** Zero or more integers. */
  public static final SeqType ITR_ZM = new SeqType(AtomType.ITR, Occ.ZERO_MORE);
  /** One or more integers. */
  public static final SeqType ITR_OM = new SeqType(AtomType.ITR, Occ.ONE_MORE);
  /** Single QName. */
  public static final SeqType QNM_O = AtomType.QNM.seqType();
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = new SeqType(AtomType.QNM, Occ.ZERO_ONE);
  /** Single URI. */
  public static final SeqType URI_O = AtomType.URI.seqType();
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = new SeqType(AtomType.URI, Occ.ZERO_ONE);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = new SeqType(AtomType.URI, Occ.ZERO_MORE);
  /** Single language. */
  public static final SeqType LAN_O = AtomType.LAN.seqType();
  /** Single string. */
  public static final SeqType STR_O = AtomType.STR.seqType();
  /** Zero or one strings. */
  public static final SeqType STR_ZO = new SeqType(AtomType.STR, Occ.ZERO_ONE);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = new SeqType(AtomType.STR, Occ.ZERO_MORE);
  /** Zero or one NCName. */
  public static final SeqType NCN_ZO = new SeqType(AtomType.NCN, Occ.ZERO_ONE);
  /** Single date. */
  public static final SeqType DAT_O = AtomType.DAT.seqType();
  /** Zero or one date. */
  public static final SeqType DAT_ZO = new SeqType(AtomType.DAT, Occ.ZERO_ONE);
  /** One day-time-duration. */
  public static final SeqType DTD_O = AtomType.DTD.seqType();
  /** Zero or one day-time-duration. */
  public static final SeqType DTD_ZO = new SeqType(AtomType.DTD, Occ.ZERO_ONE);
  /** One date-time. */
  public static final SeqType DTM_O = AtomType.DTM.seqType();
  /** Zero or one date-time. */
  public static final SeqType DTM_ZO = new SeqType(AtomType.DTM, Occ.ZERO_ONE);
  /** One time. */
  public static final SeqType TIM_O = AtomType.TIM.seqType();
  /** Zero or one time. */
  public static final SeqType TIM_ZO = new SeqType(AtomType.TIM, Occ.ZERO_ONE);
  /** Zero or one duration. */
  public static final SeqType DUR_ZO = new SeqType(AtomType.DUR, Occ.ZERO_ONE);
  /** Zero or more bytes. */
  public static final SeqType BYT_ZM = new SeqType(AtomType.BYT, Occ.ZERO_MORE);
  /** One xs:hexBinary. */
  public static final SeqType HEX_O = AtomType.HEX.seqType();
  /** Single xs:base64Binary. */
  public static final SeqType B64_O = AtomType.B64.seqType();
  /** Zero or one xs:base64Binary. */
  public static final SeqType B64_ZO = new SeqType(AtomType.B64, Occ.ZERO_ONE);
  /** Zero or more xs:base64Binary. */
  public static final SeqType B64_ZM = new SeqType(AtomType.B64, Occ.ZERO_MORE);
  /** Single binary. */
  public static final SeqType BIN_O = AtomType.BIN.seqType();

  /** Single node. */
  public static final SeqType NOD_O = NodeType.NOD.seqType();
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = new SeqType(NodeType.NOD, Occ.ZERO_ONE);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = new SeqType(NodeType.NOD, Occ.ZERO_MORE);
  /** One attribute node. */
  public static final SeqType ATT_O = NodeType.ATT.seqType();
  /** Zero or more attributes. */
  public static final SeqType ATT_ZM = new SeqType(NodeType.ATT, Occ.ZERO_MORE);
  /** One comment node. */
  public static final SeqType COM_O = NodeType.COM.seqType();
  /** One document node. */
  public static final SeqType DOC_O = NodeType.DOC.seqType();
  /** Zero or one document node. */
  public static final SeqType DOC_ZO = new SeqType(NodeType.DOC, Occ.ZERO_ONE);
  /** Zero or more document node. */
  public static final SeqType DOC_ZM = new SeqType(NodeType.DOC, Occ.ZERO_MORE);
  /** One element node. */
  public static final SeqType ELM_O = NodeType.ELM.seqType();
  /** Zero or more element nodes. */
  public static final SeqType ELM_ZM = new SeqType(NodeType.ELM, Occ.ZERO_MORE);
  /** Namespace node. */
  public static final SeqType NSP_O = NodeType.NSP.seqType();
  /** Processing instruction. */
  public static final SeqType PI_O = NodeType.PI.seqType();
  /** Text node. */
  public static final SeqType TXT_O = NodeType.TXT.seqType();
  /** Zero or one text node. */
  public static final SeqType TXT_ZO = new SeqType(NodeType.TXT, Occ.ZERO_ONE);
  /** Zero or more text nodes. */
  public static final SeqType TXT_ZM = new SeqType(NodeType.TXT, Occ.ZERO_MORE);

  /** Any function type. */
  public static final FuncType ANY_FUN = new FuncType(null, (SeqType[]) null);
  /** The general map type. */
  public static final MapType ANY_MAP = new MapType(AtomType.AAT, ITEM_ZM);
  /** The general array type. */
  public static final ArrayType ANY_ARRAY = new ArrayType(ITEM_ZM);

  /** Zero of single function. */
  public static final SeqType FUN_OZ = new SeqType(ANY_FUN, Occ.ZERO_ONE);
  /** Single function. */
  public static final SeqType FUN_O = ANY_FUN.seqType();
  /** Zero of more functions. */
  public static final SeqType FUN_ZM = new SeqType(ANY_FUN, Occ.ZERO_MORE);
  /** Zero or more maps. */
  public static final SeqType MAP_ZM = new SeqType(ANY_MAP, Occ.ZERO_MORE);
  /** Zero or one map. */
  public static final SeqType MAP_ZO = new SeqType(ANY_MAP, Occ.ZERO_ONE);
  /** Single map. */
  public static final SeqType MAP_O = ANY_MAP.seqType();
  /** Zero or more arrays. */
  public static final SeqType ARRAY_ZM = new SeqType(ANY_ARRAY, Occ.ZERO_MORE);
  /** Single array. */
  public static final SeqType ARRAY_O = ANY_ARRAY.seqType();

  /** Item type. */
  public final Type type;
  /** Occurrence indicator. */
  public final Occ occ;
  /** Kind test (can be {@code null}). */
  private final Test kind;

  /**
   * Constructor. This one is called by {@link Type#seqType()} to create
   * unique sequence type instances.
   * @param type type
   */
  SeqType(final Type type) {
    this(type, Occ.ONE);
  }

  /**
   * Private constructor.
   * @param type type
   * @param occ occurrence
   */
  private SeqType(final Type type, final Occ occ) {
    this(type, occ, null);
  }

  /**
   * Private constructor.
   * @param type type
   * @param occ occurrence indicator
   * @param kind kind test (can be {@code null})
   */
  private SeqType(final Type type, final Occ occ, final Test kind) {
    this.type = type;
    this.occ = occ;
    this.kind = kind;
  }

  /**
   * Returns a sequence type.
   * @param type type
   * @param occ occurrence indicator
   * @return sequence type
   */
  public static SeqType get(final Type type, final Occ occ) {
    return occ == Occ.ZERO ? EMP : occ == Occ.ONE ? type.seqType() : new SeqType(type, occ);
  }

  /**
   * Returns a sequence type.
   * @param type type
   * @param occ occurrence indicator
   * @param kind kind test (can be {@code null})
   * @return sequence type
   */
  public static SeqType get(final Type type, final Occ occ, final Test kind) {
    return occ == Occ.ZERO || kind == null ? get(type, occ) : new SeqType(type, occ, kind);
  }

  /**
   * Returns a sequence type with the specified type and occurrence indicator.
   * Returns this sequence type if it is identical.
   * @param tp type
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType with(final Type tp, final Occ oc) {
    return type.eq(tp) && occ == oc && kind == null ? this : get(tp, oc);
  }

  /**
   * Returns a version of this sequence type that is adapted to the given {@link Occ}.
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType with(final Occ oc) {
    return oc == occ ? this : get(type, oc, kind);
  }

  /**
   * Returns a version of this sequence type that is adapted to the given type.
   * @param tp type
   * @return sequence type
   */
  public SeqType with(final Type tp) {
    return type.eq(tp) ? this : get(tp, occ, kind);
  }

  /**
   * Checks if the specified value is an instance of this type.
   * @param value value to check
   * @return result of check
   */
  public boolean instance(final Value value) {
    final long size = value.size();
    if(!occ.check(size)) return false;
    for(long i = 0; i < size; i++) {
      if(!instance(value.itemAt(i))) return false;
      if(i == 0 && value.homogeneous()) break;
    }
    return true;
  }

  /**
   * Checks if the specified item can be part of a sequence that is instance of this type.
   * @param item item to check
   * @return result of check
   */
  public boolean instance(final Item item) {
    return item.instanceOf(type) && (kind == null || kind.eq(item));
  }

  /**
   * Casts the given item to this sequence type.
   * @param item item to cast
   * @param qc query context
   * @param sc static context
   * @param info input info
   * @param error raise error, or return {@code null}
   * @return cast value
   * @throws QueryException query exception
   */
  public Value cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo info, final boolean error) throws QueryException {

    if(item.type.eq(type)) return item;
    try {
      if(!error && info != null) info.internal(true);
      final Value v = type.cast(item, qc, sc, info);
      if(kind != null) {
        for(final Item i : v) if(!kind.eq(item)) throw castError(i, type, info);
      }
      return v;
    } catch(final QueryException ex) {
      if(error) throw ex;
      return null;
    } finally {
      if(!error && info != null) info.internal(false);
    }
  }

  /**
   * Casts a sequence to this type.
   * @param value value to cast
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return cast value
   * @throws QueryException query exception
   */
  public Value cast(final Value value, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    final long vs = value.size();
    if(!occ.check(vs)) throw INVCAST_X_X_X.get(ii, value.seqType(), this, value);

    if(value.isEmpty()) return Empty.SEQ;
    if(value instanceof Item) return cast((Item) value, qc, sc, ii, true);

    final ValueBuilder vb = new ValueBuilder();
    for(final Item it : value) vb.add(cast(it, qc, sc, ii, true));
    return vb.value();
  }

  /**
   * Checks the specified value for this sequence type.
   * @param value value to be checked
   * @param name name of variable (can be {@code null})
   * @param ii input info
   * @throws QueryException query exception
   */
  public void treat(final Value value, final QNm name, final InputInfo ii)
      throws QueryException {

    if(value.seqType().instanceOf(this)) return;

    final int size = (int) value.size();
    if(!occ.check(size)) throw typeError(value, this, name, ii);

    // empty sequence has all types
    if(size == 0) return;
    // check first item
    boolean ins = instance(value.itemAt(0));

    // check heterogeneous sequences
    if(!value.homogeneous())
      for(int i = 1; ins && i < size; i++) ins = instance(value.itemAt(i));
    if(!ins) throw typeError(value, this, name, ii);
  }

  /**
   * Promotes a value to the type of this sequence type.
   * @param value value to convert
   * @param name variable name (can be {@code null})
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param opt if the result should be optimized
   * @return converted value
   * @throws QueryException if the conversion was not possible
   */
  public Value promote(final Value value, final QNm name, final QueryContext qc,
      final StaticContext sc, final InputInfo ii, final boolean opt) throws QueryException {

    final int n = (int) value.size();
    if(!occ.check(n)) throw typeError(value, this, name, ii);
    if(n == 0) return Empty.SEQ;

    ItemList cache = null;
    for(int i = 0; i < n; i++) {
      final Item it = value.itemAt(i);
      if(instance(it)) {
        if(i == 0 && value.homogeneous()) return value;
        if(cache != null) cache.add(it);
      } else {
        if(cache == null) {
          cache = new ItemList(n);
          for(int j = 0; j < i; j++) cache.add(value.itemAt(j));
        }
        promote(it, name, cache, qc, sc, ii, opt);
      }
    }
    return cache != null ? cache.value(type) : value;
  }

  /**
   * Promotes an item to the type of this sequence type.
   * @param item item to promote
   * @param name variable name (can be {@code null})
   * @param cache item cache
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param opt if the result should be optimized
   * @throws QueryException query exception
   */
  public void promote(final Item item, final QNm name, final ItemList cache, final QueryContext qc,
      final StaticContext sc, final InputInfo ii, final boolean opt) throws QueryException {

    if(type instanceof AtomType) {
      for(final Item atom : item.atomValue(ii)) {
        final Type tp = atom.type;
        if(tp.instanceOf(type)) {
          cache.add(atom);
        } else if(tp == AtomType.ATM) {
          if(type.nsSensitive()) throw NSSENS_X_X.get(ii, item.type, type);
          for(final Item it : type.cast(atom, qc, sc, ii)) cache.add(it);
        } else if(type == AtomType.DBL && (tp == AtomType.FLT || tp.instanceOf(AtomType.DEC))) {
          cache.add(Dbl.get(atom.dbl(ii)));
        } else if(type == AtomType.FLT && tp.instanceOf(AtomType.DEC)) {
          cache.add(Flt.get(atom.flt(ii)));
        } else if(type == AtomType.STR && atom instanceof Uri) {
          cache.add(Str.get(atom.string(ii)));
        } else {
          throw typeError(item, with(Occ.ONE), name, ii);
        }
      }
    } else if(item instanceof FItem && type instanceof FuncType) {
      cache.add(((FItem) item).coerceTo((FuncType) type, qc, ii, opt));
    } else {
      throw typeError(item, with(Occ.ONE), name, ii);
    }
  }

  /**
   * Checks if this type could be converted to the given one by function conversion.
   * @param st type to convert to
   * @return result of check
   */
  public boolean promotable(final SeqType st) {
    if(intersect(st) != null) return true;
    if(occ.intersect(st.occ) == null) return false;
    final Type to = st.type;
    if(to instanceof AtomType) {
      if(type.isUntyped()) return !to.nsSensitive();
      return to == AtomType.DBL && (couldBe(AtomType.FLT) || couldBe(AtomType.DEC))
          || to == AtomType.FLT && couldBe(AtomType.DEC)
          || to == AtomType.STR && couldBe(AtomType.URI);
    }
    return st.type instanceof FuncType && type instanceof FuncType;
  }

  /**
   * Checks if this type's item type could be instance of the given one.
   * @param t other type
   * @return result of check
   */
  private boolean couldBe(final Type t) {
    return type.intersect(t) != null;
  }

  /**
   * Computes the union of two sequence types, i.e. the lowest common ancestor of both types.
   * @param st second type
   * @return resulting type
   */
  public SeqType union(final SeqType st) {
    // ignore general type of empty sequence
    final Type t = type == st.type || st.zero() ? type : zero() ? st.type : type.union(st.type);
    final Occ o = occ.union(st.occ);
    return get(t, o);
  }

  /**
   * Adds two sequence types.
   * @param st second type
   * @return resulting type
   */
  public SeqType add(final SeqType st) {
    return zero() ? st : st.zero() ? this : SeqType.get(type.union(st.type), occ.add(st.occ));
  }

  /**
   * Computes the intersection of two sequence types, i.e. the most general type that is
   * sub-type of both types. If no such type exists, {@code null} is returned.
   * @param st second type
   * @return resulting type or {@code null}
   */
  public SeqType intersect(final SeqType st) {
    final Occ o = occ.intersect(st.occ);
    if(o == null) return null;
    final Type tp = type.intersect(st.type);
    if(tp == null) return null;
    if(kind == null || st.kind == null || kind.equals(st.kind))
      return get(tp, o, kind != null ? kind : st.kind);
    final Test k = kind.intersect(st.kind);
    return k == null ? null : get(tp, o, k);
  }

  /**
   * Tests if the type yields at most one item.
   * @return result of check
   */
  public boolean zeroOrOne() {
    return occ.max <= 1;
  }

  /**
   * Tests if the type yields zero items.
   * @return result of check
   */
  public boolean zero() {
    return occ == Occ.ZERO;
  }

  /**
   * Tests if the type yields exactly one item.
   * @return result of check
   */
  public boolean one() {
    return occ == Occ.ONE;
  }

  /**
   * Tests if the type yields exactly one item and is no array.
   * @return result of check
   */
  public boolean oneNoArray() {
    return one() && !mayBeArray();
  }

  /**
   * Tests if the type yields exactly one or more items.
   * @return result of check
   */
  public boolean oneOrMore() {
    return occ.min >= 1;
  }

  /**
   * Tests if the type may yield zero items.
   * @return result of check
   */
  public boolean mayBeEmpty() {
    return occ.min == 0;
  }

  /**
   * Tests if the type may be numeric. User for predicate rewritings.
   * @return result of check
   */
  public boolean mayBeNumber() {
    // check if type is number, or any other super type
    return !zero() && (type.isNumber() || AtomType.AAT.instanceOf(type));
  }

  /**
   * Tests if the type may be an array.
   * @return result of check
   */
  public boolean mayBeArray() {
    return !(zero() ||
        type.instanceOf(AtomType.AAT) ||
        type instanceof ListType ||
        type instanceof MapType ||
        type instanceof NodeType);
  }

  /**
   * Checks if this sequence type is an instance of the specified sequence type.
   * @param st sequence type to check
   * @return result of check
   */
  public boolean instanceOf(final SeqType st) {
    final Type t1 = type, t2 = st.type;
    final Test k1 = kind, k2 = st.kind;
    return (t2 == AtomType.ITEM || t1.instanceOf(t2)) && occ.instanceOf(st.occ) &&
      // [LW] complete kind check
      (k2 == null || k1 != null && k1.intersect(k2) != null);
  }

  /**
   * Returns the atomic type for the specified expression.
   * @return type or {@code null} if type may be not atomic
   */
  public Type atomicType() {
    final Type t = type;
    return t.instanceOf(NodeType.NOD) ?
      t == NodeType.PI || t == NodeType.COM ? AtomType.STR :
      t == NodeType.NOD ? AtomType.AAT : AtomType.ATM :
      t.instanceOf(AtomType.AAT) ? t : null;
  }

  /**
   * Checks the types for equality.
   * @param st type
   * @return result of check
   */
  public boolean eq(final SeqType st) {
    return type.eq(st.type) && occ == st.occ && Objects.equals(kind, st.kind);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SeqType && eq((SeqType) obj);
  }


  /**
   * Returns a string representation of the type.
   * @return string
   */
  public String typeString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(zero() ? EMPTY_SEQUENCE + "()" : type);
    if(kind != null) sb.deleteCharAt(sb.length() - 1).append(kind).append(')');
    return sb.toString();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(!one() && type instanceof FuncType) {
      sb.append('(').append(typeString()).append(')');
    } else {
      sb.append(typeString());
    }
    if(!(type instanceof ListType)) sb.append(occ);
    return sb.toString();
  }
}
