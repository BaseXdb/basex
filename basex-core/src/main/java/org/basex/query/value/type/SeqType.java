package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Stores a sequence type definition.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Zero items (single instance). */
  public static final SeqType EMP = AtomType.ITEM.seqType(Occ.ZERO);

  /** Single item. */
  public static final SeqType ITEM_O = AtomType.ITEM.seqType();
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = AtomType.ITEM.seqType(Occ.ZERO_ONE);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = AtomType.ITEM.seqType(Occ.ZERO_MORE);
  /** One or more items. */
  public static final SeqType ITEM_OM = AtomType.ITEM.seqType(Occ.ONE_MORE);

  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT_O = AtomType.AAT.seqType();
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType AAT_ZO = AtomType.AAT.seqType(Occ.ZERO_ONE);
  /** Zero or more xs:anyAtomicType. */
  public static final SeqType AAT_ZM = AtomType.AAT.seqType(Occ.ZERO_MORE);

  /** Zero or one xs:numeric. */
  public static final SeqType NUM_O = AtomType.NUM.seqType();
  /** Zero or one xs:numeric. */
  public static final SeqType NUM_ZO = AtomType.NUM.seqType(Occ.ZERO_ONE);
  /** Double number. */
  public static final SeqType DBL_O = AtomType.DBL.seqType();
  /** Zero or one double. */
  public static final SeqType DBL_ZO = AtomType.DBL.seqType(Occ.ZERO_ONE);
  /** Double number. */
  public static final SeqType DBL_ZM = AtomType.DBL.seqType(Occ.ZERO_MORE);
  /** Float number. */
  public static final SeqType FLT_O = AtomType.FLT.seqType();
  /** Zero or one decimal number. */
  public static final SeqType DEC_ZO = AtomType.DEC.seqType(Occ.ZERO_ONE);
  /** Single integer. */
  public static final SeqType ITR_O = AtomType.ITR.seqType();
  /** Zero or one integer. */
  public static final SeqType ITR_ZO = AtomType.ITR.seqType(Occ.ZERO_ONE);
  /** Zero or more integers. */
  public static final SeqType ITR_ZM = AtomType.ITR.seqType(Occ.ZERO_MORE);
  /** Zero or more bytes. */
  public static final SeqType BYT_ZM = AtomType.BYT.seqType(Occ.ZERO_MORE);

  /** Single string. */
  public static final SeqType STR_O = AtomType.STR.seqType();
  /** Zero or one strings. */
  public static final SeqType STR_ZO = AtomType.STR.seqType(Occ.ZERO_ONE);
  /** Zero or more strings. */
  public static final SeqType STR_ZM = AtomType.STR.seqType(Occ.ZERO_MORE);
  /** Zero or one NCName. */
  public static final SeqType NCN_ZO = AtomType.NCN.seqType(Occ.ZERO_ONE);
  /** Single language. */
  public static final SeqType LAN_O = AtomType.LAN.seqType();

  /** Single URI. */
  public static final SeqType URI_O = AtomType.URI.seqType();
  /** Zero or one URIs. */
  public static final SeqType URI_ZO = AtomType.URI.seqType(Occ.ZERO_ONE);
  /** Zero or more URIs. */
  public static final SeqType URI_ZM = AtomType.URI.seqType(Occ.ZERO_MORE);

  /** Single QName. */
  public static final SeqType QNM_O = AtomType.QNM.seqType();
  /** Zero or one QNames. */
  public static final SeqType QNM_ZO = AtomType.QNM.seqType(Occ.ZERO_ONE);

  /** Single xs:boolean. */
  public static final SeqType BLN_O = AtomType.BLN.seqType();
  /** Zero or one xs:boolean. */
  public static final SeqType BLN_ZO = AtomType.BLN.seqType(Occ.ZERO_ONE);

  /** Single date. */
  public static final SeqType DAT_O = AtomType.DAT.seqType();
  /** Zero or one date. */
  public static final SeqType DAT_ZO = AtomType.DAT.seqType(Occ.ZERO_ONE);
  /** One day-time-duration. */
  public static final SeqType DTD_O = AtomType.DTD.seqType();
  /** Zero or one day-time-duration. */
  public static final SeqType DTD_ZO = AtomType.DTD.seqType(Occ.ZERO_ONE);
  /** One date-time. */
  public static final SeqType DTM_O = AtomType.DTM.seqType();
  /** Zero or one date-time. */
  public static final SeqType DTM_ZO = AtomType.DTM.seqType(Occ.ZERO_ONE);
  /** One time. */
  public static final SeqType TIM_O = AtomType.TIM.seqType();
  /** Zero or one time. */
  public static final SeqType TIM_ZO = AtomType.TIM.seqType(Occ.ZERO_ONE);
  /** Zero or one duration. */
  public static final SeqType DUR_ZO = AtomType.DUR.seqType(Occ.ZERO_ONE);

  /** Single binary. */
  public static final SeqType BIN_O = AtomType.BIN.seqType();
  /** One xs:hexBinary. */
  public static final SeqType HEX_O = AtomType.HEX.seqType();
  /** Zero or one xs:hexBinary. */
  public static final SeqType HEX_ZO = AtomType.HEX.seqType(Occ.ZERO_ONE);
  /** Single xs:base64Binary. */
  public static final SeqType B64_O = AtomType.B64.seqType();
  /** Zero or one xs:base64Binary. */
  public static final SeqType B64_ZO = AtomType.B64.seqType(Occ.ZERO_ONE);
  /** Zero or more xs:base64Binary. */
  public static final SeqType B64_ZM = AtomType.B64.seqType(Occ.ZERO_MORE);

  /** Single node. */
  public static final SeqType NOD_O = NodeType.NOD.seqType();
  /** Zero or one nodes. */
  public static final SeqType NOD_ZO = NodeType.NOD.seqType(Occ.ZERO_ONE);
  /** Zero or more nodes. */
  public static final SeqType NOD_ZM = NodeType.NOD.seqType(Occ.ZERO_MORE);
  /** One or more nodes. */
  public static final SeqType NOD_OM = NodeType.NOD.seqType(Occ.ONE_MORE);
  /** One attribute node. */
  public static final SeqType ATT_O = NodeType.ATT.seqType();
  /** Zero or more attributes. */
  public static final SeqType ATT_ZM = NodeType.ATT.seqType(Occ.ZERO_MORE);
  /** One comment node. */
  public static final SeqType COM_O = NodeType.COM.seqType();
  /** One document node. */
  public static final SeqType DOC_O = NodeType.DOC.seqType();
  /** Zero or one document node. */
  public static final SeqType DOC_ZO = NodeType.DOC.seqType(Occ.ZERO_ONE);
  /** Zero or more document node. */
  public static final SeqType DOC_ZM = NodeType.DOC.seqType(Occ.ZERO_MORE);
  /** One element node. */
  public static final SeqType ELM_O = NodeType.ELM.seqType();
  /** Zero or more element nodes. */
  public static final SeqType ELM_ZM = NodeType.ELM.seqType(Occ.ZERO_MORE);
  /** Namespace node. */
  public static final SeqType NSP_O = NodeType.NSP.seqType();
  /** Processing instruction. */
  public static final SeqType PI_O = NodeType.PI.seqType();
  /** Zero or one text node. */
  public static final SeqType TXT_ZO = NodeType.TXT.seqType(Occ.ZERO_ONE);
  /** Zero or more text nodes. */
  public static final SeqType TXT_ZM = NodeType.TXT.seqType(Occ.ZERO_MORE);

  /** Single function. */
  public static final SeqType FUNC_O = FuncType.FUNCTION.seqType();
  /** Zero of single function. */
  public static final SeqType FUNC_ZO = FuncType.FUNCTION.seqType(Occ.ZERO_ONE);
  /** Zero of more functions. */
  public static final SeqType FUNC_ZM = FuncType.FUNCTION.seqType(Occ.ZERO_MORE);
  /** Single map. */
  public static final SeqType MAP_O = MapType.MAP.seqType();
  /** Zero or one map. */
  public static final SeqType MAP_ZO = MapType.MAP.seqType(Occ.ZERO_ONE);
  /** Zero or more maps. */
  public static final SeqType MAP_ZM = MapType.MAP.seqType(Occ.ZERO_MORE);
  /** Single array. */
  public static final SeqType ARRAY_O = ArrayType.ARRAY.seqType();
  /** Zero or more arrays. */
  public static final SeqType ARRAY_ZM = ArrayType.ARRAY.seqType(Occ.ZERO_MORE);

  /** Item type. */
  public final Type type;
  /** Occurrence indicator. */
  public final Occ occ;
  /** Kind test (can be {@code null}). */
  private final Test test;

  /**
   * Private constructor.
   * @param type type
   * @param occ occurrence
   */
  SeqType(final Type type, final Occ occ) {
    this(type, occ, null);
  }

  /**
   * Private constructor.
   * @param type type
   * @param occ occurrence indicator
   * @param test kind test (can be {@code null})
   */
  private SeqType(final Type type, final Occ occ, final Test test) {
    this.type = type;
    this.occ = occ;
    this.test = test;
  }

  /**
   * Returns a sequence type.
   * @param type type
   * @param occ occurrence indicator
   * @return sequence type
   */
  public static SeqType get(final Type type, final Occ occ) {
    return occ == Occ.ZERO ? EMP : type.seqType(occ);
  }

  /**
   * Returns a sequence type.
   * @param type type
   * @param occ occurrence indicator
   * @param test kind test (can be {@code null})
   * @return sequence type
   */
  public static SeqType get(final Type type, final Occ occ, final Test test) {
    return occ == Occ.ZERO || test == null ? get(type, occ) : new SeqType(type, occ, test);
  }

  /**
   * Returns a sequence type with the specified type and occurrence indicator.
   * @param tp type
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType with(final Type tp, final Occ oc) {
    return type.eq(tp) && occ == oc && test == null ? this : get(tp, oc);
  }

  /**
   * Returns a version of this sequence type that is adapted to the given {@link Occ}.
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType with(final Occ oc) {
    return oc == occ ? this : get(type, oc, test);
  }

  /**
   * Returns a version of this sequence type that is adapted to the given type.
   * @param tp type
   * @return sequence type
   */
  public SeqType with(final Type tp) {
    return type.eq(tp) ? this : get(tp, occ, test);
  }

  /**
   * Checks if the specified value is an instance of this type.
   * @param value value to check
   * @return result of check
   */
  public boolean instance(final Value value) {
    // try shortcut (but value type may be too general)
    if(value.seqType().instanceOf(this)) return true;

    // check cardinality
    if(!occ.check(value.size())) return false;

    // value type may be too general: check type of each item
    for(final Item item : value) {
      if(!instance(item)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified item is an instance of this sequence type.
   * @param item item to check
   * @return result of check
   */
  public boolean instance(final Item item) {
    return item.instanceOf(type) && (test == null || test.matches(item));
  }

  /**
   * Casts the given item to this sequence type.
   * @param item item to cast
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return cast value or {@code null}
   * @throws QueryException query exception
   */
  public Value cast(final Item item, final boolean error, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    // kind test is ignored (simple casts have no kind test)
    if(item.type.eq(type)) return item;
    try {
      if(!error && ii != null) ii.internal(true);
      return type.cast(item, qc, sc, ii);
    } catch(final QueryException ex) {
      if(error) throw ex;
      return null;
    } finally {
      if(!error && ii != null) ii.internal(false);
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

    // check cardinality
    final long size = value.size();
    if(!occ.check(size)) throw INVTYPE_X_X_X.get(ii, value.seqType(), this, value);

    // handle simple types
    if(size == 0) return Empty.VALUE;
    if(size == 1) return cast((Item) value, true, qc, sc, ii);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      qc.checkStop();
      vb.add(cast(item, true, qc, sc, ii));
    }
    return vb.value(type);
  }

  /**
   * Checks the specified value for this sequence type.
   * @param value value to be checked
   * @param name name of variable (can be {@code null})
   * @param qc query context
   * @param ii input info
   * @throws QueryException query exception
   */
  public void treat(final Value value, final QNm name, final QueryContext qc, final InputInfo ii)
      throws QueryException {

    // try shortcut (but value type may be too general)
    if(value.seqType().instanceOf(this)) return;

    // check cardinality
    if(!occ.check(value.size())) throw typeError(value, this, name, ii, false);

    for(final Item item : value) {
      qc.checkStop();
      if(!instance(item)) throw typeError(value, this, name, ii, false);
    }
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

    final long size = value.size();
    if(!occ.check(size)) throw typeError(value, this, name, ii, true);
    if(size == 0) return Empty.VALUE;

    ItemList items = null;
    for(long i = 0; i < size; i++) {
      qc.checkStop();
      final Item item = value.itemAt(i);
      if(instance(item)) {
        if(items != null) items.add(item);
      } else {
        if(items == null) {
          items = new ItemList(size);
          for(int j = 0; j < i; j++) items.add(value.itemAt(j));
        }
        promote(item, name, items, qc, sc, ii, opt);
      }
    }
    return items != null ? items.value(type) : value;
  }

  /**
   * Promotes an item to the type of this sequence type.
   * @param item item to promote
   * @param name variable name (can be {@code null})
   * @param items item cache
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param opt if the result should be optimized
   * @throws QueryException query exception
   */
  public void promote(final Item item, final QNm name, final ItemList items, final QueryContext qc,
      final StaticContext sc, final InputInfo ii, final boolean opt) throws QueryException {

    if(type instanceof AtomType) {
      final Iter iter = item.atomValue(qc, ii).iter();
      for(Item item1; (item1 = qc.next(iter)) != null;) {
        final Type tp = item1.type;
        if(tp.instanceOf(type)) {
          items.add(item1);
        } else if(tp == AtomType.ATM) {
          if(type.nsSensitive()) throw NSSENS_X_X.get(ii, item.type, type);
          final Iter iter2 = type.cast(item1, qc, sc, ii).iter();
          for(Item item2; (item2 = qc.next(iter2)) != null;) items.add(item2);
        } else if(type == AtomType.DBL && (tp == AtomType.FLT || tp.instanceOf(AtomType.DEC))) {
          items.add(Dbl.get(item1.dbl(ii)));
        } else if(type == AtomType.FLT && tp.instanceOf(AtomType.DEC)) {
          items.add(Flt.get(item1.flt(ii)));
        } else if(type == AtomType.STR && item1 instanceof Uri) {
          items.add(Str.get(item1.string(ii)));
        } else {
          throw typeError(item, with(Occ.ONE), name, ii, true);
        }
      }
    } else if(item instanceof FItem && type instanceof FuncType) {
      items.add(((FItem) item).coerceTo((FuncType) type, qc, ii, opt));
    } else {
      throw typeError(item, with(Occ.ONE), name, ii, true);
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
    final Type tp = st.type;
    if(tp instanceof AtomType) {
      if(type.isUntyped()) return !tp.nsSensitive();
      return tp == AtomType.DBL && (type.intersect(AtomType.FLT) != null ||
               type.intersect(AtomType.DEC) != null) ||
             tp == AtomType.FLT && type.intersect(AtomType.DEC) != null ||
             tp == AtomType.STR && type.intersect(AtomType.URI) != null;
    }
    return st.type instanceof FuncType && type instanceof FuncType;
  }

  /**
   * Computes the union of two sequence types, i.e. the lowest common ancestor of both types.
   * @param st second type
   * @return resulting type
   */
  public SeqType union(final SeqType st) {
    // ignore general type of empty sequence
    final Type tp = type.eq(st.type) || st.zero() ? type : zero() ? st.type : type.union(st.type);
    final Occ oc = occ.union(st.occ);
    return get(tp, oc);
  }

  /**
   * Computes the intersection of two sequence types, i.e. the most general type that is
   * sub-type of both types. If no such type exists, {@code null} is returned.
   * @param st second type
   * @return resulting type or {@code null}
   */
  public SeqType intersect(final SeqType st) {
    final Type tp = type.intersect(st.type);
    if(tp == null) return null;
    final Occ oc = occ.intersect(st.occ);
    if(oc == null) return null;
    if(test == null || st.test == null || test.equals(st.test))
      return get(tp, oc, test != null ? test : st.test);
    final Test kn = test.intersect(st.test);
    return kn == null ? null : get(tp, oc, kn);
  }

  /**
   * Tests if expressions of this type yield at most one item.
   * @return result of check
   */
  public boolean zeroOrOne() {
    return occ.max <= 1;
  }

  /**
   * Tests if expressions of this type yield zero items.
   * @return result of check
   */
  public boolean zero() {
    return occ == Occ.ZERO;
  }

  /**
   * Tests if expressions of this type yield one item.
   * @return result of check
   */
  public boolean one() {
    return occ == Occ.ONE;
  }

  /**
   * Tests if expressions of this type yield one or more items.
   * @return result of check
   */
  public boolean oneOrMore() {
    return occ.min >= 1;
  }

  /**
   * Tests if expressions of this type may be numeric. User for predicate rewritings.
   * @return result of check
   */
  public boolean mayBeNumber() {
    // check if type is number, or any other super type
    return !zero() && (type.isNumber() || AtomType.AAT.instanceOf(type));
  }

  /**
   * Tests if expressions of this type may be an array.
   * @return result of check
   */
  public boolean mayBeArray() {
    return !(zero() || type.atomic() != null || type instanceof MapType);
  }

  /**
   * Checks if this sequence type is an instance of the specified sequence type.
   * @param st sequence type to check
   * @return result of check
   */
  public boolean instanceOf(final SeqType st) {
    // empty sequence: only check cardinality
    return zero() ? !st.oneOrMore() :
      (st.type == AtomType.ITEM || type.instanceOf(st.type)) && occ.instanceOf(st.occ) &&
      (st.test == null || test != null && test.intersect(st.test) != null);
  }

  /**
   * Checks the types for equality.
   * @param st type
   * @return result of check
   */
  public boolean eq(final SeqType st) {
    return this == st || type.eq(st.type) && occ == st.occ && Objects.equals(test, st.test);
  }

  /**
   * Returns a string representation of the type.
   * @return string
   */
  public String typeString() {
    return zero() ? EMPTY_SEQUENCE + "()" : test != null ? test.toString() : type.toString();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SeqType && eq((SeqType) obj);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(!one() && type instanceof FuncType) {
      tb.add('(').add(typeString()).add(')');
    } else {
      tb.add(typeString());
    }
    if(!(type instanceof ListType)) tb.add(occ);
    return tb.toString();
  }
}
