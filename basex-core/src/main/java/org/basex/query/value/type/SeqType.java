package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.NodeType.*;
import static org.basex.query.value.type.Occ.*;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Zero items (single instance). */
  public static final SeqType EMPTY_SEQUENCE_Z = ITEM.seqType(ZERO);

  /** Single item. */
  public static final SeqType ITEM_O = ITEM.seqType();
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = ITEM.seqType(ZERO_OR_ONE);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = ITEM.seqType(ZERO_OR_MORE);
  /** One or more items. */
  public static final SeqType ITEM_OM = ITEM.seqType(ONE_OR_MORE);

  /** Zero or one xs:anyAtomicType. */
  public static final SeqType ANY_ATOMIC_TYPE_O = ANY_ATOMIC_TYPE.seqType();
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType ANY_ATOMIC_TYPE_ZO = ANY_ATOMIC_TYPE.seqType(ZERO_OR_ONE);
  /** Zero or more xs:anyAtomicType. */
  public static final SeqType ANY_ATOMIC_TYPE_ZM = ANY_ATOMIC_TYPE.seqType(ZERO_OR_MORE);

  /** Zero or one xs:numeric. */
  public static final SeqType NUMERIC_O = NUMERIC.seqType();
  /** Zero or one xs:numeric. */
  public static final SeqType NUMERIC_ZO = NUMERIC.seqType(ZERO_OR_ONE);
  /** Double number. */
  public static final SeqType DOUBLE_O = DOUBLE.seqType();
  /** Zero or one double. */
  public static final SeqType DOUBLE_ZO = DOUBLE.seqType(ZERO_OR_ONE);
  /** Double number. */
  public static final SeqType DOUBLE_ZM = DOUBLE.seqType(ZERO_OR_MORE);
  /** Float number. */
  public static final SeqType FLOAT_O = FLOAT.seqType();
  /** Zero or one decimal number. */
  public static final SeqType DECIMAL_ZO = DECIMAL.seqType(ZERO_OR_ONE);
  /** Single integer. */
  public static final SeqType INTEGER_O = INTEGER.seqType();
  /** Zero or one integer. */
  public static final SeqType INTEGER_ZO = INTEGER.seqType(ZERO_OR_ONE);
  /** Zero or more integers. */
  public static final SeqType INTEGER_ZM = INTEGER.seqType(ZERO_OR_MORE);
  /** Zero or more bytes. */
  public static final SeqType BYTE_ZM = BYTE.seqType(ZERO_OR_MORE);

  /** Single string. */
  public static final SeqType STRING_O = STRING.seqType();
  /** Zero or one strings. */
  public static final SeqType STRING_ZO = STRING.seqType(ZERO_OR_ONE);
  /** Zero or more strings. */
  public static final SeqType STRING_ZM = STRING.seqType(ZERO_OR_MORE);
  /** Zero or one NCName. */
  public static final SeqType NCNAME_ZO = NCNAME.seqType(ZERO_OR_ONE);
  /** Single language. */
  public static final SeqType LANGUAGE_O = LANGUAGE.seqType();

  /** Single URI. */
  public static final SeqType ANY_URI_O = ANY_URI.seqType();
  /** Zero or one URIs. */
  public static final SeqType ANY_URI_ZO = ANY_URI.seqType(ZERO_OR_ONE);
  /** Zero or more URIs. */
  public static final SeqType ANY_URI_ZM = ANY_URI.seqType(ZERO_OR_MORE);

  /** Single QName. */
  public static final SeqType QNAME_O = QNAME.seqType();
  /** Zero or one QNames. */
  public static final SeqType QNAME_ZO = QNAME.seqType(ZERO_OR_ONE);

  /** Single xs:boolean. */
  public static final SeqType BOOLEAN_O = BOOLEAN.seqType();
  /** Zero or one xs:boolean. */
  public static final SeqType BOOLEAN_ZO = BOOLEAN.seqType(ZERO_OR_ONE);

  /** Single date. */
  public static final SeqType DATE_O = DATE.seqType();
  /** Zero or one date. */
  public static final SeqType DATE_ZO = DATE.seqType(ZERO_OR_ONE);
  /** One day-time-duration. */
  public static final SeqType DAY_TIME_DURATION_O = DAY_TIME_DURATION.seqType();
  /** Zero or one day-time-duration. */
  public static final SeqType DAY_TIME_DURATION_ZO = DAY_TIME_DURATION.seqType(ZERO_OR_ONE);
  /** One date-time. */
  public static final SeqType DATE_TIME_O = DATE_TIME.seqType();
  /** Zero or one date-time. */
  public static final SeqType DATE_TIME_ZO = DATE_TIME.seqType(ZERO_OR_ONE);
  /** One time. */
  public static final SeqType TIME_O = TIME.seqType();
  /** Zero or one time. */
  public static final SeqType TIME_ZO = TIME.seqType(ZERO_OR_ONE);
  /** Zero or one duration. */
  public static final SeqType DURATION_ZO = DURATION.seqType(ZERO_OR_ONE);

  /** Single binary. */
  public static final SeqType BINARY_O = BINARY.seqType();
  /** One xs:hexBinary. */
  public static final SeqType HEX_BINARY_O = HEX_BINARY.seqType();
  /** Zero or one xs:hexBinary. */
  public static final SeqType HEX_BINARY_ZO = HEX_BINARY.seqType(ZERO_OR_ONE);
  /** Single xs:base64Binary. */
  public static final SeqType BASE64_BINARY_O = BASE64_BINARY.seqType();
  /** Zero or one xs:base64Binary. */
  public static final SeqType BASE64_BINARY_ZO = BASE64_BINARY.seqType(ZERO_OR_ONE);
  /** Zero or more xs:base64Binary. */
  public static final SeqType BASE64_BINARY_ZM = BASE64_BINARY.seqType(ZERO_OR_MORE);

  /** Single node. */
  public static final SeqType NODE_O = NODE.seqType();
  /** Zero or one nodes. */
  public static final SeqType NODE_ZO = NODE.seqType(ZERO_OR_ONE);
  /** Zero or more nodes. */
  public static final SeqType NODE_ZM = NODE.seqType(ZERO_OR_MORE);
  /** One or more nodes. */
  public static final SeqType NODE_OM = NODE.seqType(ONE_OR_MORE);
  /** One attribute node. */
  public static final SeqType ATTRIBUTE_O = ATTRIBUTE.seqType();
  /** Zero or more attributes. */
  public static final SeqType ATTRIBUTE_ZM = ATTRIBUTE.seqType(ZERO_OR_MORE);
  /** One comment node. */
  public static final SeqType COMMENT_O = COMMENT.seqType();
  /** One document node. */
  public static final SeqType DOCUMENT_NODE_O = DOCUMENT_NODE.seqType();
  /** Zero or one document node. */
  public static final SeqType DOCUMENT_NODE_ZO = DOCUMENT_NODE.seqType(ZERO_OR_ONE);
  /** Zero or more document node. */
  public static final SeqType DOCUMENT_NODE_ZM = DOCUMENT_NODE.seqType(ZERO_OR_MORE);
  /** One element node. */
  public static final SeqType ELEMENT_O = ELEMENT.seqType();
  /** Zero or more element nodes. */
  public static final SeqType ELEMENT_ZM = ELEMENT.seqType(ZERO_OR_MORE);
  /** Namespace node. */
  public static final SeqType NAMESPACE_NODE_O = NAMESPACE_NODE.seqType();
  /** Processing instruction. */
  public static final SeqType PROCESSING_INSTRUCTION_O = PROCESSING_INSTRUCTION.seqType();
  /** Zero or one text node. */
  public static final SeqType TEXT_ZO = TEXT.seqType(ZERO_OR_ONE);
  /** Zero or more text nodes. */
  public static final SeqType TEXT_ZM = TEXT.seqType(ZERO_OR_MORE);

  // function types must be placed here due to circular dependencies

  /** Any function type. */
  public static final FuncType FUNCTION = new FuncType(null, (SeqType[]) null);
  /** The general map type. */
  public static final MapType MAP = new MapType(ANY_ATOMIC_TYPE, ITEM_ZM);
  /** The general array type. */
  public static final ArrayType ARRAY = new ArrayType(ITEM_ZM);

  /** Single function. */
  public static final SeqType FUNCTION_O = FUNCTION.seqType();
  /** Zero of single function. */
  public static final SeqType FUNCTION_ZO = FUNCTION.seqType(ZERO_OR_ONE);
  /** Zero of more functions. */
  public static final SeqType FUNCTION_ZM = FUNCTION.seqType(ZERO_OR_MORE);
  /** Single map. */
  public static final SeqType MAP_O = MAP.seqType();
  /** Zero or one map. */
  public static final SeqType MAP_ZO = MAP.seqType(ZERO_OR_ONE);
  /** Zero or more maps. */
  public static final SeqType MAP_ZM = MAP.seqType(ZERO_OR_MORE);
  /** Single array. */
  public static final SeqType ARRAY_O = ARRAY.seqType();
  /** Zero or more arrays. */
  public static final SeqType ARRAY_ZM = ARRAY.seqType(ZERO_OR_MORE);

  /** Item type. */
  public final Type type;
  /** Occurrence indicator. */
  public final Occ occ;
  /** Kind test (can be {@code null}). */
  private final Test test;

  /**
   * Constructor.
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
    return occ == ZERO ? EMPTY_SEQUENCE_Z : type.seqType(occ);
  }

  /**
   * Returns a sequence type.
   * @param type type
   * @param occ occurrence indicator
   * @param test kind test (can be {@code null}; ignored if this is no node type)
   * @return sequence type
   */
  public static SeqType get(final Type type, final Occ occ, final Test test) {
    return occ == ZERO || test == null || !(type instanceof NodeType) ?
      get(type, occ) : new SeqType(type, occ, test);
  }

  /**
   * Returns a sequence type with the specified occurrence indicator.
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType with(final Occ oc) {
    return oc == occ ? this : get(type, oc, test);
  }

  /**
   * Returns a sequence type with a new occurrence indicator.
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType union(final Occ oc) {
    return oc == occ ? this : get(type, occ.union(oc), test);
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
   * Casts a sequence to this type.
   * @param value value to cast
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return cast value
   * @throws QueryException query exception
   */
  public Value cast(final Value value, final boolean error, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    // check cardinality
    final long size = value.size();
    if(!occ.check(size)) {
      if(error) throw INVTYPE_X_X_X.get(ii, value.seqType(), this, value);
      return null;
    }
    if(size == 0) return Empty.VALUE;

    try {
      // enable light-weight error handling
      if(!error && ii != null) ii.internal(true);
      // cast single items
      if(size == 1) {
        final Item item = (Item) value;
        return item.type.eq(type) ? item : type.cast(item, qc, sc, ii);
      }
      // cast sequences
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item item : value) {
        if(item.type.eq(type)) {
          vb.add(item);
        } else {
          qc.checkStop();
          vb.add(type.cast(item, qc, sc, ii));
        }
      }
      return vb.value(type);
    } catch(final QueryException ex) {
      if(error) throw ex;
      return null;
    } finally {
      if(!error && ii != null) ii.internal(false);
    }
  }

  /**
   * Treats the specified value as this sequence type.
   * @param value value to check
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
   * Promotes the specified value to this sequence type.
   * @param value value to promote
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
    ItemList items = null;
    for(long i = 0; i < size; i++) {
      qc.checkStop();
      final Item item = value.itemAt(i);
      if(instance(item)) {
        if(items != null) items.add(item);
      } else {
        if(items == null) {
          items = new ItemList(Seq.initialCapacity(size));
          for(int j = 0; j < i; j++) items.add(value.itemAt(j));
        }
        promote(item, name, items, qc, sc, ii, opt);
      }
    }
    final long is = items != null ? items.size() : value.size();
    if(!occ.check(is)) throw typeError(value, this, name, ii, true);
    return items != null ? items.value(type) : value;
  }

  /**
   * Promotes the specified item to this item type.
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
        } else if(tp == UNTYPED_ATOMIC) {
          if(type.nsSensitive()) throw NSSENS_X_X.get(ii, item.type, type);
          final Iter iter2 = type.cast(item1, qc, sc, ii).iter();
          for(Item item2; (item2 = qc.next(iter2)) != null;) items.add(item2);
        } else if(type == DOUBLE && (tp == FLOAT || tp.instanceOf(DECIMAL))) {
          items.add(Dbl.get(item1.dbl(ii)));
        } else if(type == FLOAT && tp.instanceOf(DECIMAL)) {
          items.add(Flt.get(item1.flt(ii)));
        } else if(type == STRING && item1 instanceof Uri) {
          items.add(Str.get(item1.string(ii)));
        } else {
          throw typeError(item, with(EXACTLY_ONE), name, ii, true);
        }
      }
    } else if(item instanceof FItem && type instanceof FuncType) {
      items.add(((FItem) item).coerceTo((FuncType) type, qc, ii, opt));
    } else {
      throw typeError(item, with(EXACTLY_ONE), name, ii, true);
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
      return tp == DOUBLE && (type.intersect(FLOAT) != null || type.intersect(DECIMAL) != null) ||
             tp == FLOAT && type.intersect(DECIMAL) != null ||
             tp == STRING && type.intersect(ANY_URI) != null;
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
    final Type tp = st.zero() ? type : zero() ? st.type : type.union(st.type);
    final Occ oc = occ.union(st.occ);
    final Test ts = st.zero() ? test : zero() ? st.test : Test.get(test, st.test);
    return get(tp, oc, ts);
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
    return occ == ZERO;
  }

  /**
   * Tests if expressions of this type yield one item.
   * @return result of check
   */
  public boolean one() {
    return occ == EXACTLY_ONE;
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
    return !zero() && (type.isNumber() || ANY_ATOMIC_TYPE.instanceOf(type));
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
      (st.type == ITEM || type.instanceOf(st.type)) &&
      occ.instanceOf(st.occ) && kindInstanceOf(st);
  }

  /**
   * Checks if the kind of this sequence type is an instance of the kind of the specified
   * sequence type.
   * @param st sequence type to check
   * @return result of check
   */
  public boolean kindInstanceOf(final SeqType st) {
    return st.test == null || test != null && test.instanceOf(st.test);
  }

  /**
   * Checks the types for equality.
   * @param st type
   * @return result of check
   */
  public boolean eq(final SeqType st) {
    return this == st || type.eq(st.type) && occ == st.occ && Objects.equals(test, st.test);
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
    return zero() ? QueryText.EMPTY_SEQUENCE + "()" :
      test != null ? test.toString() : type.toString();
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
